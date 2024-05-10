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
 * 必应搜索引擎
 */
@Component
@RequiredArgsConstructor
public class BingSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 搜索 - 未找到分页参数 first 开始索引 20 40 60
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://cn.bing.com/search?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.BING_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements result = document.getElementsByAttribute("data-id");
        result.forEach(
            obj -> {
                Elements linkElements = obj.getElementsByTag("a");
                // 加入第一个href
                if (!linkElements.isEmpty() && linkElements.first().attr("href").startsWith("http")) {
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
        map.put("first", String.valueOf(Integer.parseInt(map.get("first")) + size));
    }

    /**
     * 获取查询参数
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("first", "0");
        // 直接搜索, 忽略搜索建议
        params.put("go", "搜索");
        // 直接发起搜索
        params.put("qs", "ds");
        // 搜索发出点, QBRE点击搜索框
        params.put("form", "QBRE");
        StringBuilder searchParam = new StringBuilder();
        if (searchDTO.getKeyWord() != null) {
            searchParam.append(searchDTO.getKeyWord() + " ");
        }
        if (searchDTO.getAllKeyWord() != null) {
            searchParam.append("\"" + searchDTO.getAllKeyWord() + "\"" + " ");
        }
        if (searchDTO.getAnyKeyWord() != null) {
            searchParam.append(searchDTO.getAnyKeyWord().replace(" ", " OR ") + " ");
        }
        if (searchDTO.getNoKeyWord() != null) {
            searchParam.append("-" +  "\"" + searchDTO.getNoKeyWord() + "\"" + " ");
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
            params.put("filters", searchDTO.getLimitTime().getBingParam());
        }
        if (searchDTO.isLimitChinese()) {
            //// 限制zh-cn简体中文 en-us 英文
            //params.put("setlang", "zh-cn");
            params.put("language", "zh");
        }
        return params;
    }

}
