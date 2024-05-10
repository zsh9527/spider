package com.zsh.spider.dao;

import com.zsh.spider.pojo.po.SpiderResultPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * SpiderResultDao
 */
public interface SpiderResultDao extends JpaRepository<SpiderResultPO, Long> {

    boolean existsByTaskIdAndUrl(Long taskId, String url);

    @Query("""
        from #{#entityName} t 
        where
            t.taskId = :taskId 
            and t.similarScore >= :score 
            and (:engineName is null or t.sourceEngineName = :engineName) 
            and (:content is null or t.content like %:content%) 
    """)
    Page<SpiderResultPO> findAllByTaskId(Long taskId, Double score, String engineName, String content, PageRequest pageRequest);

    /**
     * 测试专用
     */
    @Modifying
    @Transactional
    @Query("delete from #{#entityName} t where t.taskId = :taskId")
    Integer deleteByTaskId(Long taskId);
}
