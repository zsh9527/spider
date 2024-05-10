package com.zsh.spider.config;

/**
 * SpiderConstants
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/04 10:10
 */
public class SpiderConstants {

    /**
     * 搜索引擎最大查询数量
     */
    public static final Integer MAX_SEARCH_SIZE = 20;

    /**
     * 高负载限制分数
     */
    public static final Double HIGH_LOAD_BORD_SCORE = 0.1;

    /**
     * 丢弃分数, 数据不保存
     */
    public static final Double DISCARD_SCORE = 0.00001;

    /**
     * 关联链接分数
     */
    public static final Double RELATED_LINK_SCORE = 0.00001;
}
