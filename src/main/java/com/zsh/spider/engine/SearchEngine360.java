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
 * 360搜索引擎
 */
@Component
@RequiredArgsConstructor
public class SearchEngine360 implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 搜索 -- 每页数量为1, 分页参数为pn 1, 2 ,3
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://www.so.com/s?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.SO_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements result = document.getElementsByAttributeValue("class", "res-list");
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
        // 超出数量分页360查询始终返回最后一页
        Element pageElement = document.getElementById("page");
        if (pageElement == null) {
            return Pair.of(searchResult, true);
        }
        var nextPage = pageElement.getElementsContainingText("下一页");
        if (nextPage.isEmpty()) {
            return Pair.of(searchResult, true);
        }
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
     * 360不支持no keyword
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        // 搜索关键字编码
        params.put("ie", "utf-8");
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
            params.put("adv_t", searchDTO.getLimitTime().get360Param());
        }
        return params;
    }

}
