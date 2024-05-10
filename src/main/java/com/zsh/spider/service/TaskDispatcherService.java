package com.zsh.spider.service;

import com.zsh.spider.dao.EngineResultDao;
import com.zsh.spider.dao.TaskDao;
import com.zsh.spider.engine.SearchEngine;
import com.zsh.spider.pojo.convertor.ResultConvertor;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import com.zsh.spider.pojo.dto.SpiderDTO;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.pojo.po.TaskPO;
import com.zsh.spider.task.CatchErrorTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

/**
 * 优先级队列线程池
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDispatcherService {

    private final List<SearchEngine> searchEngines;
    private final TaskDao taskDao;
    private final EngineResultDao engineResultDao;
    private final SpiderDispatcherService daemonSpiderService;

    public ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 开始搜索
     */
    public void startSearch(SearchDTO searchDTO) {
        TaskPO taskPO = new TaskPO(searchDTO);
        taskDao.save(taskPO);
        BlockingQueue<SearchEngineResultDTO> asyncQueue = new LinkedBlockingQueue<>();
        for (SearchEngine engine : searchEngines) {
            executorService.submit(
                new CatchErrorTask("搜索引擎", () -> engine.search(searchDTO, asyncQueue)));
        }
        executorService.submit(new CatchErrorTask("搜索数据", () -> {
            while (true) {
                // 从队列中获取元素, 最多等待5分钟
                SearchEngineResultDTO obj = null;
                try {
                    obj = asyncQueue.poll(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    // 忽略中断操作
                }
                if (obj == null) {
                    break;
                }
                var engineResultPO = ResultConvertor.INSTANCT.toEngineResultPO(obj, taskPO.getId());
                if (!engineResultDao.existsByTaskIdAndUrl(engineResultPO.getTaskId(), engineResultPO.getUrl())) {
                    engineResultDao.save(engineResultPO);
                    SpiderDTO dto = new SpiderDTO(engineResultPO.getUrl(), engineResultPO.getIndex(),
                        engineResultPO.getSourceEngineName());
                    // 搜索引擎结果直接添加任务
                    daemonSpiderService.addSpiderTask(new SpiderTaskPO(taskPO.getId(), dto));
                }
            }
        }));
    }
}
