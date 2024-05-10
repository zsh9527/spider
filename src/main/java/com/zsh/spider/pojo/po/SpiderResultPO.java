package com.zsh.spider.pojo.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * 爬虫结果
 * 任务id + url 为唯一索引
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "spider_result")
public class SpiderResultPO {
    /**
     * 自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

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
     * 内容关联分数
     */
    private Double similarScore;

    /**
     * 匹配的部分内容
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> matchContent = Collections.emptyList();

    /**
     * 内部url
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> childUrl = Collections.emptyList();

    /**
     * 内容爬取时间
     */
    @Column(insertable = false, updatable = false)
    private Instant createDate;
}
