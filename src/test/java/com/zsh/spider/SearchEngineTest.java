package com.zsh.spider;

import com.zsh.spider.config.SpiderConstants;
import com.zsh.spider.enums.FileTypeEnum;
import com.zsh.spider.enums.LimitTimeEnum;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import com.zsh.spider.engine.*;
import com.zsh.spider.service.SpiderDispatcherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 搜索引擎测试
 *
 * @author zsh
 * @version 1.0.0
 * @date 2022/09/27 18:26
 */
@SpringBootTest
@MockBean({SpiderDispatcherService.class})
public class SearchEngineTest {

    @Autowired
    private BaiduSearchEngine baiduSearchEngine;
    @Autowired
    private BingSearchEngine bingSearchEngine;
    @Autowired
    private SearchEngine360 searchEngine360;
    @Autowired
    private SougouSearchEngine sougouSearchEngine;
    @Autowired
    private ChinaSoSearchEngine chinaSoSearchEngine;
    @Autowired
    private YandexSearchEngine yandexSearchEngine;
    @Autowired
    private GoogleSearchEngine googleSearchEngine;
    @Autowired
    private YahooSearchEngine yahooSearchEngine;
    @Autowired
    private DuckDuckGoSearchEngine duckDuckGoSearchEngine;

    private BlockingQueue<SearchEngineResultDTO> asyncQueue = new LinkedBlockingQueue<>();

    @BeforeEach
    void before() {
        asyncQueue.clear();
    }

    /**
     * 百度参数构建
     */
    @Test
    public void buildBaiduParam() {
        SearchDTO searchDTO = new SearchDTO("内容", "完全匹配", "any1 any2",
            "nokey", FileTypeEnum.WORD, "baidu.com", LimitTimeEnum.DAY);
        var params = baiduSearchEngine.getSearchParam(searchDTO);
        var result = baiduSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * bing参数构建
     */
    @Test
    public void buildBingParam() {
        SearchDTO searchDTO = new SearchDTO("内容", "完全匹配", "any1 any2",
            "nokey", FileTypeEnum.WORD, "baidu.com", LimitTimeEnum.YEAR);
        var params = bingSearchEngine.getSearchParam(searchDTO);
        var result = baiduSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * 360参数构建
     */
    @Test
    public void build360Param() {
        SearchDTO searchDTO = new SearchDTO("内容", "完全匹配", null,
            null, FileTypeEnum.WORD, "baidu.com", LimitTimeEnum.YEAR);
        var params = searchEngine360.getSearchParam(searchDTO);
        var result = baiduSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * 搜狗参数构建
     */
    @Test
    public void buildSougouParam() {
        SearchDTO searchDTO = new SearchDTO("内容 测试", null, null,
            null, FileTypeEnum.WORD, "baidu.com", LimitTimeEnum.YEAR);
        var params = sougouSearchEngine.getSearchParam(searchDTO);
        var result = baiduSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * 中国搜索参数构建
     */
    @Test
    public void buildChainSoParam() {
        SearchDTO searchDTO = new SearchDTO("内容 测试", null, null,
            null, null, "baidu.com", LimitTimeEnum.YEAR);
        var params = sougouSearchEngine.getSearchParam(searchDTO);
        var result = chinaSoSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * yandex参数构建
     */
    @Test
    public void buildYandexSoParam() {
        SearchDTO searchDTO = new SearchDTO("内容 测试", null, null,
            null, null, "baidu.com", LimitTimeEnum.YEAR);
        var params = yandexSearchEngine.getSearchParam(searchDTO);
        var result = yandexSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * google参数构建
     */
    @Test
    public void buildGoogleParam() {
        SearchDTO searchDTO = new SearchDTO("内容", "完全匹配", "any1 any2",
            "nokey", FileTypeEnum.WORD, "baidu.com", LimitTimeEnum.DAY);
        var params = googleSearchEngine.getSearchParam(searchDTO);
        var result = baiduSearchEngine.buildParam(params);
        Assertions.assertNotEquals("", result);
    }

    /**
     * 百度搜索
     */
    @Test
    public void searchBaidu() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("abc123").setFileType(FileTypeEnum.PDF)
            .setLimitSite("baidu.com").setLimitTime(LimitTimeEnum.YEAR);
        baiduSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
    }

    /**
     * bing搜索
     */
    @Test
    public void searchBing() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("内容")
            .setLimitSite("baidu.com").setLimitTime(LimitTimeEnum.YEAR);
        bingSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= SpiderConstants.MAX_SEARCH_SIZE);
    }

    /**
     * 360搜索
     */
    @Test
    public void search360() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("内容")
            .setLimitSite("baidu.com").setFileType(FileTypeEnum.WORD).setLimitTime(LimitTimeEnum.YEAR);
        searchEngine360.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
    }

    /**
     * 搜狗搜索
     */
    @Test
    public void searchSougou() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("内容")
            .setFileType(FileTypeEnum.WORD).setLimitTime(LimitTimeEnum.YEAR);
        sougouSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 11);
    }

    /**
     * 中国搜索
     */
    @Test
    public void searchChinaSo() {
        SearchDTO searchDTO = new SearchDTO().setAllKeyWord("内容12").setLimitSite("baidu.com")
            .setLimitTime(LimitTimeEnum.YEAR);
        chinaSoSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 11);
    }

    /**
     * yandex搜索
     */
    @Test
    public void searchYandex() {
        SearchDTO searchDTO = new SearchDTO().setAllKeyWord("内容")
            .setFileType(FileTypeEnum.PDF);
        yandexSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 11);
    }

    /**
     * google搜索
     */
    @Test
    public void searchGoogle() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("abc123")
            .setLimitSite("baidu.com").setLimitTime(LimitTimeEnum.YEAR);
        googleSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 11);
    }

    /**
     * yahoo搜索
     */
    @Test
    public void searchYahoo() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("淘宝客服")
            .setLimitSite("baidu.com").setLimitTime(LimitTimeEnum.YEAR);
        yahooSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 6);
    }

    /**
     * duckduckgo搜索
     */
    @Test
    @Disabled
    public void searchDuckDuckGo() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("淘宝")
            .setLimitSite("baidu.com").setLimitTime(LimitTimeEnum.YEAR);
        duckDuckGoSearchEngine.search(searchDTO, asyncQueue);
        Assertions.assertFalse(asyncQueue.isEmpty());
        Assertions.assertTrue(asyncQueue.size() >= 6);
    }
}
