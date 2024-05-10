package com.zsh.spider.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 爬虫结果
 * 任务id + url 为唯一索引
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpiderResultVO {
    /**
     * 自增
     */
    private Long id;

    /**
     * 来源引擎名称
     */
    private String sourceEngineName;

    /**
     * 标题
     */
    private String title;

    /**
     * url
     */
    private String url;

    /**
     * 父级url
     */
    private String parentUrl;

    /**
     * 页面内容
     */
    private String content;

    /**
     * 页面内容
     */
    private String smallContent;

    /**
     * 内容关联分数
     */
    private Double similarScore;

    /**
     * 匹配的部分内容
     */
    private String matchContent;

    /**
     * 内部url
     */
    private String childUrl;

    /**
     * 内容爬取时间
     */
    private Instant createDate;
}
