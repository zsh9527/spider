package com.zsh.spider.pojo.vo;


import com.zsh.spider.enums.TaskStateEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 任务
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskWithCountVO {

    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务查询参数
     */
    private String searchParam;

    /**
     * 任务状态
     */
    private TaskStateEnum state;

    /**
     * 任务创建时间
     */
    private Instant createDate;

    private Long engineCount;

    private Long spiderCount;
}
