package com.zsh.spider.controller;

import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.po.EngineResultPO;
import com.zsh.spider.pojo.vo.TaskWithCountVO;
import com.zsh.spider.pojo.vo.PageListVO;
import com.zsh.spider.pojo.vo.SpiderResultVO;
import com.zsh.spider.service.TaskDispatcherService;
import com.zsh.spider.service.TaskService;
import com.zsh.spider.service.TaskStateService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * @author zsh
 * @version 1.0.0
 * @date 2022/2/21 10:41
 */
@RestController
@AllArgsConstructor
@CrossOrigin
public class TaskController {

    private final TaskService taskService;
    private final TaskStateService taskStateService;
    private final TaskDispatcherService taskDispatcherService;

    /**
     * 增加任务
     */
    @PostMapping("v2/task")
    public void addTask(
        @RequestBody SearchDTO req
    ) {
        taskDispatcherService.startSearch(req);
    }

    /**
     * 中止任务
     */
    @DeleteMapping("v2/task/{taskId}")
    public void stopTask(
        @PathVariable Long taskId
    ) {
        taskStateService.stopOrExpireSpider(taskId, TaskStateEnum.STOP);
    }

    /**
     * 任务列表
     */
    @GetMapping("v2/task")
    public PageListVO<TaskWithCountVO> pageTask(
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        return taskService.pageTask(page, size);
    }

    /**
     * 任务搜索引擎结果
     */
    @GetMapping("v2/task/engine")
    public PageListVO<EngineResultPO> pageTaskEngineResult(
        @RequestParam("task_id") Long taskId,
        @RequestParam(value = "engine_name", required = false) String engineName,
        @RequestParam(value = "order_by", defaultValue = "index") String orderBy,
        @RequestParam(value = "order_direction", defaultValue = "ASC") Sort.Direction orderDirection,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        Sort sort = Sort.by(orderDirection, com.zsh.spider.util.StringUtils.underlineToCamel(orderBy));
        return taskService.pageTaskEngineResult(taskId, page, size, engineName, sort);
    }

    /**
     * 任务搜索爬虫结果
     */
    @GetMapping("v2/task/spider")
    public PageListVO<SpiderResultVO> pageTaskSpiderResult(
        @RequestParam("task_id") Long taskId,
        @RequestParam(value = "score", defaultValue = "0") Double score,
        @RequestParam(value = "engine_name", required = false) String engineName,
        @RequestParam(required = false) String content,
        @RequestParam(value = "order_by", defaultValue = "similarScore") String orderBy,
        @RequestParam(value = "order_direction", defaultValue = "DESC") Sort.Direction orderDirection,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        Sort sort = Sort.by(orderDirection, com.zsh.spider.util.StringUtils.underlineToCamel(orderBy));
        var pageRequest = PageRequest.of(page - 1, size, sort);
        return taskService.pageTaskSpiderResult(taskId, score, engineName, content, pageRequest);
    }
}
