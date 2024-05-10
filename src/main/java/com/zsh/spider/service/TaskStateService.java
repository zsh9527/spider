package com.zsh.spider.service;

import com.zsh.spider.config.SpiderConstants;
import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.dao.TaskDao;
import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.po.TaskPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务状态
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskStateService {

    private final TaskDao taskDao;
    private final SpiderTaskDao spiderTaskDao;

    // 限制分数, 分数过低任务抛弃
    private volatile Double limitScore = SpiderConstants.DISCARD_SCORE;

    /**
     * 从缓存中获取task
     */
    @Cacheable(cacheNames = "task", key = "#taskId")
    public TaskPO getTask(Long taskId) {
        return taskDao.findById(taskId).get();
    }

    public void setLimitScore(double limitScore) {
        this.limitScore = limitScore;
    }

    public Double getLimitScore() {
        return this.limitScore;
    }

    /**
     * 中止或过期任务
     */
    @CacheEvict(cacheNames = "task", key = "#taskId")
    @Transactional
    public void stopOrExpireSpider(Long taskId, TaskStateEnum state) {
        taskDao.updateStateById(state, taskId);
        // 清理所有过期和异常任务
        // TODO 此处清理待优化
        var changeCount = spiderTaskDao.deleteAllNotNormalTask();
        log.info("清理过期和异常任务数量：" + changeCount);
    }
}
