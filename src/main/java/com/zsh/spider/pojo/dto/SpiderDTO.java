package com.zsh.spider.pojo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 爬取 DTO
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SpiderDTO implements Comparable<SpiderDTO> {

    /**
     * 爬取URL
     */
    private String url;

    /**
     * 爬取层级
     */
    private int level;

    /**
     * 关联系数 -- 由父级内容关联分数和层级计算
     */
    private double score;

    /**
     * 父级url
     */
    private String parentUrl;

    /**
     * 来源引擎名称
     */
    private String sourceEngineName;

    /**
     * 从父级构建
     */
    public SpiderDTO(String url, SpiderDTO parent) {
        this.url = url;
        this.sourceEngineName = parent.getSourceEngineName();
        this.level = parent.getLevel() + 1;
        this.parentUrl = parent.getUrl();
        // 每爬取一层分数衰减10%, 最低到10%
        this.score = parent.getScore() * Math.max(1 - 0.1 * level, 0.1);
    }

    /**
     * 第一级构造器
     * 0-9 分数为10
     * 10-20 分数9
     * 100以上分数固定为1
     */
    public SpiderDTO(String url, Integer index, String sourceEngineName) {
        this.url = url;
        this.sourceEngineName = sourceEngineName;
        this.level = 0;
        // index每10个分数衰减10%
        BigDecimal decrease = BigDecimal.valueOf(Math.min(index / 10, 9));
        this.score = BigDecimal.TEN.subtract(decrease)
            .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public int compareTo(SpiderDTO obj) {
        if (score - obj.score > 0) {
            return 1;
        }
        return -1;
    }
}
