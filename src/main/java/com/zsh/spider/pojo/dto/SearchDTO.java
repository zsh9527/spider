package com.zsh.spider.pojo.dto;


import com.zsh.spider.config.SpiderConstants;
import com.zsh.spider.enums.FileTypeEnum;
import com.zsh.spider.enums.LimitTimeEnum;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 搜索dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SearchDTO implements Serializable {

    /**
     * 关键字 -- 类似普通的搜索
     */
    private String keyWord;

    /**
     * 匹配的完整关键字
     */
    @Nullable
    private String allKeyWord;

    /**
     * 匹配的任意关键字 -- 多个关键字空格分离
     */
    @Nullable
    private String anyKeyWord;

    /**
     * 不包含的关键字 -- 多个关键字空格分离
     */
    @Nullable
    private String noKeyWord;

    /**
     * 文件类型 doc
     */
    @Nullable
    private FileTypeEnum fileType;

    /**
     * 限制搜索网站 -- 用于只在特定站内搜索
     */
    @Nullable
    private String limitSite;

    /**
     * 限制时间
     */
    @Nullable
    private LimitTimeEnum limitTime;

    /**
     * 是否限制结果为简体中文
     */
    private boolean limitChinese = false;

    /**
     * 每个搜索引擎最大爬取数量
     */
    private int maxSearchSize = SpiderConstants.MAX_SEARCH_SIZE;

    /**
     * false 则只爬取搜索引擎数据
     */
    private boolean deepSearch = true;

    /**
     * 严格检查子级url
     * true 则只爬虫a标签且title和搜索内容关联
     */
    private boolean strictChildUrl = false;

    /**
     * 忽略网站 -- 搜索引擎中不生效, 逐层爬取时生效
     */
    private List<String> ignoreSpiderSite = Collections.emptyList();

    public SearchDTO(String keyWord, String allKeyWord, String anyKeyWord, String noKeyWord,
                     FileTypeEnum fileType, String limitSite, LimitTimeEnum limitTime) {
        this();
        this.keyWord = keyWord;
        this.allKeyWord = allKeyWord;
        this.anyKeyWord = anyKeyWord;
        this.noKeyWord = noKeyWord;
        this.fileType = fileType;
        this.limitSite = limitSite;
        this.limitTime = limitTime;
    }

    public String buildName() {
        if (keyWord != null) {
            return keyWord;
        }
        if (allKeyWord != null) {
            return allKeyWord;
        }
        return anyKeyWord;
    }
}
