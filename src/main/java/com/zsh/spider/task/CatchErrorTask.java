package com.zsh.spider.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 捕获异常任务
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/02/29 14:50
 */
@Slf4j
public class CatchErrorTask implements Runnable {

    private final String taskName;
    private final Runnable runnable;

    public CatchErrorTask(String taskName, Runnable runnable) {
        this.taskName = taskName;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            this.runnable.run();
        } catch (Exception e) {
            var message = e.getMessage();
            if (StringUtils.isEmpty(message)) {
                message = e.getCause().getMessage();
            }
            log.error(taskName + "任务执行报错：" + message);
        }

    }
}
