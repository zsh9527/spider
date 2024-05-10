package com.zsh.spider.util.similar;

import org.xm.similarity.text.EditDistanceSimilarity;
import org.xm.similarity.text.TextSimilarity;

/**
 * 段落文本相似度计算 -- 段落文本 段落粒度（一段话，25字符 < length(text) < 500字符）
 * edit相似度
 */
public class EditSimilar extends Similar {


    /**
     * 获取内容相似度分数
     */
    public double getSimilarScore(String word1, String word2) {
        TextSimilarity editSimilarity = new EditDistanceSimilarity();
        return editSimilarity.getSimilarity(word1, word2);
    }

}
