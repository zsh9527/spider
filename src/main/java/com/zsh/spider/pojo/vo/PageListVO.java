package com.zsh.spider.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * PageListVO
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/12 17:19
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageListVO<T> {
    /**
     * 响应体
     */
    private List<T> list;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 任务数量
     */
    private Long taskCount;

    public PageListVO(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }
}
