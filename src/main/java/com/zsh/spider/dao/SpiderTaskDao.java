package com.zsh.spider.dao;

import com.zsh.spider.pojo.po.SpiderTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SpiderTaskDao
 */
public interface SpiderTaskDao extends JpaRepository<SpiderTaskPO, Long> {

    /**
     * 按照分数倒叙查询数据
     */
    @Query(value = """
        select t1.* from `spider_task` t1
        where id in (
            select t.id
            from `spider_task` t
            where
                1 = 1
            order by t.score desc
            limit :limit
        )
    """, nativeQuery = true)
    List<SpiderTaskPO> findAllOrderByScoreDescLimit(long limit);

    /**
     * 按照分数倒叙查询数据
     */
    @Query(value = """
        select t.*
        from `spider_task` t
        where
            1 = 1
        order by t.score desc
        limit :limit
    """, nativeQuery = true)
    List<SpiderTaskPO> findAllOrderByScoreDescLimit2(long limit);

    /**
     * 按照分数倒叙查询数据
     */
    @Query(value = """
        select t1.* from `spider_task` t1
        where id in (select temp.id from (
            select t.id
            from `spider_task` t
            where
                1 = 1
            order by t.score desc
            limit :limit
        ) as temp)
    """, nativeQuery = true)
    List<SpiderTaskPO> findAllOrderByScoreDescLimit3(long limit);

    /**
     * 按照分数查询然后删除任务 -- 检查那个速度更快
     *
     * @param limit 最大删除数量
     */
    @Query(value = """
        select t.id
        from `spider_task` t
        where
            t.score < :score
        order by t.score asc
        limit :limit
    """, nativeQuery = true)
    List<Long> listIdByScoreLtOrderByScoreDescLimit(double score, long limit);

    /**
     * 按照分数删除任务
     *
     * @param limit 最大删除数量
     */
    @Modifying
    @Query(value = """
        delete from `spider_task` t1
        where t1.id in (
            select t.id
            from `spider_task` t
            where
                t.score < :score
            order by t.score asc
            limit :limit
        )
    """, nativeQuery = true)
    @Transactional
    int deleteByScoreLtOrderByScoreDescLimit(double score, long limit);

    /**
     * 删除已中止或过期任务
     */
    @Modifying
    @Query(value = """
        delete from `spider_task` t
        where t.task_id in (
            select id
            from task
            where
                state != 'NORMAL'
        )
    """, nativeQuery = true)
    @Transactional
    int deleteAllNotNormalTask();
}
