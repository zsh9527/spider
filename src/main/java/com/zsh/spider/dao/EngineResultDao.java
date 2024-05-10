package com.zsh.spider.dao;

import com.zsh.spider.pojo.po.EngineResultPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * EngineResultDao
 */
public interface EngineResultDao extends JpaRepository<EngineResultPO, Long> {

    boolean existsByTaskIdAndUrl(Long taskId, String url);

    @Query("""
        from #{#entityName} t
        where
            t.taskId = :taskId
            and (:engineName is null or t.sourceEngineName = :engineName)
    """)
    Page<EngineResultPO> findAllByTaskId(Long taskId, String engineName, PageRequest pageRequest);
}
