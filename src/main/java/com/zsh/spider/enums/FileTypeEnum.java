package com.zsh.spider.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 */
@Getter
public enum FileTypeEnum {
    PDF("pdf"),
    WORD("doc"),
    EXCEL("xls");

    private final String name;

    FileTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
