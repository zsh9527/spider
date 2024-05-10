package com.zsh.spider.util;

import com.zsh.spider.config.SpiderConstants;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.util.similar.CosineSimilar;
import com.zsh.spider.util.similar.PhraseSimilar;
import info.debatty.java.stringsimilarity.Cosine;

import java.util.*;

/**
 * 相似度工具类
 */
public class SimilarUtils {

    public static PhraseSimilar keySimilar = new PhraseSimilar();
    public static PhraseSimilar anyKeySimilar = new PhraseSimilar();
    public static PhraseSimilar allKeySimilar = new PhraseSimilar();
    public static PhraseSimilar relatedSimilar = new PhraseSimilar();
    public static Cosine cosine = new Cosine(2);

    // 多个关键字all_key权重
    public static double total_weight = 1.0;
    public static double many_all_key_weight = 0.6;
    public static double no_key_weight = -0.3;

    /**
     * 获取内容相似度分数
     */
    public static double getContentSimilarScore(String content, SearchDTO search) {
        double finalScore = 0;
        double remainWeight = total_weight;
        if (search.getAllKeyWord() != null) {
            // 不加双引号匹配程度更好
            double allKeyScore = allKeySimilar.getSimilarScoreIgnoreException(content, search.getAllKeyWord());
            if (search.getKeyWord() != null || search.getAnyKeyWord() != null) {
                // 还有其他匹配条件 完全匹配权重占60%
                finalScore += many_all_key_weight * allKeyScore;
                remainWeight = remainWeight - many_all_key_weight;
            } else {
                finalScore += allKeyScore;
                remainWeight = 0;
            }
        }
        if (search.getKeyWord() != null) {
            double keyScore = keySimilar.getSimilarScoreIgnoreException(content, search.getKeyWord());
            finalScore += remainWeight * keyScore;
        }
        if (search.getAnyKeyWord() != null) {
            double anyKeyScore = anyKeySimilar.getSimilarScoreIgnoreException(content, search.getAnyKeyWord());
            finalScore += remainWeight * anyKeyScore;
        }
        if (search.getNoKeyWord() != null) {
            double noKeyScore = anyKeySimilar.getSimilarScoreIgnoreException(content, search.getNoKeyWord());
            finalScore += noKeyScore * no_key_weight;
        }
        return Math.max(finalScore, 0.0);
    }

    /**
     * 是否关联链接
     */
    public static boolean isRelatedLink(String title, SearchDTO search) {
        if (search.getAllKeyWord() != null &&
            relatedSimilar.getSimilarScoreIgnoreException(title, search.getAllKeyWord()) >= SpiderConstants.RELATED_LINK_SCORE) {
            return true;
        }
        if (search.getKeyWord() != null &&
            relatedSimilar.getSimilarScoreIgnoreException(title, search.getKeyWord()) >= SpiderConstants.RELATED_LINK_SCORE) {
            return true;
        }
        if (search.getAnyKeyWord() != null &&
            relatedSimilar.getSimilarScoreIgnoreException(title, search.getAnyKeyWord()) >= SpiderConstants.RELATED_LINK_SCORE) {
            return true;
        }
        return false;
    }

    /**
     * 获取相似的字符串
     */
    public static List<String> getSimilarContent(String text, SearchDTO search) {
        // 将搜索词分为2
        Set<String> searchContent = new HashSet<>();
        if (search.getAllKeyWord() != null) {
            searchContent.addAll(
                cosine.getProfile(search.getAllKeyWord()).keySet());
        }
        if (search.getKeyWord() != null) {
            for (String str : search.getKeyWord().split(" ")) {
                searchContent.addAll(cosine.getProfile(str).keySet());
            }
        }
        if (search.getAnyKeyWord() != null) {
            for (String str : search.getAnyKeyWord().split(" ")) {
                searchContent.addAll(cosine.getProfile(str).keySet());
            }
        }
        // 获取所有index
        TreeSet<Integer> sortedIndex = new TreeSet<>();
        searchContent.forEach(keyword -> sortedIndex.addAll(findKeywordIndices(text, keyword)));
        Iterator<Integer> iterator = sortedIndex.iterator();
        int before = -1;
        int start = -1;
        int length = text.length() - 1;
        Set<String> result = new HashSet<>();
        while (iterator.hasNext()) {
            int next = iterator.next();
            if (start == -1) {
                // 无开始位置
                start = next;
            } else {
                // 判断是否结束
                if (next > before + 10) {
                    // 关联部分位置相差太远, 截断上部分位置
                    if (result.isEmpty() || start != before) {
                        // 只有一个关键字的数据仅仅非空才加入
                        result.add(text.substring(Math.max(0, start - 5), Math.min(before + 5, length)));
                    }
                    start = -1;
                }
            }
            before = next;
        }
        if (start != -1 && (result.isEmpty() || start != before)) {
            result.add(text.substring(Math.max(0, start - 5), Math.min(before + 5, length)));
        }
        return result.stream().toList();
    }

    public static List<Integer> findKeywordIndices(String text, String keyword) {
        List<Integer> indices = new ArrayList<>();
        int index = text.indexOf(keyword);
        while (index != -1) {
            indices.add(index);
            index = text.indexOf(keyword, index + 1);
        }
        return indices;
    }
}
