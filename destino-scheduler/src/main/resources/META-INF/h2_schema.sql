CREATE TABLE IF NOT EXISTS execution (
    scheduler_id bigint NOT NULL,
    execution_time bigint NOT NULL,
    mode integer NOT NULL,
    job_name varchar(300) DEFAULT '',
    script_type varchar(30) DEFAULT -1,
    script_version bigint DEFAULT -1,
    param varchar(3000) DEFAULT NULL,
    timeout bigint DEFAULT -1,
    scheduler_sign varchar(256) NOT NULL,
    scheduler_update_time bigint NOT NULL,
    process integer NOT NULL,
    addressing_strategy integer NOT NULL,
    blocked_strategy integer NOT NULL,
    expired_strategy integer NOT NULL,
    supervisor_id bigint NOT NULL,
    actual_executed_time bigint NOT NULL,
    dest_namespace varchar(300),
    dest_group_name varchar(300),
    dest_service_name varchar(300),
    dest_ip varchar(100),
    dest_port integer,
    dest_cluster varchar(300),
    dest_mode integer,
    PRIMARY KEY (scheduler_id, execution_time)
);

CREATE INDEX IF NOT EXISTS idx_execution ON execution (execution_time);
CREATE INDEX IF NOT EXISTS idx_namespace_execution ON execution (dest_namespace, execution_time);
CREATE INDEX IF NOT EXISTS idx_namespace_process_execution ON execution (dest_namespace, process, execution_time);

CREATE TABLE IF NOT EXISTS execution_activated (
    scheduler_id bigint NOT NULL,
    execution_time bigint NOT NULL,
    mode integer NOT NULL,
    job_name varchar(300) DEFAULT '',
    script_type integer DEFAULT -1,
    script_version bigint DEFAULT -1,
    param varchar(3000) DEFAULT NULL,
    timeout bigint DEFAULT -1,
    scheduler_sign varchar(256) NOT NULL,
    scheduler_update_time bigint NOT NULL,
    process integer NOT NULL,
    addressing_strategy integer NOT NULL,
    blocked_strategy integer NOT NULL,
    expired_strategy integer NOT NULL,
    supervisor_id bigint NOT NULL,
    actual_executed_time bigint NOT NULL,
    dest_namespace varchar(300),
    dest_group_name varchar(300),
    dest_service_name varchar(300),
    dest_ip varchar(100),
    dest_port integer,
    dest_cluster varchar(300),
    dest_mode integer,
    PRIMARY KEY (scheduler_id, execution_time)
);