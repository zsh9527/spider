package com.zsh.spider;

import com.zsh.spider.dao.SpiderResultDao;
import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.dao.TaskDao;
import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.service.SpiderDispatcherService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * DaoTest
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/28 17:07
 */
@SpringBootTest
@Transactional
@Slf4j
@MockBean({SpiderDispatcherService.class})
public class DaoTest {

    @Autowired
    private SpiderTaskDao spiderTaskDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private SpiderResultDao spiderResultDao;

    /**
     * 查询
     *
     * 检查返回数据
     */
    @Test
    @Sql("/sql/spider_task.sql")
    public void testFindAllOrderByScoreDescLimit() {
        var list = spiderTaskDao.findAllOrderByScoreDescLimit2(5);
        Assertions.assertEquals(5, list.size());
        var ids = list.stream().map(SpiderTaskPO::getId).toList();
        Assertions.assertTrue(ids.containsAll(List.of(-2L, -4L, -7L, -9L, -10L)));
    }

    /**
     * 查询 -- 检查耗时
     * 3668 findAllOrderByScoreDescLimit2
     * 2725 findAllOrderByScoreDescLimit
     * 2725 15856  findAllOrderByScoreDescLimit3
     * 添加索引后耗时 12483
     */
    @Test
    public void testFindAllOrderByScoreDescLimitTime() {
        var startTime = System.currentTimeMillis();
        //var list = spiderTaskDao.findAllOrderByScoreDescLimit(2000);
        //var list = spiderTaskDao.findAllOrderByScoreDescLimit2(2000);
        //spiderTaskDao.findAllOrderByScoreDescLimit3(10000);
        var endTime = System.currentTimeMillis();
        log.info("耗时：" + (endTime - startTime));
    }

    /**
     * 按照分数删除任务
     *
     * 检查返回数据
     */
    @Test
    @Sql("/sql/spider_task.sql")
    public void testDeleteByScoreLtOrderByScoreDescLimit() {
        var changeCount = spiderTaskDao.deleteByScoreLtOrderByScoreDescLimit(0.0, 2);
        Assertions.assertEquals(2, changeCount);
        Assertions.assertFalse(spiderTaskDao.existsById(-5L));
        Assertions.assertFalse(spiderTaskDao.existsById(-6L));
    }

    /**
     * 按照分数删除任务 -- 检查花费时间
     *
     *
     * delete count:382541
     * 2024-03-30T10:20:35.816+08:00  INFO 57224 --- [    Test worker] com.zsh.spider.DaoTest                   : 花费时间:10657
     */
    @Test
    public void testDeleteByScoreLtOrderByScoreDescLimitTime1() {
        long startTime = System.currentTimeMillis();
        var changeCount = spiderTaskDao.deleteByScoreLtOrderByScoreDescLimit(0.1, 500000);
        long endTime = System.currentTimeMillis();
        log.info("delete count:" + changeCount);
        log.info("花费时间:" + (endTime - startTime));
    }

    /**
     * 按照分数删除任务
     *
     * 检查花费时间2
     删除关联度低任务数量:100000
     2024-03-30T10:22:50.631+08:00  INFO 34576 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:100000
     2024-03-30T10:22:53.724+08:00  INFO 34576 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:100000
     2024-03-30T10:22:56.352+08:00  INFO 34576 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:82541
     2024-03-30T10:22:56.352+08:00  INFO 34576 --- [    Test worker] com.zsh.spider.DaoTest                   : 花费时间:13618
     */
    @Test
    public void testDeleteByScoreLtOrderByScoreDescLimitTime2() {
        int maxDeleteCount = 500000;
        long startTime = System.currentTimeMillis();
        while (maxDeleteCount > 0) {
            // 批次删除防止数据太大
            long oneBatchSize = Math.min(100000, maxDeleteCount);
            maxDeleteCount -= oneBatchSize;
            var changeCount = spiderTaskDao.deleteByScoreLtOrderByScoreDescLimit(0.1,
                oneBatchSize);
            log.info("删除关联度低任务数量:" + changeCount);
            if (changeCount < oneBatchSize) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("花费时间:" + (endTime - startTime));
    }

    /**
     * 按照分数删除任务 -- 检查花费时间
     *
     *
     * delete count:382541
     * 2024-03-30T10:25:18.761+08:00  INFO 94116 --- [    Test worker] com.zsh.spider.DaoTest                   : 花费时间:7667
     */
    @Test
    public void testDeleteByScoreLtOrderByScoreDescLimitTime3() {
        long startTime = System.currentTimeMillis();
        var ids = spiderTaskDao.listIdByScoreLtOrderByScoreDescLimit(0.1, 500000);
        spiderTaskDao.deleteAllById(ids);
        long endTime = System.currentTimeMillis();
        log.info("delete count:" + ids.size());
        log.info("花费时间:" + (endTime - startTime));
    }

    /**
     * 按照分数删除任务
     *
     * 检查花费时间4
     删除关联度低任务数量:100000
     2024-03-30T10:27:01.888+08:00  INFO 32056 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:100000
     2024-03-30T10:27:05.056+08:00  INFO 32056 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:100000
     2024-03-30T10:27:07.757+08:00  INFO 32056 --- [    Test worker] com.zsh.spider.DaoTest                   : 删除关联度低任务数量:82541
     2024-03-30T10:27:07.758+08:00  INFO 32056 --- [    Test worker] com.zsh.spider.DaoTest                   : 花费时间:15439
     */
    @Test
    public void testDeleteByScoreLtOrderByScoreDescLimitTime4() {
        int maxDeleteCount = 500000;
        long startTime = System.currentTimeMillis();
        while (maxDeleteCount > 0) {
            // 批次删除防止数据太大
            long oneBatchSize = Math.min(100000, maxDeleteCount);
            maxDeleteCount -= oneBatchSize;
            var ids = spiderTaskDao.listIdByScoreLtOrderByScoreDescLimit(0.1, oneBatchSize);
            spiderTaskDao.deleteAllById(ids);
            log.info("删除关联度低任务数量:" + ids.size());
            if (ids.size() < oneBatchSize) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("花费时间:" + (endTime - startTime));
    }

    /**
     * 删除已中止或过期任务
     *
     * 检查返回数据
     */
    @Test
    @Sql({"/sql/spider_task.sql", "/sql/task.sql"})
    public void testDeleteAllNotNormalTask() {
        var changeCount = spiderTaskDao.deleteAllNotNormalTask();
        Assertions.assertEquals(3, changeCount);
    }

    /**
     * 更新任务状态
     */
    @Test
    @Sql({"/sql/task.sql"})
    public void updateStateById() {
        var changeCount = taskDao.updateStateById(TaskStateEnum.STOP, -11L);
        Assertions.assertEquals(1, changeCount);
        var po = taskDao.findById(-11L).get();
        Assertions.assertEquals(TaskStateEnum.STOP, po.getState());
    }

    /**
     * 查询即将过期任务
     */
    @Test
    @Sql({"/sql/task.sql"})
    public void findIdToBeExpireTask() {
        var list = taskDao.findIdToBeExpireTask(TaskStateEnum.NORMAL, Instant.now());
        Assertions.assertTrue(list.contains(-11L));
    }

    /**
     * 查询即将过期任务
     */
    @Test
    public void findAllByTaskId() {
        var pageData = spiderResultDao.findAllByTaskId(13L, 0.01,
            null, "深圳", PageRequest.of(0, 10));
        Assertions.assertTrue(pageData.getContent().size() >= 0);
    }
}
