package com.zsh.spider.util.similar;

import info.debatty.java.stringsimilarity.NGram;

/**
 * Kondrak 定义的归一化 N 元语法距离
 */
public class NGram2Similar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        NGram twogram = new NGram(2);
        return twogram.distance(word1, word2);
    }

}
