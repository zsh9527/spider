package com.zsh.spider.engine;

import com.zsh.spider.client.SpiderClient;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * duckduckgo 搜索引擎 保护隐私 -- 数据非普通http请求方式获取
 */
@Component
@RequiredArgsConstructor
public class DuckDuckGoSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 搜索
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        var url = "https://duckduckgo.com/?" + buildParam(params);
        return Pair.of(Collections.emptyList(), true);
    }

    /**
     * 翻页
     */
    @Override
    public void nextPage(Map<String, String> map, int size) {

    }

    /**
     * 获取查询参数
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        // 固定每页数量
        params.put("t", "h_");
        params.put("ia", "web");
        StringBuilder searchParam = new StringBuilder();
        if (searchDTO.getKeyWord() != null) {
            searchParam.append(searchDTO.getKeyWord() + " ");
        }
        if (searchDTO.getAllKeyWord() != null) {
            searchParam.append("\"" + searchDTO.getAllKeyWord() + "\"" + " ");
        }
        if (searchDTO.getAnyKeyWord() != null) {
            searchParam.append(searchDTO.getAnyKeyWord() + " ");
        }
        if (searchDTO.getNoKeyWord() != null) {
            searchParam.append("-" + searchDTO.getNoKeyWord() + " ");
        }
        if (searchDTO.getFileType() != null) {
            searchParam.append("filetype:" + searchDTO.getFileType().getName() + " ");
        }
        if (searchDTO.getLimitSite() != null) {
            searchParam.append("site:" + searchDTO.getLimitSite() + " ");
        }
        if (searchParam.length() > 0 && searchParam.charAt(searchParam.length() - 1) == ' ') {
            // 删除最后一个字符
            searchParam.deleteCharAt(searchParam.length() - 1);
            params.put("q", searchParam.toString());
        }
        if (searchDTO.getLimitTime() != null) {
            params.put("df", searchDTO.getLimitTime().get360Param());
        }
        return params;
    }

}
