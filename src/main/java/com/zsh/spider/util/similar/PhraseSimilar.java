package com.zsh.spider.util.similar;

import org.xm.Similarity;

/**
 * 短语相似度 -- 用于短语
 */
public class PhraseSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        return Similarity.phraseSimilarity(word1, word2);
    }

}
