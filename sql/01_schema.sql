-- ============================================================
-- 毕业设计管理系统 — 数据库建表脚本
-- 题目37：毕业设计管理系统
-- MySQL 8.0+
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS graduation_management
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE graduation_management;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 系信息表 (drop child tables first to respect FK order)
-- ============================================================
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS submission;
DROP TABLE IF EXISTS selection;
DROP TABLE IF EXISTS topic;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS class;
DROP TABLE IF EXISTS major;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS deadline;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS department;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE department (
    dept_id     INT PRIMARY KEY AUTO_INCREMENT  COMMENT '系ID',
    dept_name   VARCHAR(50) NOT NULL UNIQUE      COMMENT '系名称',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系信息表';

-- ============================================================
-- 2. 专业信息表
-- ============================================================
CREATE TABLE major (
    major_id    INT PRIMARY KEY AUTO_INCREMENT   COMMENT '专业ID',
    major_name  VARCHAR(50) NOT NULL             COMMENT '专业名称',
    dept_id     INT NOT NULL                     COMMENT '所属系ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_major_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_major_name_dept UNIQUE (major_name, dept_id)
) ENGINE=InnoDB COMMENT='专业信息表';

-- ============================================================
-- 3. 班级信息表
-- ============================================================
CREATE TABLE class (
    class_id    INT PRIMARY KEY AUTO_INCREMENT   COMMENT '班级ID',
    class_name  VARCHAR(50) NOT NULL             COMMENT '班级名称，如"计算机2024-1班"',
    major_id    INT NOT NULL                     COMMENT '所属专业ID',
    grade       VARCHAR(10) NOT NULL             COMMENT '年级，如"2024"',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES major(major_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_class_name_major UNIQUE (class_name, major_id)
) ENGINE=InnoDB COMMENT='班级信息表';

-- ============================================================
-- 4. 学生信息表
-- ============================================================
CREATE TABLE student (
    stu_id      VARCHAR(20) PRIMARY KEY           COMMENT '学号',
    stu_name    VARCHAR(30) NOT NULL              COMMENT '姓名',
    password    VARCHAR(255) NOT NULL DEFAULT '$2a$10$placeholder' COMMENT '密码(BCrypt加密)',
    class_id    INT NOT NULL                      COMMENT '所属班级ID',
    phone       VARCHAR(20) DEFAULT NULL          COMMENT '联系电话',
    email       VARCHAR(100) DEFAULT NULL         COMMENT '邮箱',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(class_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_student_phone CHECK (phone IS NULL OR LENGTH(phone) >= 8),
    CONSTRAINT chk_student_email CHECK (email IS NULL OR email LIKE '%@%')
) ENGINE=InnoDB COMMENT='学生信息表';

-- ============================================================
-- 5. 教师信息表
-- ============================================================
CREATE TABLE teacher (
    teacher_id      VARCHAR(20) PRIMARY KEY       COMMENT '教师工号',
    teacher_name    VARCHAR(30) NOT NULL          COMMENT '姓名',
    password        VARCHAR(255) NOT NULL DEFAULT '$2a$10$placeholder' COMMENT '密码(BCrypt加密)',
    dept_id         INT NOT NULL                  COMMENT '所属系ID',
    title           VARCHAR(30) DEFAULT '讲师'    COMMENT '职称',
    research_direction VARCHAR(200) DEFAULT NULL  COMMENT '研究方向',
    phone           VARCHAR(20) DEFAULT NULL      COMMENT '联系电话',
    email           VARCHAR(100) DEFAULT NULL     COMMENT '邮箱',
    max_students    INT DEFAULT 5                 COMMENT '最多指导学生数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_teacher_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_teacher_max CHECK (max_students > 0 AND max_students <= 20),
    CONSTRAINT chk_teacher_email CHECK (email IS NULL OR email LIKE '%@%')
) ENGINE=InnoDB COMMENT='教师信息表';

-- ============================================================
-- 6. 毕业设计题目表
-- ============================================================
CREATE TABLE topic (
    topic_id    INT PRIMARY KEY AUTO_INCREMENT    COMMENT '题目ID',
    title       VARCHAR(200) NOT NULL             COMMENT '题目名称',
    description TEXT DEFAULT NULL                 COMMENT '题目描述与要求',
    teacher_id  VARCHAR(20) NOT NULL              COMMENT '指导教师工号',
    direction   VARCHAR(100) DEFAULT NULL         COMMENT '研究方向（如：Web开发、机器学习、数据库）',
    status      TINYINT DEFAULT 0                 COMMENT '状态：0-可选，1-已选',
    max_select  INT DEFAULT 1                     COMMENT '最多可选人数',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_topic_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_topic_max CHECK (max_select >= 1)
) ENGINE=InnoDB COMMENT='毕业设计题目表';

-- ============================================================
-- 7. 选题记录表
-- ============================================================
CREATE TABLE selection (
    selection_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '选题记录ID',
    stu_id          VARCHAR(20) NOT NULL UNIQUE    COMMENT '学生学号（1:1）',
    topic_id        INT NOT NULL                   COMMENT '题目ID',
    select_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选题时间',
    status          VARCHAR(20) DEFAULT '进行中'   COMMENT '状态：进行中/已完成',
    final_score     DECIMAL(5,2) DEFAULT NULL      COMMENT '最终综合成绩',
    completed_at    DATETIME DEFAULT NULL          COMMENT '完成时间',
    CONSTRAINT fk_selection_student FOREIGN KEY (stu_id) REFERENCES student(stu_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_selection_topic FOREIGN KEY (topic_id) REFERENCES topic(topic_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_selection_score CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 100))
) ENGINE=InnoDB COMMENT='选题记录表';

-- ============================================================
-- 8. 阶段文档提交表
-- ============================================================
CREATE TABLE submission (
    submission_id   INT PRIMARY KEY AUTO_INCREMENT  COMMENT '提交记录ID',
    selection_id    INT NOT NULL                    COMMENT '选题记录ID',
    stage           VARCHAR(20) NOT NULL            COMMENT '阶段：开题报告/中期检查/初稿/终稿',
    file_path       VARCHAR(500) DEFAULT NULL       COMMENT '文档存储路径',
    description     TEXT DEFAULT NULL               COMMENT '提交说明',
    submit_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    status          VARCHAR(20) DEFAULT '待审阅'    COMMENT '审阅状态：待审阅/已通过/需修改',
    version         INT DEFAULT 1                   COMMENT '版本号（支持多次提交）',
    CONSTRAINT fk_submission_selection FOREIGN KEY (selection_id)
        REFERENCES selection(selection_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_submission_stage CHECK (stage IN ('开题报告', '中期检查', '初稿', '终稿')),
    CONSTRAINT chk_submission_status CHECK (status IN ('待审阅', '已通过', '需修改'))
) ENGINE=InnoDB COMMENT='阶段文档提交表';

-- ============================================================
-- 9. 教师审阅评分表
-- ============================================================
CREATE TABLE review (
    review_id       INT PRIMARY KEY AUTO_INCREMENT  COMMENT '审阅记录ID',
    submission_id   INT NOT NULL                    COMMENT '提交记录ID',
    teacher_id      VARCHAR(20) NOT NULL            COMMENT '审阅教师工号',
    score           DECIMAL(5,2) NOT NULL           COMMENT '评分（0-100）',
    comment         TEXT DEFAULT NULL               COMMENT '评语',
    review_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审阅时间',
    CONSTRAINT fk_review_submission FOREIGN KEY (submission_id)
        REFERENCES submission(submission_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_review_score CHECK (score >= 0 AND score <= 100),
    CONSTRAINT uk_review_submission UNIQUE (submission_id)   -- 每份提交只能评审一次
) ENGINE=InnoDB COMMENT='教师审阅评分表';

-- ============================================================
-- 10. 流程期限配置表
-- ============================================================
CREATE TABLE deadline (
    deadline_id     INT PRIMARY KEY AUTO_INCREMENT  COMMENT '期限配置ID',
    stage           VARCHAR(20) NOT NULL            COMMENT '阶段名称',
    start_date      DATE NOT NULL                   COMMENT '开放起始日期',
    end_date        DATE NOT NULL                   COMMENT '截止日期',
    description     VARCHAR(500) DEFAULT NULL       COMMENT '说明',
    semester        VARCHAR(20) DEFAULT NULL        COMMENT '学期，如"2024-2025-2"',
    CONSTRAINT chk_deadline_date CHECK (end_date >= start_date),
    CONSTRAINT chk_deadline_stage CHECK (stage IN ('选题', '开题报告', '中期检查', '初稿', '终稿')),
    CONSTRAINT uk_deadline_stage_semester UNIQUE (stage, semester)
) ENGINE=InnoDB COMMENT='流程期限配置表';

-- ============================================================
-- 11. 审计日志表（后续将按学期分区）
-- ============================================================
CREATE TABLE audit_log (
    log_id          BIGINT AUTO_INCREMENT           COMMENT '日志ID',
    table_name      VARCHAR(50) NOT NULL            COMMENT '操作的表名',
    operation       VARCHAR(10) NOT NULL            COMMENT '操作类型：INSERT/UPDATE/DELETE',
    record_id       VARCHAR(50) DEFAULT NULL        COMMENT '被操作记录的主键值',
    old_value       JSON DEFAULT NULL               COMMENT '操作前数据（JSON格式）',
    new_value       JSON DEFAULT NULL               COMMENT '操作后数据（JSON格式）',
    operated_by     VARCHAR(30) DEFAULT NULL        COMMENT '操作者（用户名或工号/学号）',
    operation_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address      VARCHAR(50) DEFAULT NULL        COMMENT '操作IP地址',
    PRIMARY KEY (log_id, operation_time),
    INDEX idx_audit_table (table_name),
    INDEX idx_audit_time (operation_time),
    INDEX idx_audit_operator (operated_by)
) ENGINE=InnoDB COMMENT='审计日志表'
PARTITION BY RANGE (TO_DAYS(operation_time)) (
    PARTITION p2024 VALUES LESS THAN (TO_DAYS('2025-01-01')),
    PARTITION p2025 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION p2026 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ============================================================
-- 12. 系统用户表（管理员）
-- ============================================================
CREATE TABLE sys_user (
    user_id     INT PRIMARY KEY AUTO_INCREMENT     COMMENT '用户ID',
    username    VARCHAR(30) NOT NULL UNIQUE        COMMENT '用户名',
    password    VARCHAR(255) NOT NULL              COMMENT '密码(BCrypt加密)',
    real_name   VARCHAR(30) DEFAULT NULL           COMMENT '真实姓名',
    role        VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sys_user_role CHECK (role IN ('ADMIN', 'SUPER_ADMIN'))
) ENGINE=InnoDB COMMENT='系统用户表（管理员）';

-- ============================================================
-- 基本索引创建（更多索引见 06_indexes.sql）
-- ============================================================
-- 学生表高频查询索引
CREATE INDEX idx_student_class ON student(class_id);
CREATE INDEX idx_student_name ON student(stu_name);

-- 教师表索引
CREATE INDEX idx_teacher_dept ON teacher(dept_id);
CREATE INDEX idx_teacher_name ON teacher(teacher_name);

-- 题目表索引
CREATE INDEX idx_topic_teacher ON topic(teacher_id);
CREATE INDEX idx_topic_status ON topic(status);
CREATE INDEX idx_topic_direction ON topic(direction);

-- 选题表索引
CREATE INDEX idx_selection_topic ON selection(topic_id);
CREATE INDEX idx_selection_status ON selection(status);

-- 文档提交表索引
CREATE INDEX idx_submission_selection ON submission(selection_id);
CREATE INDEX idx_submission_stage ON submission(stage);
CREATE INDEX idx_submission_status ON submission(status);

-- 审阅表索引
CREATE INDEX idx_review_teacher ON review(teacher_id);
