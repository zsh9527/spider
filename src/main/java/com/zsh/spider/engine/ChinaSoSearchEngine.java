package com.zsh.spider.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsh.spider.client.SpiderClient;
import com.zsh.spider.pojo.dto.ChinaSoDTO;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中国搜索搜索引擎
 */
@Component
@RequiredArgsConstructor
public class ChinaSoSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;
    private final ObjectMapper objectMapper;

    private static final Integer PAGE_SIZE = 50;

    /**
     * ps 为 PAGE_SIZE
     * 搜索 -- 每页数量为50, 分页参数为pn 1, 2 ,3
     */
    @Override
    @SneakyThrows
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://www.chinaso.com/v5/general/v1/web/search?" + buildParam(params);
        String content = spiderClient.getContent(url, "");
        var resp = objectMapper.readValue(content, ChinaSoDTO.class);
        var result = resp.getData().getData();
        result.stream()
            .map(obj -> new SearchEngineResultDTO(obj.getUrl(), obj.getTitle(),
                obj.getSnippet(), Instant.ofEpochSecond(obj.getTimestamp()))
            )
            .forEach(searchResult::add);
        return Pair.of(searchResult, false);
    }

    /**
     * 翻页
     */
    @Override
    public void nextPage(Map<String, String> map, int size) {
        map.put("pn", String.valueOf(Integer.parseInt(map.get("pn")) + 1));
    }

    /**
     * 获取查询参数
     * 不支持no keyword 不支持filetype
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        params.put("ps", PAGE_SIZE.toString());
        params.put("force", "0");
        // order time 参数代表按照时间排序, 默认按照相关性排序
        // params.put("order", "time");
        StringBuilder searchParam = new StringBuilder();
        if (searchDTO.getKeyWord() != null) {
            searchParam.append(searchDTO.getKeyWord() + " ");
        }
        if (searchDTO.getAllKeyWord() != null) {
            searchParam.append("\"" + searchDTO.getAllKeyWord() + "\"" + " ");
        }
        if (searchDTO.getAnyKeyWord() != null) {
            searchParam.append(searchDTO.getNoKeyWord() + " ");
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
            params.put("stime", searchDTO.getLimitTime().getChinaSoParam());
            params.put("etime", "now");
        }
        return params;
    }

}
