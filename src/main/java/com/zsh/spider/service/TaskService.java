package com.zsh.spider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsh.spider.dao.EngineResultDao;
import com.zsh.spider.dao.SpiderResultDao;
import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.dao.TaskDao;
import com.zsh.spider.pojo.convertor.ResultConvertor;
import com.zsh.spider.pojo.po.EngineResultPO;
import com.zsh.spider.pojo.vo.TaskWithCountVO;
import com.zsh.spider.pojo.vo.PageListVO;
import com.zsh.spider.pojo.vo.SpiderResultVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 优先级队列线程池
 */
@Service
@Slf4j
@AllArgsConstructor
public class TaskService {

    private final TaskDao taskDao;
    private final EngineResultDao engineResultDao;
    private final SpiderResultDao spiderResultDao;
    private final SpiderTaskDao spiderTaskDao;
    private final ObjectMapper objectMapper;

    public PageListVO<TaskWithCountVO> pageTask(Integer page, Integer size) {
        var pageData = taskDao.findAllAndCount(PageRequest.of(page - 1, size,
            Sort.by(Sort.Direction.DESC, "create_date")));
        // 任务数过多关联查询非常慢
        var spiderTaskCount = spiderTaskDao.count();
        var content = pageData.getContent().stream().map(obj -> {
            try {
                return objectMapper.readValue(objectMapper.writeValueAsString(obj), TaskWithCountVO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        return new PageListVO<>(content, pageData.getTotalElements(), spiderTaskCount);
    }

    public PageListVO<EngineResultPO> pageTaskEngineResult(Long taskId, Integer page, Integer size,
                                                           String engineName, Sort sort) {
        var pageRequest = PageRequest.of(page - 1, size, sort);
        var pageData = engineResultDao.findAllByTaskId(taskId, engineName, pageRequest);
        return new PageListVO<>(pageData.getContent(), pageData.getTotalElements());
    }

    public PageListVO<SpiderResultVO> pageTaskSpiderResult(Long taskId, Double score, String engineName,
                                                           String content, PageRequest pageRequest) {
        var pageData = spiderResultDao.findAllByTaskId(taskId, score, engineName, content, pageRequest);
        var list = pageData.getContent().stream().map(obj -> {
                var vo = ResultConvertor.INSTANCT.toSpiderResultVO(obj);
                vo.setSmallContent(Jsoup.parse(obj.getContent()).text());
                vo.setMatchContent(String.join("---", obj.getMatchContent()));
                vo.setChildUrl(String.join("---", obj.getChildUrl()));
                return vo;
            }
        ).toList();
        return new PageListVO<>(list, pageData.getTotalElements());
    }
}
