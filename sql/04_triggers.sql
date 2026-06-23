-- ============================================================
-- 毕业设计管理系统 — 触发器脚本
-- 题目37：3个触发器
-- ============================================================
USE graduation_management;

DELIMITER //

-- ============================================================
-- 触发器1：选题后自动更新题目状态为"已选"
-- 时机：AFTER INSERT ON selection
-- 作用：学生选题后自动将 topic.status 设为 1（已选）
--       并检查导师是否已满，若满则拒绝后续选题
-- ============================================================
DROP TRIGGER IF EXISTS trg_selection_after_insert;
CREATE TRIGGER trg_selection_after_insert
AFTER INSERT ON selection
FOR EACH ROW
BEGIN
    DECLARE v_current_count INT;
    DECLARE v_max_select INT;

    -- 统计该题目当前已选人数
    SELECT COUNT(*) INTO v_current_count
    FROM selection WHERE topic_id = NEW.topic_id;

    -- 获取题目最大可选人数
    SELECT max_select INTO v_max_select
    FROM topic WHERE topic_id = NEW.topic_id;

    -- 若选满则更新状态为已选
    IF v_current_count >= v_max_select THEN
        UPDATE topic SET status = 1 WHERE topic_id = NEW.topic_id;
    END IF;
END //

-- ============================================================
-- 触发器2：文档提交/更新时自动记录审计日志
-- 时机：AFTER INSERT ON submission
-- 作用：将每次文档提交写入 audit_log 表
-- ============================================================
DROP TRIGGER IF EXISTS trg_submission_audit_log;
CREATE TRIGGER trg_submission_audit_log
AFTER INSERT ON submission
FOR EACH ROW
BEGIN
    DECLARE v_stu_id VARCHAR(20);

    -- 通过 selection 关联找到学生学号
    SELECT stu_id INTO v_stu_id
    FROM selection WHERE selection_id = NEW.selection_id;

    -- 写入审计日志
    INSERT INTO audit_log (table_name, operation, record_id, new_value, operated_by, operation_time)
    VALUES (
        'submission',
        'INSERT',
        NEW.submission_id,
        JSON_OBJECT(
            'selection_id', NEW.selection_id,
            'stage', NEW.stage,
            'file_path', NEW.file_path,
            'status', NEW.status,
            'version', NEW.version
        ),
        v_stu_id,
        NOW()
    );
END //

-- ============================================================
-- 触发器3：审阅评分后自动更新提交状态并重算综合成绩
-- 时机：AFTER INSERT ON review
-- 作用：教师评分后自动更新 submission 状态（≥60通过，<60需修改）
--       同时调用存储过程重新计算最终成绩
-- ============================================================
DROP TRIGGER IF EXISTS trg_review_after_insert;
CREATE TRIGGER trg_review_after_insert
AFTER INSERT ON review
FOR EACH ROW
BEGIN
    DECLARE v_selection_id INT;
    DECLARE v_stu_id VARCHAR(20);
    DECLARE v_new_status VARCHAR(20);

    -- 根据分数确定审阅结果
    IF NEW.score >= 60 THEN
        SET v_new_status = '已通过';
    ELSE
        SET v_new_status = '需修改';
    END IF;

    -- 更新提交状态
    UPDATE submission SET status = v_new_status
    WHERE submission_id = NEW.submission_id;

    -- 查找对应的选题记录和学生
    SELECT sel.selection_id, sel.stu_id INTO v_selection_id, v_stu_id
    FROM submission sub
    JOIN selection sel ON sub.selection_id = sel.selection_id
    WHERE sub.submission_id = NEW.submission_id;

    -- 调用存储过程重新计算综��成绩（通过函数方式实现）
    -- 注意：MySQL 触发器中不能直接 CALL 有 OUT 参数的过程，
    -- 这里内联计算
    UPDATE selection SET final_score = (
        SELECT
            COALESCE(
                (SELECT r.score FROM submission sub
                 LEFT JOIN review r ON sub.submission_id = r.submission_id
                 WHERE sub.selection_id = v_selection_id AND sub.stage = '开题报告'
                 ORDER BY sub.version DESC LIMIT 1), 0) * 0.15 +
            COALESCE(
                (SELECT r.score FROM submission sub
                 LEFT JOIN review r ON sub.submission_id = r.submission_id
                 WHERE sub.selection_id = v_selection_id AND sub.stage = '中期检查'
                 ORDER BY sub.version DESC LIMIT 1), 0) * 0.15 +
            COALESCE(
                (SELECT r.score FROM submission sub
                 LEFT JOIN review r ON sub.submission_id = r.submission_id
                 WHERE sub.selection_id = v_selection_id AND sub.stage = '初稿'
                 ORDER BY sub.version DESC LIMIT 1), 0) * 0.30 +
            COALESCE(
                (SELECT r.score FROM submission sub
                 LEFT JOIN review r ON sub.submission_id = r.submission_id
                 WHERE sub.selection_id = v_selection_id AND sub.stage = '终稿'
                 ORDER BY sub.version DESC LIMIT 1), 0) * 0.40
    )
    WHERE selection_id = v_selection_id;

    -- 记录审计日志
    INSERT INTO audit_log (table_name, operation, record_id, new_value, operated_by, operation_time)
    VALUES (
        'review',
        'INSERT',
        NEW.review_id,
        JSON_OBJECT(
            'submission_id', NEW.submission_id,
            'score', NEW.score,
            'teacher_id', NEW.teacher_id,
            'status', v_new_status
        ),
        NEW.teacher_id,
        NOW()
    );
END //

DELIMITER ;

-- ============================================================
-- 验证触发器效果（用于报告截图）
-- ============================================================

-- -- 测试触发器1：学生选题后查看 topic 状态变化
-- INSERT INTO selection (stu_id, topic_id) VALUES ('2021016', 2);
-- SELECT topic_id, title, status FROM topic WHERE topic_id = 2;
-- -- 预期：status 从 0 变为 1

-- -- 测试触发器2：提交文档后查看 audit_log
-- INSERT INTO submission (selection_id, stage, file_path, description)
-- VALUES (1, '初稿', '/files/2025/2021001/初稿_v1.pdf', '测试提交');
-- SELECT * FROM audit_log WHERE table_name = 'submission' ORDER BY log_id DESC LIMIT 1;

-- -- 测试触发器3：教师评分后查看 submission 状态变化
-- INSERT INTO review (submission_id, teacher_id, score, comment)
-- VALUES (LAST_INSERT_ID(), 'T001', 85, '内容完整，格式规范');
-- SELECT status FROM submission WHERE submission_id = LAST_INSERT_ID();
