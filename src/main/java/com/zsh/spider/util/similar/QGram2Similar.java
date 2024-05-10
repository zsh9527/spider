package com.zsh.spider.util.similar;

import info.debatty.java.stringsimilarity.QGram;

/**
 * Kondrak 定义的归一化 N 元语法距离
 */
public class QGram2Similar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        QGram dig = new QGram(2);
        return dig.distance(word1, word2);
    }

}
