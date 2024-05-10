package com.zsh.spider.util.similar;

import org.xm.Similarity;

/**
 * 优化的编辑距离句子相似度 -- 用于句子
 */
public class EditDistanceSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        return Similarity.editDistanceSimilarity(word1, word2);
    }

}
