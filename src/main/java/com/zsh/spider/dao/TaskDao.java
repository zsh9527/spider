package com.zsh.spider.dao;

import com.zsh.spider.enums.TaskStateEnum;
import com.zsh.spider.pojo.po.TaskPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * TaskDao
 */
public interface TaskDao extends JpaRepository<TaskPO, Long> {

    /**
     * 更新任务状态
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        update #{#entityName} t
        set
            t.state = :state
        where t.id = :id
    """)
    @Transactional
    int updateStateById(TaskStateEnum state, Long id);

    /**
     * 查询即将过期任务
     */
    @Query(value = """
        select t.id
        from #{#entityName} t
        where t.state = :state
        and t.createDate < :date
    """)
    List<Long> findIdToBeExpireTask(TaskStateEnum state, Instant date);

    @Query(value = """
        select t.*,
            (SELECT COUNT(*) FROM engine_result t2 WHERE t2.task_id = t.id) AS engine_count,
            (SELECT COUNT(*) FROM spider_result t3 WHERE t3.task_id = t.id) AS spider_count
        from #{#entityName} t
        where
            1 = 1
    """, countQuery = """
        select count(t.*),
        from #{#entityName} t
        where
            1 = 1
    """, nativeQuery = true)
    Page<Map<String, String>> findAllAndCount(PageRequest pageRequest);
}
