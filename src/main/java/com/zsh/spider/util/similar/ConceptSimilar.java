package com.zsh.spider.util.similar;

import org.xm.Similarity;

/**
 * 概念相似度 -- 用于词语
 */
public class ConceptSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        return Similarity.conceptSimilarity(word1, word2);
    }

}
