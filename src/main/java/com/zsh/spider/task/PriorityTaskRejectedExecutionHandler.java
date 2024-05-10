package com.zsh.spider.task;

import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.service.TaskStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 优先级任务拒绝策略
 */
@Slf4j
public class PriorityTaskRejectedExecutionHandler implements RejectedExecutionHandler {

    private final InnerBean inner;

    public PriorityTaskRejectedExecutionHandler(InnerBean inner) {
        this.inner = inner;
    }

    /**
     * 任务过多保存到数据库中
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        PriorityTask task = (PriorityTask) r;
        SpiderTaskPO po = task.getSpiderTaskPO();
        log.debug("任务队列已满拒绝任务：" + po.getScore());
        // 保存到数据库中
        if (po.getSpider().getScore() > inner.taskStateService.getLimitScore()) {
            po.setId(null);
            inner.spiderTaskDao.save(po);
            log.info("拒绝任务保存到数据库：" + po.getId());
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class InnerBean {
        private final SpiderTaskDao spiderTaskDao;
        private final TaskStateService taskStateService;
    }
}
