package com.zsh.spider.service;

import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.task.BordPriorityBlockingQueue;
import com.zsh.spider.task.PriorityTask;
import com.zsh.spider.task.PriorityTaskRejectedExecutionHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 优先级队列线程池
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SpiderDispatcherService implements ApplicationListener<ContextClosedEvent> {

    private final PriorityTask.InnerBean innerBean;
    private final PriorityTaskRejectedExecutionHandler.InnerBean innerBean3;

    @Autowired
    private BordPriorityBlockingQueue.InnerBean innerBean2;

    private ThreadPoolExecutor executor;
    private BordPriorityBlockingQueue<Runnable> priorityQueue;

    /**
     * 初始化线程池
     */
    @PostConstruct
    void initExecutor() {
        this.priorityQueue =
            new BordPriorityBlockingQueue<>(50000L, innerBean2);
        var threadNum = Runtime.getRuntime().availableProcessors();
        // 创建线程池, 使用优先级队列作为任务队列, 爬去内容字节长度比较大, 线程数过大会导致使用太多内存
        // 线程池只有在队列已满情况下才会尝试创建达到maximumPoolSize数量
        this.executor = new ThreadPoolExecutor(
            threadNum * 10, threadNum * 100, 1, TimeUnit.MINUTES,
            priorityQueue, new PriorityTaskRejectedExecutionHandler(innerBean3)
        );
    }

    /**
     * add 爬虫任务
     */
    public void addSpiderTask(SpiderTaskPO spiderTaskPO) {
        executor.execute(new PriorityTask(spiderTaskPO, innerBean));

    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.warn("程序中止: 触发关闭钩子");
        executor.shutdown();
        priorityQueue.saveDataToDatabase();
        log.warn("保存数据完成: 程序关闭");
    }
}
