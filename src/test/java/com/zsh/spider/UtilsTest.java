package com.zsh.spider;

import com.zsh.spider.dao.EngineResultDao;
import com.zsh.spider.dao.SpiderResultDao;
import com.zsh.spider.pojo.dto.SpiderDTO;
import com.zsh.spider.pojo.po.EngineResultPO;
import com.zsh.spider.pojo.po.SpiderResultPO;
import com.zsh.spider.service.SpiderDispatcherService;
import com.zsh.spider.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类测试
 *
 * @author zsh
 * @version 1.0.0
 * @date 2022/09/27 18:26
 */
@Slf4j
@SpringBootTest
@MockBean({SpiderDispatcherService.class})
public class UtilsTest {

    @Autowired
    private SpiderResultDao spiderResultDao;
    @Autowired
    private EngineResultDao engineResultDao;

    /**
     * 解析时间
     */
    @Test
    public void testParseTime() {
        var result = HttpUtils.parseTime(
            "深圳房价近十年年度数据走势分析-中国房价行情 2023年 69,058 元/㎡ 平均总价: 805 万元 售租比: 61 挂牌数量: 6.92 万套 分析使用样本: 楼盘小区 个 | 房产交易 495.00万套次 数据纠错 出售单价走势 平均单价: 69,058 元/㎡ 房价走势 图表 数据 * 价值=平均房租(元/月/㎡)... https://www.creprice.cn/...- 2024-1-4 推荐您搜索：");
        var day = Duration.between(result, Instant.parse("2024-01-04T00:00:00Z")).abs().toDays();
        Assertions.assertTrue(day <= 1);
        result = HttpUtils.parseTime(
            "深圳房价十年走势图深圳房价上涨如此迅猛的原因-装修新闻-好设计... 2024年1月26日 - 也推动了深圳房价的不断上涨。10年间,深圳房价上涨了多少?哪个区的房价涨幅最快?近10年深圳房价走势图显示,从2006年至2021年上半年,深圳商品... bj.haogu114.com");
        day = Duration.between(result, Instant.parse("2024-01-26T00:00:00Z")).abs().toDays();
        Assertions.assertTrue(day <= 1);
    }

    /**
     * 测试分数构造器
     */
    @Test
    public void testSpiderDTO() {
        double delta = 0.001; // 设置一个允许的误差范围
        var score = new SpiderDTO("url", 1, "testname").getScore();
        Assertions.assertEquals(10, score, delta);
        score = new SpiderDTO("url", 12, "testname").getScore();
        Assertions.assertEquals(9, score, delta);
        score = new SpiderDTO("url", 23, "testname").getScore();
        Assertions.assertEquals(8, score, delta);
        score = new SpiderDTO("url", 97, "testname").getScore();
        Assertions.assertEquals(1, score, delta);
        score = new SpiderDTO("url", 102, "testname").getScore();
        Assertions.assertEquals(1, score, delta);
        score = new SpiderDTO("url", 1102, "testname").getScore();
        Assertions.assertEquals(1, score, delta);
    }

    /**
     * 测试分数构造器
     */
    @Test
    public void testSpiderDTO2() {
        SpiderDTO spiderDTO = new SpiderDTO();
        spiderDTO.setParentUrl("parentUrl");
        spiderDTO.setLevel(0);
        spiderDTO.setScore(1);
        SpiderDTO obj = new SpiderDTO("url", spiderDTO);
        Assertions.assertEquals(0.9, obj.getScore());
        obj = new SpiderDTO("url", obj);
        Assertions.assertEquals(0.9 * 0.8, obj.getScore());
        obj = new SpiderDTO("url", obj);
        Assertions.assertEquals(0.9 * 0.8 * 0.7, obj.getScore());
        spiderDTO.setLevel(11);
        obj = new SpiderDTO("url", spiderDTO);
        Assertions.assertEquals(0.1, obj.getScore());
    }

    /**
     * 测试url正则测试器
     */
    @Test
    public void testUrlPattern() {
        Pattern httpPattern1 = HttpUtils.getUrlPattern(1);
        Pattern httpPattern2 = HttpUtils.getUrlPattern(2);
        Map<Pattern, String> map = Map.of(
            httpPattern1, "httpPattern1",
            httpPattern2, "httpPattern2");
        List<EngineResultPO> list = engineResultDao.findAll();
        list.stream().forEach(obj -> {
            map.entrySet().stream().forEach(entry -> {
                Matcher matcher = entry.getKey().matcher(obj.getUrl());
                if (!matcher.find()) {
                    log.info(entry.getValue() + "匹配失败" + obj.getUrl());
                }
                while (matcher.find()) {
                    if (!obj.getUrl().equals(matcher.group())) {
                        log.info(entry.getValue() + "匹配失败" + obj.getUrl());
                    }
                }
            });
        });

        List<SpiderResultPO> list2 = spiderResultDao.findAll();
        list2.stream().forEach(obj -> {
            map.entrySet().stream().forEach(entry -> {
                Matcher matcher = entry.getKey().matcher(obj.getUrl());
                if (!matcher.find()) {
                    log.info(entry.getValue() + "匹配失败" + obj.getUrl());
                }
                while (matcher.find()) {
                    if (!obj.getUrl().equals(matcher.group())) {
                        log.info(entry.getValue() + "匹配失败" + obj.getUrl());
                    }
                }
                if (obj.getChildUrl() != null) {
                    obj.getChildUrl().stream().forEach(url -> {
                        Matcher matcher2 = entry.getKey().matcher(url);
                        if (!matcher2.find()) {
                            log.info(entry.getValue() + "匹配失败" + url);
                        }
                        while (matcher2.find()) {
                            if (!obj.getUrl().equals(matcher2.group())) {
                                log.info(entry.getValue() + "匹配失败" + url);
                            }
                        }
                    });
                }
            });
        });

    }
}

