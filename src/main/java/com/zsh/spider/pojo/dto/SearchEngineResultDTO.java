package com.zsh.spider.pojo.dto;


import com.zsh.spider.util.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jsoup.Jsoup;

import java.time.Instant;

/**
 * 搜索引擎结果DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchEngineResultDTO {

    /**
     * 标题
     */
    private String title;

    /**
     * url
     */
    private String url;

    /**
     * 匹配的关联内容
     */
    private String content;

    /**
     * 内容更新时间
     */
    private Instant uploadTime;

    /**
     * 查询结果index
     */
    private Integer index;

    /**
     * 来源引擎名称
     */
    private String sourceEngineName;

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setSourceEngineName(String sourceEngineName) {
        this.sourceEngineName = sourceEngineName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUploadTime(Instant uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 设置内容并解析时间
     */
    public void setContent(String content) {
        this.content = content;
        this.uploadTime = HttpUtils.parseTime(content);
    }

    public SearchEngineResultDTO( String url, String title, String content, Instant uploadTime) {
        this.url = url;
        this.title = Jsoup.parse(title).text();
        this.content = Jsoup.parse(content).text();
        this.uploadTime = uploadTime;
    }
}
