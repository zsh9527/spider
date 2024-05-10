CREATE TABLE IF NOT EXISTS `task`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_name` varchar(255) NOT NULL COMMENT '任务名称',
--  设置为json格式反序列化会报错
    `search_param` varchar(65535) NOT NULL COMMENT '任务查询参数',
    `state` varchar(12) NOT NULL DEFAULT 'NORMAL' COMMENT '任务状态',
    `create_date` datetime ( 0 ) NOT NULL DEFAULT CURRENT_TIMESTAMP ( 0 ) COMMENT '创建时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `engine_result`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint(11) NOT NULL COMMENT '任务ID',
    `index` int(11) NOT NULL COMMENT '查询结果index',
    `source_engine_name` varchar(255) NOT NULL COMMENT '来源引擎名称',
    `url` varchar(4096) NOT NULL COMMENT 'url',
    `title` varchar(4096) NOT NULL COMMENT '标题',
    `content` text NOT NULL COMMENT '匹配的关联内容',
    `upload_time` datetime ( 0 ) NULL COMMENT '内容更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX uk_task_url (`task_id`, `url`)
);

CREATE TABLE IF NOT EXISTS `spider_result`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint(11) NOT NULL COMMENT '任务ID',
    `source_engine_name` varchar(255) NOT NULL COMMENT '来源引擎名称',
    `title` varchar(4096) NULL COMMENT '标题',
    `url` varchar(4096) NOT NULL COMMENT 'url',
    `parent_url` varchar(4096) NULL COMMENT '父级url',
    `content` text NOT NULL COMMENT '匹配的关联内容',
    `similar_score` double NOT NULL COMMENT '相似度分数',
    `match_content` text NULL COMMENT '匹配的部分内容',
    `child_url` text NULL COMMENT '内部url',
    `create_date` datetime ( 0 ) NOT NULL DEFAULT CURRENT_TIMESTAMP ( 0 ) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX uk2_task_url (`task_id`, `url`)
);

CREATE TABLE IF NOT EXISTS `spider_task`  (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint(11) NOT NULL COMMENT '任务ID',
    `score` double NOT NULL COMMENT '关联分数',
--  设置为json格式反序列化会报错
    `spider` varchar(12000) NOT NULL COMMENT '爬取参数',
    PRIMARY KEY (`id`)
);