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
 * 百度搜索引擎
 */
@Component
@RequiredArgsConstructor
public class BaiduSearchEngine implements SearchEngine {

    private final SpiderClient spiderClient;

    /**
     * 每页固定数量
     * rn 每页数量 50 -- 设置为100返回结果存在问题
     * pn 开始索引 0, 50, 100
     */
    private static final Integer PAGE_SIZE = 50;

    /**
     * 搜索
     */
    @Override
    public Pair<List<SearchEngineResultDTO>, Boolean> searchOnePage(SearchDTO searchDTO, Map<String, String> params) {
        ArrayList<SearchEngineResultDTO> searchResult = new ArrayList<>();
        var url = "https://www.baidu.com/s?" + buildParam(params);
        String content = spiderClient.getContent(url, CookieConstants.BAIDU_COOKIE);
        Document document = Jsoup.parse(content);
        // 搜索结果
        Elements result = document.getElementsByAttributeValueMatching("id", "^\\d+$");
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
        map.put("pn", String.valueOf(Integer.parseInt(map.get("pn")) + PAGE_SIZE));
    }

    /**
     * 获取查询参数
     */
    @Override
    public Map<String, String> getSearchParam(SearchDTO searchDTO) {
        Map<String, String> params = new HashMap<>();
        params.put("pn", "0");
        // 搜索关键字编码
        params.put("ie", "utf-8");
        // 每页固定数量
        params.put("rn", PAGE_SIZE.toString());
        // 搜索来源, 高级搜索baiduadv, 百度内部搜索baidu (也包含部分广告)
        params.put("tn", "baiduadv");
        if (searchDTO.getKeyWord() != null) {
            params.put("q1", searchDTO.getKeyWord());
        }
        if (searchDTO.getAllKeyWord() != null) {
            params.put("q2", searchDTO.getAllKeyWord());
        }
        if (searchDTO.getAnyKeyWord() != null) {
            params.put("q3", searchDTO.getAnyKeyWord());
        }
        if (searchDTO.getNoKeyWord() != null) {
            params.put("q4", searchDTO.getNoKeyWord());
        }
        if (searchDTO.getFileType() != null) {
            params.put("ft", searchDTO.getFileType().getName());
        }
        if (searchDTO.getLimitSite() != null) {
            params.put("q6", searchDTO.getLimitSite());
        }
        if (searchDTO.getLimitTime() != null) {
            var times = searchDTO.getLimitTime().getStartAndEndTime();
            params.put("gpc", "stf=" + times[0].getEpochSecond() + ".000" + ","
                + times[1].getEpochSecond() + ".000" + "|stftype=1");
        } else {
            params.put("gpc", "stf");
        }
        return params;
    }

}
