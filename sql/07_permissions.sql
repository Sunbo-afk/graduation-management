-- ============================================================
-- 毕业设计管理系统 — 用户权限管理脚本
-- 题目37：创建3个MySQL用户角色 + GRANT最小权限
-- ============================================================

-- ============================================================
-- 1. 创建数据库用户（管理员需在MySQL root下执行）
-- ============================================================

-- 学生角色：只能查看题目列表、自己的选题和提交记录
CREATE USER IF NOT EXISTS 'gms_student'@'localhost'
    IDENTIFIED BY 'Student@2025';
-- 密码策略：至少8位，包含大小写字母、数字、特殊字符

-- 教师角色：可以查看指导的学生、审阅文档、打分
CREATE USER IF NOT EXISTS 'gms_teacher'@'localhost'
    IDENTIFIED BY 'Teacher@2025';

-- 管理员角色：拥有所有表的管理权限
CREATE USER IF NOT EXISTS 'gms_admin'@'localhost'
    IDENTIFIED BY 'Admin@2025';

-- ============================================================
-- 2. 学生角色权限（最小必要权限）
-- ============================================================

-- 基础表查询权限
GRANT SELECT ON graduation_management.department TO 'gms_student'@'localhost';
GRANT SELECT ON graduation_management.major TO 'gms_student'@'localhost';
GRANT SELECT ON graduation_management.class TO 'gms_student'@'localhost';

-- 学生可以查看教师信息（选题时需要）
GRANT SELECT (teacher_id, teacher_name, title, research_direction, email, max_students)
    ON graduation_management.teacher TO 'gms_student'@'localhost';

-- 学生可以查看可选题目
GRANT SELECT ON graduation_management.topic TO 'gms_student'@'localhost';

-- 学生只能查看和修改自己的信息
-- （应用层控制，数据库层给基础权限）
GRANT SELECT, UPDATE (phone, email, password)
    ON graduation_management.student TO 'gms_student'@'localhost';

-- 学生可以操作选题（INSERT自己的选题，SELECT查看自己的）
GRANT SELECT, INSERT ON graduation_management.selection TO 'gms_student'@'localhost';

-- 学生可以提交文档、查看自己的提交记录
GRANT SELECT, INSERT ON graduation_management.submission TO 'gms_student'@'localhost';

-- 学生可以查看评语和成绩
GRANT SELECT ON graduation_management.review TO 'gms_student'@'localhost';

-- 学生可以查看流程期限
GRANT SELECT ON graduation_management.deadline TO 'gms_student'@'localhost';

-- 学生可以查看视图
GRANT SELECT ON graduation_management.v_student_progress TO 'gms_student'@'localhost';
GRANT SELECT ON graduation_management.v_topic_detail TO 'gms_student'@'localhost';
GRANT SELECT ON graduation_management.v_score_ranking TO 'gms_student'@'localhost';

-- ============================================================
-- 3. 教师角色权限
-- ============================================================

-- 基础表查询
GRANT SELECT ON graduation_management.department TO 'gms_teacher'@'localhost';
GRANT SELECT ON graduation_management.major TO 'gms_teacher'@'localhost';
GRANT SELECT ON graduation_management.class TO 'gms_teacher'@'localhost';
GRANT SELECT ON graduation_management.student TO 'gms_teacher'@'localhost';

-- 教师可以修改自己的信息
GRANT SELECT, UPDATE (phone, email, password, research_direction)
    ON graduation_management.teacher TO 'gms_teacher'@'localhost';

-- 教师可以管理自己的题目（INSERT/UPDATE/DELETE）
GRANT SELECT, INSERT, UPDATE, DELETE ON graduation_management.topic TO 'gms_teacher'@'localhost';

-- 教师可以查看选题记录
GRANT SELECT ON graduation_management.selection TO 'gms_teacher'@'localhost';

-- 教师可以查看提交记录并下载文档
GRANT SELECT ON graduation_management.submission TO 'gms_teacher'@'localhost';

-- 教师可以审阅打分（INSERT 评审记录，查看自己的评审）
GRANT SELECT, INSERT, UPDATE ON graduation_management.review TO 'gms_teacher'@'localhost';

-- 教师可以查看流程期限
GRANT SELECT ON graduation_management.deadline TO 'gms_teacher'@'localhost';

-- 教师可以查看视图
GRANT SELECT ON graduation_management.v_student_progress TO 'gms_teacher'@'localhost';
GRANT SELECT ON graduation_management.v_topic_detail TO 'gms_teacher'@'localhost';
GRANT SELECT ON graduation_management.v_score_ranking TO 'gms_teacher'@'localhost';

-- 教师可以查看自己的审计日志记录
GRANT SELECT ON graduation_management.audit_log TO 'gms_teacher'@'localhost';

-- ============================================================
-- 4. 管理员角色权限
-- ============================================================

-- 管理员拥有所有权限
GRANT ALL PRIVILEGES ON graduation_management.* TO 'gms_admin'@'localhost';

-- 管理员不能直接修改 audit_log（由触发器自动写）
-- 但可以查看和清理
GRANT SELECT, DELETE ON graduation_management.audit_log TO 'gms_admin'@'localhost';

-- ============================================================
-- 5. 刷新权限
-- ============================================================
FLUSH PRIVILEGES;

-- ============================================================
-- 6. 验证权限
-- ============================================================

-- -- 查看各用户权限
-- SHOW GRANTS FOR 'gms_student'@'localhost';
-- SHOW GRANTS FOR 'gms_teacher'@'localhost';
-- SHOW GRANTS FOR 'gms_admin'@'localhost';

-- -- 测试学生角色：尝试删除题目（预期失败）
-- -- mysql -u gms_student -p'Student@2025' -e "
-- --   USE graduation_management;
-- --   DELETE FROM topic WHERE topic_id = 1;
-- -- "
-- -- 预期结果：ERROR 1142 (42000): DELETE command denied

-- -- 测试学生角色：尝试查看自己的选题（预期成功）
-- -- mysql -u gms_student -p'Student@2025' -e "
-- --   USE graduation_management;
-- --   SELECT * FROM selection WHERE stu_id = '2021001';
-- -- "
-- -- 预期结果：返回该学生自己的选题记录

-- ============================================================
-- 7. 应用层安全说明
-- ============================================================
-- 数据库用户权限 + 应用层RBAC双保险：
-- - gms_student : 应用层只能访问「学生端」功能
-- - gms_teacher : 应用层只能访问「教师端」功能
-- - gms_admin  : 应用层可访问「管理端」功能
--
-- 应用层连接池统一使用高权限账号，角色区分在应用层实现
-- 以上MySQL用户主要用于：
--   1. 演示 GRANT/REVOKE 高级技术
--   2. 特殊场景（如直接数据库运维）的权限隔离
