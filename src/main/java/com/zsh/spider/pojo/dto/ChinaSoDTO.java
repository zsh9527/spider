package com.zsh.spider.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 中国搜索dto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ChinaSoDTO {

    private DataDTO data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DataDTO {

        private List<ContentDataDTO> data = Collections.emptyList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class ContentDataDTO {

        private String snippet;
        private String title;
        private String url;
        private Long timestamp;
    }
}
