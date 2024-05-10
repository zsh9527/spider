package com.zsh.spider.util.similar;

import org.xm.similarity.text.CosineSimilarity;

/**
 * 段落文本相似度计算 -- 段落文本 段落粒度（一段话，25字符 < length(text) < 500字符）
 * cos相似度分值
 */
public class CosineSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        CosineSimilarity cosSimilarity = new CosineSimilarity();
        return cosSimilarity.getSimilarity(word1, word2);
    }

}
