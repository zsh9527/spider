package com.zsh.spider.engine;

import com.zsh.spider.client.SpiderClient;
import com.zsh.spider.config.CookieConstants;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 雅虎搜索引擎
 */
@Component
@RequiredArgsConstructor
public class YahooSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    private static final Integer PAGE_SIZE = 7;

    /**
     * 搜索 - 未找到分页参数 b 开始索引 8 15 22
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://search.yahoo.com/search?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.BING_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Element webList = document.getElementById("web");
        if (webList == null) {
            return Pair.of(searchResult, true);
        }
        Elements result = webList.select("ol > li");
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
        map.put("b", String.valueOf(Integer.parseInt(map.get("b")) + PAGE_SIZE));
    }

    /**
     * 获取查询参数
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("b", "1");
        // 固定每页数量
        params.put("pz", String.valueOf(PAGE_SIZE));
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
            params.put("p", searchParam.toString());
        }
        if (searchDTO.getLimitTime() != null) {
            params.put("fr2", "time");
            params.put("btf", searchDTO.getLimitTime().get360Param());
        }
        return params;
    }

}
