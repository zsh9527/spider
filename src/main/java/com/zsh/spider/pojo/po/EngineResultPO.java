package com.zsh.spider.pojo.po;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 搜索引擎结果
 *
 * 任务id + url 为唯一索引
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "engine_result")
public class EngineResultPO {
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
     * 查询结果index
     */
    private Integer index;

    /**
     * 来源引擎名称
     */
    private String sourceEngineName;

    /**
     * url
     */
    private String url;

    /**
     * 标题
     */
    private String title;

    /**
     * 匹配的关联内容
     */
    private String content;

    /**
     * 内容更新时间
     */
    private Instant uploadTime;
}
