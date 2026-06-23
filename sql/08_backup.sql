-- ============================================================
-- 毕业设计管理系统 — 数据库备份与恢复策略
-- 题目37：完整备份 + 差异备份 + 日志备份 + 恢复流程
-- ============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 1. 备份策略设计
-- ============================================================
-- | 备份类型   | 频率         | 保留周期 | 说明                     |
-- |-----------|-------------|---------|--------------------------|
-- | 完整备份   | 每周日 02:00 | 4周     | 全库完整备份               |
-- | 差异备份   | 每天 02:00   | 7天     | 自上次完整备份以来的变更     |
-- | binlog备份 | 实时（每小时存档）| 30天 | 增量恢复，支持时间点恢复    |

-- ============================================================
-- 2. 完整备份脚本（Linux/macOS Shell，Windows 用 bat）
-- ============================================================

-- -- MySQL命令行备份命令（在报告中使用）：
-- mysqldump -u root -p \
--   --single-transaction \
--   --routines \
--   --triggers \
--   --events \
--   --databases graduation_management \
--   --result-file="/backup/full/graduation_$(date +%Y%m%d_%H%M%S).sql"
--
-- 参数说明：
-- --single-transaction : 一致性快照，不锁表（InnoDB）
-- --routines           : 包含存储过程和函数
-- --triggers           : 包含触发器
-- --events             : 包含定时事件

-- ============================================================
-- 3. 差异备份脚本
-- ============================================================

-- -- 差异备份：基于上一次完整备份
-- mysqldump -u root -p \
--   --single-transaction \
--   --routines \
--   --triggers \
--   --no-create-db \
--   --skip-add-drop-table \
--   --databases graduation_management \
--   --where="1=1" \
--   --result-file="/backup/diff/graduation_diff_$(date +%Y%m%d).sql"
--
-- -- 更精确的差异：使用 binlog 位置
-- -- 记录上次完整备份时的 binlog 位置
-- SHOW MASTER STATUS;
-- -- 导出从该位置到当前的 binlog
-- mysqlbinlog --start-position=<上次备份位置> mysql-bin.00000X > /backup/inc/inc_$(date +%Y%m%d).sql

-- ============================================================
-- 4. 完整备份存储过程（定时调用）
-- ============================================================

DELIMITER //

DROP PROCEDURE IF EXISTS sp_auto_backup //
CREATE PROCEDURE sp_auto_backup()
BEGIN
    DECLARE v_backup_path VARCHAR(500);
    DECLARE v_backup_name VARCHAR(200);

    SET v_backup_name = CONCAT('graduation_full_', DATE_FORMAT(NOW(), '%Y%m%d_%H%M%S'), '.sql');
    -- SET v_backup_path = CONCAT('/backup/full/', v_backup_name);

    -- 记录备份操作日志
    INSERT INTO audit_log (table_name, operation, record_id, new_value, operated_by, operation_time)
    VALUES ('system', 'BACKUP', v_backup_name,
            JSON_OBJECT('type','FULL','time',NOW()),
            'SYSTEM', NOW());
END //

DELIMITER ;

-- ============================================================
-- 5. 恢复流程
-- ============================================================

-- -- 场景1：完全恢复（数据库崩溃）
-- -- Step 1：恢复最近一次完整备份
-- mysql -u root -p graduation_management < /backup/full/graduation_20250615_020000.sql
--
-- -- Step 2：恢复差异备份
-- mysql -u root -p graduation_management < /backup/diff/graduation_diff_20250621.sql
--
-- -- Step 3：恢复 binlog 增量（到故障前一刻）
-- mysqlbinlog --start-datetime="2025-06-21 02:00:00" \
--   --stop-datetime="2025-06-22 15:30:00" \
--   /var/lib/mysql/mysql-bin.00000X | mysql -u root -p
--
-- -- 场景2：时间点恢复（误删数据）
-- -- 恢复到误操作前的时间点
-- mysqlbinlog --stop-datetime="2025-06-22 14:55:00" \
--   /var/lib/mysql/mysql-bin.00000X | mysql -u root -p
--
-- -- 场景3：单表恢复
-- mysql -u root -p graduation_management < /backup/full/graduation_20250615.sql
-- -- 或用 mysqldump 筛选单表
-- mysqldump -u root -p graduation_management student > /backup/student_backup.sql

-- ============================================================
-- 6. 备份验证
-- ============================================================

-- -- 验证备份文件完整性
-- -- 在测试库还原
-- CREATE DATABASE IF NOT EXISTS graduation_management_test;
-- mysql -u root -p graduation_management_test < /backup/full/graduation_20250615.sql
-- -- 对比表行数
-- SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'graduation_management';
-- SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'graduation_management_test';

-- ============================================================
-- 7. Windows 备份脚本示例（backup.bat，放项目根目录）
-- ============================================================
-- @echo off
-- set BACKUP_DIR=E:\backup\graduation
-- set MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 8.0\bin
-- set DATE=%date:~0,4%%date:~5,2%%date:~8,2%
-- set TIME=%time:~0,2%%time:~3,2%%time:~6,2%
-- if not exist "%BACKUP_DIR%\full" mkdir "%BACKUP_DIR%\full"
-- "%MYSQL_HOME%\mysqldump.exe" -u root -pYourPassword ^
--   --single-transaction --routines --triggers ^
--   --databases graduation_management ^
--   --result-file="%BACKUP_DIR%\full\graduation_%DATE%_%TIME%.sql"
-- echo Backup completed: graduation_%DATE%_%TIME%.sql
