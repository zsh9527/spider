package com.zsh.spider.util.similar;

import lombok.extern.slf4j.Slf4j;

/**
 * 相似度工具类
 */
@Slf4j
public abstract class Similar {

    /**
     * 获取内容相似度分数
     */
    public double getSimilarScoreIgnoreException(String word1, String word2) {
        try {
            double score = getSimilarScore(word1, word2);
            if (Double.isNaN(score)) {
                return 0.0;
            }
            return score;
        } catch (Exception e) {
            log.warn(getClass().getSimpleName() + "解析报错");
            return 0.0;
        }
    }

    /**
     * 获取内容相似度分数
     */
    public abstract double getSimilarScore(String word1, String word2);
}
