package com.zsh.spider.task;

import com.zsh.spider.config.SpiderConstants;
import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.dao.TaskDao;
import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.service.SpiderDispatcherService;
import com.zsh.spider.service.TaskStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 有界的优先级队列 E 目前只支持为PriorityTask
 */
@Slf4j
public class BordPriorityBlockingQueue<Runnable> extends PriorityBlockingQueue<Runnable> {

    private final Long maxSize;
    private final InnerBean inner;

    public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    public BordPriorityBlockingQueue(Long maxSize, InnerBean inner) {
        super();
        this.maxSize = maxSize;
        this.inner = inner;
        addTaskSchedule();
        deleteTaskSchedule();
        deleteExpireOrStopTaskSchedule();
    }

    @Override
    public boolean offer(Runnable o) {
        int size = super.size();
        if (size >= maxSize) {
            //throw new IllegalArgumentException("Priority out of bounds");
            return false;
        }
        return super.offer(o);
    }

    public void saveDataToDatabase() {
        List<SpiderTaskPO> list = super.stream()
            .map(obj -> (PriorityTask) obj)
            .map(PriorityTask::getSpiderTaskPO)
            .map(obj -> {
                // id设置为null, 用于saveAll执行插入操作
                obj.setId(null);
                return obj;
            })
            .toList();
        inner.spiderTaskDao.saveAllAndFlush(list);
        log.info("保存内存中任务到数据库：" + list.size());
    }

    /**
     * 任务数过少时 定时往线程池中增加任务
     */
    private void addTaskSchedule() {
        scheduledExecutorService.scheduleWithFixedDelay(new CatchErrorTask("定时添加爬虫任务", () -> {
            if (super.size() <= maxSize / 4) {
                long size = Math.min(10000, maxSize / 2);
                // 查询任务十分耗时, 所以延迟间隔比较短
                var list = inner.spiderTaskDao.findAllOrderByScoreDescLimit(size);
                if (list.isEmpty()) {
                    return;
                }
                // 直接调用offer方法不会激活线程
                for (SpiderTaskPO obj : list) {
                    inner.spiderDispatcherService.addSpiderTask(obj);
                }
                log.debug("添加成功新任务数量：" + list.size());
                List<Long> ids = list.stream().map(SpiderTaskPO::getId).toList();
                // 删除添加成功任务
                inner.spiderTaskDao.deleteAllById(ids);
                log.debug("删除成功已添加任务：" + list.size());
            }
        }), 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 负载过多时, 定时删除关联度低的任务
     */
    private void deleteTaskSchedule() {
        scheduledExecutorService.scheduleWithFixedDelay(new CatchErrorTask("定时删除关联度低任务", () -> {
            long size = inner.spiderTaskDao.count();
            double limitScore = SpiderConstants.DISCARD_SCORE;
            // 超出负载10倍
            if (size > 10 * maxSize) {
                // 任务数越多, 限制分数越严格
                limitScore = Math.min(SpiderConstants.HIGH_LOAD_BORD_SCORE,
                    SpiderConstants.DISCARD_SCORE * size / 10 * maxSize);
            }
            log.debug("限制任务关联分数：" + limitScore);
            inner.taskStateService.setLimitScore(limitScore);
            // 最小保留数据  10W 或者 10倍maxSize
            long maxDeleteCount = size - Math.max(100000, 10 * maxSize);
            // 先查询再删除速度更快
            var ids = inner.spiderTaskDao.listIdByScoreLtOrderByScoreDescLimit(limitScore, maxDeleteCount);
            inner.spiderTaskDao.deleteAllById(ids);
            log.info("删除关联度低任务数量:" + ids.size());
        }), 15, 120, TimeUnit.SECONDS);
    }

    /**
     * 删除已过期或者已停止任务
     * 任务一小时过期 TODO 时间待测试
     */
    private void deleteExpireOrStopTaskSchedule() {
        scheduledExecutorService.scheduleWithFixedDelay(new CatchErrorTask("删除已过期或者已停止任务", () -> {
            // 查询即将过期任务
            List<Long> taskIds = inner.taskDao.findIdToBeExpireTask(TaskStateEnum.NORMAL,
                Instant.now().plus(-1, ChronoUnit.HOURS));
            if (taskIds.isEmpty()) {
                // 始终触发清理异常任务
                taskIds.add(0L);
            }
            log.debug("过期任务:" + taskIds);
            taskIds.stream().forEach(
                taskId -> inner.taskStateService.stopOrExpireSpider(taskId, TaskStateEnum.EXPIRE)
            );
        }), 210, 300, TimeUnit.SECONDS);
    }

    @Component
    @RequiredArgsConstructor
    public static class InnerBean {
        private final SpiderTaskDao spiderTaskDao;
        private final TaskDao taskDao;
        private final TaskStateService taskStateService;
        private final SpiderDispatcherService spiderDispatcherService;
    }
}
