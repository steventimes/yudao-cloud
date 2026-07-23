-- Yudao BPM 业务表（Flowable ACT_* 表由引擎自动维护）。
-- 可重复执行；用于已有基础库未包含 BPM 模块表的环境。
CREATE TABLE IF NOT EXISTS `bpm_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号', `name` varchar(63) NOT NULL COMMENT '名称',
  `code` varchar(63) NOT NULL COMMENT '编码', `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态', `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_code_tenant` (`code`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程分类';

CREATE TABLE IF NOT EXISTS `bpm_form` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号', `name` varchar(63) NOT NULL COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT 0, `conf` varchar(1000) DEFAULT NULL COMMENT '表单配置',
  `fields` varchar(5000) DEFAULT NULL COMMENT '表单字段 JSON', `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 动态表单';

CREATE TABLE IF NOT EXISTS `bpm_process_definition_info` (
  `id` bigint NOT NULL AUTO_INCREMENT, `process_definition_id` varchar(64) NOT NULL,
  `model_id` varchar(64) NOT NULL, `model_type` tinyint NOT NULL DEFAULT 10,
  `category` varchar(64) DEFAULT NULL, `icon` varchar(512) DEFAULT NULL, `description` varchar(255) DEFAULT NULL,
  `form_type` tinyint NOT NULL, `form_id` bigint DEFAULT NULL, `form_conf` varchar(1000) DEFAULT NULL,
  `form_fields` varchar(5000) DEFAULT NULL, `form_custom_create_path` varchar(255) DEFAULT NULL,
  `form_custom_view_path` varchar(255) DEFAULT NULL, `simple_model` text,
  `visible` bit(1) NOT NULL DEFAULT b'1', `sort` bigint DEFAULT 0,
  `start_user_ids` varchar(255) DEFAULT NULL, `start_dept_ids` varchar(255) DEFAULT NULL,
  `manager_user_ids` varchar(255) DEFAULT NULL, `allow_cancel_running_process` bit(1) DEFAULT b'1',
  `allow_withdraw_task` bit(1) DEFAULT b'0', `process_id_rule` varchar(1000) DEFAULT NULL,
  `auto_approval_type` tinyint DEFAULT NULL, `title_setting` varchar(1000) DEFAULT NULL,
  `summary_setting` varchar(1000) DEFAULT NULL, `process_before_trigger_setting` text,
  `process_after_trigger_setting` text, `task_before_trigger_setting` text, `task_after_trigger_setting` text,
  `print_template_setting` text, `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_process_definition_id` (`process_definition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程定义信息';

CREATE TABLE IF NOT EXISTS `bpm_process_instance_copy` (
  `id` bigint NOT NULL AUTO_INCREMENT, `start_user_id` bigint NOT NULL, `process_instance_name` varchar(64) DEFAULT NULL,
  `process_instance_id` varchar(64) NOT NULL, `process_definition_id` varchar(64) NOT NULL,
  `category` varchar(64) DEFAULT NULL, `activity_id` varchar(64) DEFAULT NULL, `activity_name` varchar(64) DEFAULT NULL,
  `task_id` varchar(64) DEFAULT NULL, `user_id` bigint NOT NULL, `reason` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), KEY `idx_user_id` (`user_id`), KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程抄送';

CREATE TABLE IF NOT EXISTS `bpm_process_expression` (
  `id` bigint NOT NULL AUTO_INCREMENT, `name` varchar(63) NOT NULL, `status` tinyint NOT NULL DEFAULT 0,
  `expression` varchar(255) NOT NULL, `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程表达式';

CREATE TABLE IF NOT EXISTS `bpm_process_listener` (
  `id` bigint NOT NULL AUTO_INCREMENT, `name` varchar(63) NOT NULL, `status` tinyint NOT NULL DEFAULT 0,
  `type` varchar(32) NOT NULL, `event` varchar(32) NOT NULL, `value_type` varchar(32) NOT NULL, `value` varchar(255) NOT NULL,
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 流程监听器';

CREATE TABLE IF NOT EXISTS `bpm_user_group` (
  `id` bigint NOT NULL AUTO_INCREMENT, `name` varchar(63) NOT NULL, `description` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0, `user_ids` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `tenant_id` bigint NOT NULL DEFAULT 0, PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BPM 用户组';
