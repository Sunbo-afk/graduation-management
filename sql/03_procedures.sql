-- ============================================================
-- 毕业设计管理系统 — 存储过程脚本
-- 题目37：2个存储过程（均带 IN/OUT 参数）
-- ============================================================
USE graduation_management;

DELIMITER //

-- ============================================================
-- 存储过程1：计算学生最终综合成绩
-- 输入：学生学号
-- 输出：综合成绩（加权：开题15% + 中期15% + 初稿30% + 终稿40%）
--       各阶段成绩列表
--       完成状态
-- ============================================================
DROP PROCEDURE IF EXISTS calc_final_score;
CREATE PROCEDURE calc_final_score(
    IN  p_stu_id    VARCHAR(20),
    OUT p_final_score DECIMAL(5,2),
    OUT p_status    VARCHAR(50),
    OUT p_detail    VARCHAR(500)
)
BEGIN
    DECLARE v_selection_id   INT;
    DECLARE v_score_kaiti    DECIMAL(5,2) DEFAULT NULL;
    DECLARE v_score_zhongqi  DECIMAL(5,2) DEFAULT NULL;
    DECLARE v_score_chugao   DECIMAL(5,2) DEFAULT NULL;
    DECLARE v_score_zhonggao DECIMAL(5,2) DEFAULT NULL;
    DECLARE v_kaiti_status   VARCHAR(20) DEFAULT '未提交';
    DECLARE v_zhongqi_status VARCHAR(20) DEFAULT '未提交';
    DECLARE v_chugao_status  VARCHAR(20) DEFAULT '未提交';
    DECLARE v_zhonggao_status VARCHAR(20) DEFAULT '未提交';

    -- 查找选题记录
    SELECT selection_id INTO v_selection_id
    FROM selection WHERE stu_id = p_stu_id;

    IF v_selection_id IS NULL THEN
        SET p_final_score = NULL;
        SET p_status = '未选题';
        SET p_detail = '该学生尚未选择毕业设计题目';
    ELSE
        -- 获取各阶段最新提交的成绩
        SELECT r.score, sub.status INTO v_score_kaiti, v_kaiti_status
        FROM submission sub
        LEFT JOIN review r ON sub.submission_id = r.submission_id
        WHERE sub.selection_id = v_selection_id AND sub.stage = '开题报告'
        ORDER BY sub.version DESC LIMIT 1;

        SELECT r.score, sub.status INTO v_score_zhongqi, v_zhongqi_status
        FROM submission sub
        LEFT JOIN review r ON sub.submission_id = r.submission_id
        WHERE sub.selection_id = v_selection_id AND sub.stage = '中期检查'
        ORDER BY sub.version DESC LIMIT 1;

        SELECT r.score, sub.status INTO v_score_chugao, v_chugao_status
        FROM submission sub
        LEFT JOIN review r ON sub.submission_id = r.submission_id
        WHERE sub.selection_id = v_selection_id AND sub.stage = '初稿'
        ORDER BY sub.version DESC LIMIT 1;

        SELECT r.score, sub.status INTO v_score_zhonggao, v_zhonggao_status
        FROM submission sub
        LEFT JOIN review r ON sub.submission_id = r.submission_id
        WHERE sub.selection_id = v_selection_id AND sub.stage = '终稿'
        ORDER BY sub.version DESC LIMIT 1;

        -- 加权计算最终成绩
        SET p_final_score =
            COALESCE(v_score_kaiti, 0) * 0.15 +
            COALESCE(v_score_zhongqi, 0) * 0.15 +
            COALESCE(v_score_chugao, 0) * 0.30 +
            COALESCE(v_score_zhonggao, 0) * 0.40;

        -- 判断状态
        IF v_zhonggao_status = '已通过' THEN
            SET p_status = '已完成';
        ELSEIF v_zhonggao_status = '待审阅' OR v_zhonggao_status = '需修改' THEN
            SET p_status = '终稿审阅中';
        ELSEIF v_chugao_status = '已通过' OR v_chugao_status = '待审阅' THEN
            SET p_status = '论文撰写中';
        ELSEIF v_zhongqi_status = '已通过' OR v_zhongqi_status = '待审阅' THEN
            SET p_status = '中期阶段';
        ELSEIF v_kaiti_status = '已通过' OR v_kaiti_status = '待审阅' THEN
            SET p_status = '开题阶段';
        ELSE
            SET p_status = '已选题';
        END IF;

        -- 生成详情描述
        SET p_detail = CONCAT(
            '开题报告: ', COALESCE(v_kaiti_status, '未提交'), ' (', COALESCE(v_score_kaiti, 0), '分×15%)',
            ' | 中期检查: ', COALESCE(v_zhongqi_status, '未提交'), ' (', COALESCE(v_score_zhongqi, 0), '分×15%)',
            ' | 初稿: ', COALESCE(v_chugao_status, '未提交'), ' (', COALESCE(v_score_chugao, 0), '分×30%)',
            ' | 终稿: ', COALESCE(v_zhonggao_status, '未提交'), ' (', COALESCE(v_score_zhonggao, 0), '分×40%)',
            ' | 综合成绩: ', ROUND(p_final_score, 2), '分'
        );

        -- 更新 selection 表的 final_score
        UPDATE selection SET final_score = ROUND(p_final_score, 2)
        WHERE selection_id = v_selection_id;
    END IF;
END //

-- ============================================================
-- 存储过程2：统计某系毕业设计完成情况
-- 输入：系ID（NULL 表示全统计）
-- 输出：总人数、已完成人数、各阶段人数
-- ============================================================
DROP PROCEDURE IF EXISTS stat_dept_completion;
CREATE PROCEDURE stat_dept_completion(
    IN  p_dept_id    INT,
    OUT p_total_stu  INT,
    OUT p_completed  INT,
    OUT p_no_select  INT,
    OUT p_report     VARCHAR(1000)
)
BEGIN
    -- 总人数（该系所有学生）
    SELECT COUNT(*) INTO p_total_stu
    FROM student s
    JOIN class c ON s.class_id = c.class_id
    JOIN major m ON c.major_id = m.major_id
    WHERE (p_dept_id IS NULL OR m.dept_id = p_dept_id);

    -- 已完成人数（终稿已通过）
    SELECT COUNT(DISTINCT s.stu_id) INTO p_completed
    FROM student s
    JOIN class c      ON s.class_id = c.class_id
    JOIN major m      ON c.major_id = m.major_id
    JOIN selection sel ON s.stu_id = sel.stu_id
    WHERE (p_dept_id IS NULL OR m.dept_id = p_dept_id)
      AND sel.status = '已完成';

    -- 未选题人数
    SELECT COUNT(*) INTO p_no_select
    FROM student s
    JOIN class c ON s.class_id = c.class_id
    JOIN major m ON c.major_id = m.major_id
    WHERE (p_dept_id IS NULL OR m.dept_id = p_dept_id)
      AND s.stu_id NOT IN (SELECT stu_id FROM selection);

    -- 生成统计报告
    SET p_report = CONCAT(
        '毕业设计完成情况统计',
        ' | 总人数: ', p_total_stu,
        ' | 已完成: ', p_completed,
        ' | 未选题: ', p_no_select,
        ' | 进行中: ', (p_total_stu - p_completed - p_no_select),
        ' | 完成率: ', ROUND(p_completed / NULLIF(p_total_stu, 0) * 100, 2), '%'
    );
END //

DELIMITER ;

-- ============================================================
-- 存储过程调用示例（用于报告截图）
-- ============================================================

-- -- 调用存储过程1：计算学号为2021006的学生的最终成绩
-- CALL calc_final_score('2021006', @score, @status, @detail);
-- SELECT @score AS 综合成绩, @status AS 状态, @detail AS 详情;

-- -- 调用存储过程2：统计计算机系（dept_id=1）完成情况
-- CALL stat_dept_completion(1, @total, @done, @unsel, @rpt);
-- SELECT @total AS 总人数, @done AS 已完成, @unsel AS 未选题, @rpt AS 报告;
