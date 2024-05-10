package com.zsh.spider.pojo.po;

import com.zsh.spider.pojo.dto.SpiderDTO;
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
 * 爬虫任务
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "spider_task")
public class SpiderTaskPO {

    /**
     * 爬取id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 分数
     */
    private double score;

    /**
     * 爬取参数
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private SpiderDTO spider;

    public SpiderTaskPO(Long taskId, SpiderDTO spider) {
        this.taskId = taskId;
        this.score = spider.getScore();
        this.spider = spider;
    }
}
