package com.zsh.spider.pojo.po;


import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.dto.SearchDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * 搜索任务PO
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "task")
public class TaskPO {

    /**
     * 任务id -- 自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务查询参数
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private SearchDTO searchParam;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    private TaskStateEnum state = TaskStateEnum.NORMAL;

    /**
     * 任务创建时间
     */
    @Column(insertable = false, updatable = false)
    private Instant createDate;

    /**
     * 构造器
     */
    public TaskPO(SearchDTO searchParam) {
        this();
        this.searchParam = searchParam;
        this.taskName = searchParam.buildName();
    }
}
