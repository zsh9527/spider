package com.zsh.spider.engine;

import com.zsh.spider.client.SpiderClient;
import com.zsh.spider.config.CookieConstants;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * google搜索引擎
 */
@Component
@RequiredArgsConstructor
public class GoogleSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    private static final Integer PAGE_SIZE = 10;

    /**
     * 搜索 - 未找到分页参数 start 开始索引 10 20 30
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://www.google.com/search?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.GOOGLE_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements result = document.getElementsByAttribute("data-snc");
        result.forEach(
            obj -> {
                Elements linkElements = obj.getElementsByTag("a");
                // 加入第一个href
                if (!linkElements.isEmpty()) {
                    SearchEngineResultDTO dto = new SearchEngineResultDTO();
                    dto.setUrl(linkElements.first().attr("href"));
                    dto.setTitle(linkElements.text());
                    dto.setContent(obj.text());
                    searchResult.add(dto);
                }
            }
        );
        return Pair.of(searchResult, false);
    }

    /**
     * 翻页
     */
    @Override
    public void nextPage(Map<String, String> map, int size) {
        map.put("start", String.valueOf(Integer.parseInt(map.get("start")) + PAGE_SIZE));
    }

    /**
     * 获取查询参数
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("start", "0");
        // 安全搜索参数
        params.put("safe", "images");
        // 搜索内容可出现在任何位置
        params.put("as_occt", "any");
        if (searchDTO.getKeyWord() != null) {
            params.put("as_q", searchDTO.getKeyWord());
        }
        if (searchDTO.getAllKeyWord() != null) {
            params.put("as_epq", searchDTO.getAllKeyWord());
        }
        if (searchDTO.getAnyKeyWord() != null) {
            params.put("as_oq", searchDTO.getAnyKeyWord());
        }
        if (searchDTO.getNoKeyWord() != null) {
            params.put("as_eq", searchDTO.getNoKeyWord());
        }
        if (searchDTO.getFileType() != null) {
            params.put("as_filetype", searchDTO.getFileType().getName());
        }
        if (searchDTO.getLimitSite() != null) {
            params.put("as_sitesearch", searchDTO.getLimitSite());
        }
        if (searchDTO.getLimitTime() != null) {
            params.put("as_qdr", searchDTO.getLimitTime().get360Param());
        }
        if (searchDTO.isLimitChinese()) {
            params.put("lr", "lang_zh-CN");
        }
        return params;
    }

}
