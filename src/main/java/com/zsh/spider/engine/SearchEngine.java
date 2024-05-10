package com.zsh.spider.engine;

import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 搜索引擎
 */
public interface SearchEngine {

    /**
     * 搜索
     */
    @SneakyThrows
    default void search(SearchDTO searchDTO, BlockingQueue<SearchEngineResultDTO> queue) {
        var params = this.getSearchParam(searchDTO);
        int searchIndex = 0;
        while (searchIndex < searchDTO.getMaxSearchSize()) {
            Pair<List<SearchEngineResultDTO>, Boolean> resp = searchOnePage(searchDTO, params);
            for (SearchEngineResultDTO obj : resp.getKey()) {
                obj.setSourceEngineName(getEngineName());
                obj.setIndex(searchIndex++);
            }
            queue.addAll(resp.getKey());
            int size = resp.getKey().size();
            // 查询返回数量小于5, 默认认定没有下一页
            if (size < 5 || resp.getValue()) {
                // 查询完成
                break;
            }
            // 翻页
            nextPage(params, size);
        }
    }

    /**
     * 搜索
     *
     * @return boolean用于判断是否查询结束
     */
    Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params);

    /**
     * 获取查询参数
     */
    Map<String, String> getSearchParam(SearchDTO searchDTO);

    /**
     * 翻页
     *
     * @param size 上一次返回结果数量
     */
    void nextPage(Map<String, String> map, int size);

    default String buildParam(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.entrySet().stream().forEach(entry -> {
            try {
                sb.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8") + "&");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
            // 删除最后一个字符
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    default String getEngineName() {
        return this.getClass().getSimpleName().replace("SearchEngine", "");
    }
}
