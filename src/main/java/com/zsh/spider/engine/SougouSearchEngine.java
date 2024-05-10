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
 * 搜狗搜索引擎
 */
@Component
@RequiredArgsConstructor
public class SougouSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 搜索 -- 每页数量为1, 分页参数为page 1, 2 ,3
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://www.sogou.com/web?" + buildParam(params);
        String content = spiderClient.getContent(url, "");
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements contentList = document.getElementsByAttributeValue("class", "results");
        if (contentList.isEmpty()) {
            return Pair.of(searchResult, true);
        }
        Elements result = contentList.get(0).children();
        result.forEach(
            obj -> {
                Elements linkElements = obj.getElementsByTag("a");
                // 加入第一个href
                if (!linkElements.isEmpty()) {
                    SearchEngineResultDTO dto = new SearchEngineResultDTO();
                    dto.setUrl(linkElements.first().attr("href"));
                    if (!dto.getUrl().startsWith("http")) {
                        dto.setUrl("https://www.sogou.com" + dto.getUrl());
                    }
                    dto.setTitle(linkElements.text());
                    dto.setContent(obj.text());
                    searchResult.add(dto);
                }
            }
        );
        // 超出数量分页360查询始终返回最后一页
        Element pageElement = document.getElementById("pagebar_container");
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
        map.put("page", String.valueOf(Integer.parseInt(map.get("page")) + 1));
    }

    /**
     * 获取查询参数
     * sougou不支持no keyword
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        // 搜索关键字编码
        params.put("ie", "utf-8");
        // 所有地方匹配
        params.put("located", "0");
        // tro参数off代表按照相关性排序, on代表按照时间排序
        params.put("tro", "off");
        StringBuilder searchParam = new StringBuilder();
        if (searchDTO.getKeyWord() != null) {
            searchParam.append(searchDTO.getKeyWord() + " ");
            params.put("q", searchDTO.getKeyWord());
        }
        if (searchDTO.getAllKeyWord() != null) {
            searchParam.append("\"" + searchDTO.getAllKeyWord() + "\"" + " ");
            params.put("q", searchDTO.getAllKeyWord());
        } else {
            // 拆分关键字
            params.put("include", "checkbox");
        }
        if (searchDTO.getAnyKeyWord() != null) {
            searchParam.append(searchDTO.getNoKeyWord() + " ");
            params.put("q", searchDTO.getAnyKeyWord());
        }
        if (searchDTO.getFileType() != null) {
            searchParam.append("filetype:" + searchDTO.getFileType().getName() + " ");
            params.put("filetype", searchDTO.getFileType().getName());
        }
        if (searchDTO.getLimitSite() != null) {
            searchParam.append("site:" + searchDTO.getLimitSite() + " ");
            params.put("sitequery", searchDTO.getLimitSite());
        }
        if (searchParam.length() > 0 && searchParam.charAt(searchParam.length() - 1) == ' ') {
            // 删除最后一个字符
            searchParam.deleteCharAt(searchParam.length() - 1);
            params.put("query", searchParam.toString());
        }
        if (searchDTO.getLimitTime() != null) {
            params.put("sourceid", searchDTO.getLimitTime().getSougouParam());
            params.put("tsn", String.valueOf(searchDTO.getLimitTime().ordinal() + 1));
        }
        return params;
    }

}
