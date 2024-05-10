package com.zsh.spider.util.similar;

import info.debatty.java.stringsimilarity.RatcliffObershelp;

/**
 * Ratcliff/Obershelp 模式识别
 */
public class ROSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        RatcliffObershelp ro = new RatcliffObershelp();
        return ro.similarity(word1, word2);
    }

}
