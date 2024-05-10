package com.zsh.spider.task;

import com.zsh.spider.client.SpiderClient;
import com.zsh.spider.dao.SpiderResultDao;
import com.zsh.spider.dao.SpiderTaskDao;
import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.convertor.ResultConvertor;
import com.zsh.spider.pojo.dto.SpiderDTO;
import com.zsh.spider.pojo.po.SpiderResultPO;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.pojo.po.TaskPO;
import com.zsh.spider.service.TaskStateService;
import com.zsh.spider.util.HttpUtils;
import com.zsh.spider.util.SimilarUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 带优先级任务
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/02/29 14:50
 */
@Slf4j
public class PriorityTask implements Comparable<PriorityTask>, Runnable {

    private final SpiderTaskPO spiderTaskPO;
    private final InnerBean inner;

    public SpiderTaskPO getSpiderTaskPO() {
        return spiderTaskPO;
    }

    public PriorityTask(SpiderTaskPO spiderTaskPO, InnerBean inner) {
        this.spiderTaskPO = spiderTaskPO;
        this.inner = inner;
    }

    @Override
    public void run() {
        try {
            this.runNoCatch();
        } catch (Exception e) {
            var message = e.getMessage();
            if (StringUtils.isEmpty(message)) {
                message = e.getCause().getMessage();
            }
            log.trace("爬虫任务执行报错：" + message, e);
            //log.info("爬虫任务执行报错：" + message);
        }
    }

    public void runNoCatch() {
        var dto = spiderTaskPO.getSpider();
        var taskId = spiderTaskPO.getTaskId();
        TaskPO task = inner.taskStateService.getTask(taskId);
        if (task.getState() != TaskStateEnum.NORMAL) {
            log.trace("任务已过期或者中止");
        }
        while (true) {
            log.trace("执行任务关联分数" + dto.getScore());
            // 爬取过数据不再爬取
            if (!inner.spiderResultDao.existsByTaskIdAndUrl(taskId, dto.getUrl())) {
                String content = inner.spiderClient.getSpiderContent(dto);
                Document document = Jsoup.parse(content);
                var redirectUrl = HttpUtils.parseRedirectUrl(document);
                if (redirectUrl != null) {
                    dto.setUrl(redirectUrl);
                    continue;
                }
                // 关联则保存数据
                var po = buildSpiderResult(dto, taskId, document, task);
                try {
                    if (task.getSearchParam().getIgnoreSpiderSite().stream()
                        .noneMatch(ignoreSite -> po.getUrl().contains(ignoreSite) &&
                            po.getUrl().indexOf(ignoreSite) < 20 + ignoreSite.length())) {
                        inner.spiderResultDao.save(po);
                    }
                    if (po.getSimilarScore() < inner.taskStateService.getLimitScore()) {
                        log.trace("关联分数太小, 丢弃:" + po.getSimilarScore());
                        break;
                    }
                    dto.setScore(po.getSimilarScore());
                    if (task.getSearchParam().isDeepSearch()) {
                        addChildTask(po.getChildUrl(), dto, taskId);
                    }
                } catch (DataIntegrityViolationException e) {
                    log.trace("重复插入");
                }
            }
            break;
        }
    }

    /**
     * 构建爬虫结果
     */
    private SpiderResultPO buildSpiderResult(SpiderDTO dto, Long taskId, Document document, TaskPO task) {
        SpiderResultPO po = ResultConvertor.INSTANCT.toSpiderResultPO(dto, taskId);
        String text = document.text();
        Double similarScore = SimilarUtils.getContentSimilarScore(text, task.getSearchParam());
        po.setContent(text);
        po.setSimilarScore(similarScore);
        String title = document.title();
        if (title.isEmpty()) {
            po.setTitle(po.getUrl());
        } else {
            po.setTitle(title);
        }
        List<String> childUrl;
        if (task.getSearchParam().isStrictChildUrl()) {
            childUrl = HttpUtils.getChildLinkByA(document, task.getSearchParam(), po.getUrl());
        } else {
            childUrl = HttpUtils.getChildLink(document, task.getSearchParam(), po.getUrl());
        }
        po.setChildUrl(childUrl);
        List<String> similarContent = SimilarUtils.getSimilarContent(text, task.getSearchParam());
        po.setMatchContent(similarContent);
        return po;
    }

    /**
     * 增加子任务 -- 保存在数据库中
     */
    private void addChildTask(List<String> childUrl, SpiderDTO dto, Long taskId) {
        TaskPO task2 = inner.taskStateService.getTask(taskId);
        if (task2.getState() != TaskStateEnum.NORMAL) {
            log.trace("任务已过期或者中止, 不增加子任务");
            return;
        }
        var list = childUrl.stream()
            .map(url -> new SpiderDTO(url, dto))
            .map(spider -> new SpiderTaskPO(taskId, spider))
            .toList();
        inner.spiderTaskDao.saveAll(list);
    }


    @Override
    public int compareTo(PriorityTask obj) {
        return obj.spiderTaskPO.getSpider().compareTo(this.spiderTaskPO.getSpider());
    }

    @Component
    @RequiredArgsConstructor
    public static class InnerBean {
        private final SpiderResultDao spiderResultDao;
        private final SpiderClient spiderClient;
        private final SpiderTaskDao spiderTaskDao;
        private final TaskStateService taskStateService;
    }
}
