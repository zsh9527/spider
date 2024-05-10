package com.zsh.spider.util.similar;

import info.debatty.java.stringsimilarity.Cosine;

import java.util.Map;

/**
 * cos相似度分值
 */
public class Cosine2Similar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        Cosine cosine = new Cosine(2);
        Map<String, Integer> profile1 = cosine.getProfile(word1);
        Map<String, Integer> profile2 = cosine.getProfile(word2);
        return cosine.similarity(profile1, profile2);
    }

}
