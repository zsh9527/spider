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
 * yandex搜索引擎
 */
@Component
@RequiredArgsConstructor
public class YandexSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 搜索 - 分页参数为p 0, 1, 2
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://yandex.com/search/?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.YANDEX_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements result = document.getElementsByAttribute("data-cid");
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
        // 超出数量分页查询始终返回第一页
        Elements pageElements = document.getElementsByAttributeValue("class", "Pager-Content");
        if (pageElements.isEmpty()) {
            return Pair.of(searchResult, true);
        }
        var nextPage = pageElements.first().getElementsContainingText("next");
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
        map.put("p", String.valueOf(Integer.parseInt(map.get("p")) + 1));
    }

    /**
     * 获取查询参数
     * -否定 实际查询起来不支持
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("p", "0");
        // 不确定参数
        params.put("lr", "109371");
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
        if (searchDTO.getFileType() != null) {
            searchParam.append("mime:" + searchDTO.getFileType().getName() + " ");
        }
        if (searchDTO.getLimitSite() != null) {
            searchParam.append("site:" + searchDTO.getLimitSite() + " ");
        }
        if (searchParam.length() > 0 && searchParam.charAt(searchParam.length() - 1) == ' ') {
            // 删除最后一个字符
            searchParam.deleteCharAt(searchParam.length() - 1);
            params.put("text", searchParam.toString());
        }
        if (searchDTO.getLimitTime() != null) {
            params.put("within", searchDTO.getLimitTime().getYandexParam());
        }
        if (searchDTO.isLimitChinese()) {
            // 不支持zh
            //params.put("lang", "zh");
        }
        return params;
    }

}
