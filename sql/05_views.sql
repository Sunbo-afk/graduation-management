-- ============================================================
-- 毕业设计管理系统 — 视图脚本
-- 题目37：3个视图
-- ============================================================
SET NAMES utf8mb4;
USE graduation_management;

-- -------------------------------------------------------
-- 视图1：学生毕业设计进度总览
-- 用途：教师和管理员查看每个学生的完整进度（学生信息+选题+各阶段提交状态+成绩）
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_student_progress AS
SELECT
    s.stu_id                                    AS 学号,
    s.stu_name                                  AS 姓名,
    d.dept_name                                 AS 系别,
    m.major_name                                AS 专业,
    c.class_name                                AS 班级,
    c.grade                                     AS 年级,
    t.title                                     AS 课题名称,
    tea.teacher_name                            AS 指导教师,
    sel.status                                  AS 毕业设计状态,
    -- 各阶段提交情况
    (SELECT status FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '开题报告' ORDER BY sub.version DESC LIMIT 1) AS 开题报告状态,
    (SELECT MAX(submit_time) FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '开题报告') AS 开题报告提交时间,
    (SELECT score FROM submission sub LEFT JOIN review r ON sub.submission_id = r.submission_id WHERE sub.selection_id = sel.selection_id AND sub.stage = '开题报告' ORDER BY sub.version DESC LIMIT 1) AS 开题报告成绩,
    (SELECT status FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '中期检查' ORDER BY sub.version DESC LIMIT 1) AS 中期检查状态,
    (SELECT MAX(submit_time) FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '中期检查') AS 中期检查提交时间,
    (SELECT score FROM submission sub LEFT JOIN review r ON sub.submission_id = r.submission_id WHERE sub.selection_id = sel.selection_id AND sub.stage = '中期检查' ORDER BY sub.version DESC LIMIT 1) AS 中期检查成绩,
    (SELECT status FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '初稿' ORDER BY sub.version DESC LIMIT 1) AS 初稿状态,
    (SELECT MAX(submit_time) FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '初稿') AS 初稿提交时间,
    (SELECT score FROM submission sub LEFT JOIN review r ON sub.submission_id = r.submission_id WHERE sub.selection_id = sel.selection_id AND sub.stage = '初稿' ORDER BY sub.version DESC LIMIT 1) AS 初稿成绩,
    (SELECT status FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '终稿' ORDER BY sub.version DESC LIMIT 1) AS 终稿状态,
    (SELECT MAX(submit_time) FROM submission sub WHERE sub.selection_id = sel.selection_id AND sub.stage = '终稿') AS 终稿提交时间,
    (SELECT score FROM submission sub LEFT JOIN review r ON sub.submission_id = r.submission_id WHERE sub.selection_id = sel.selection_id AND sub.stage = '终稿' ORDER BY sub.version DESC LIMIT 1) AS 终稿成绩,
    sel.final_score                             AS 综合成绩
FROM student s
JOIN class c      ON s.class_id = c.class_id
JOIN major m      ON c.major_id = m.major_id
JOIN department d ON m.dept_id = d.dept_id
LEFT JOIN selection sel ON s.stu_id = sel.stu_id
LEFT JOIN topic t      ON sel.topic_id = t.topic_id
LEFT JOIN teacher tea  ON t.teacher_id = tea.teacher_id
ORDER BY d.dept_id, m.major_id, c.class_id, s.stu_id;

-- -------------------------------------------------------
-- 视图2：选题详情（含学生、题目、教师完整信息）
-- 用途：查看选题的完整相关信息，隐藏学生密码等敏感信息
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_topic_detail AS
SELECT
    t.topic_id          AS 题目编号,
    t.title             AS 题目名称,
    t.description       AS 题目描述,
    t.direction         AS 研究方向,
    CASE t.status
        WHEN 0 THEN '可选'
        WHEN 1 THEN '已选'
    END                 AS 题目状态,
    t.max_select        AS 可选人数,
    tea.teacher_id      AS 教师工号,
    tea.teacher_name    AS 指导教师,
    tea.title           AS 教师职称,
    tea.research_direction AS 教师研究方向,
    tea.email           AS 教师邮箱,
    s.stu_id            AS 学生学号,
    s.stu_name          AS 学生姓名,
    c.class_name        AS 班级,
    m.major_name        AS 专业,
    sel.select_time     AS 选题时间,
    sel.status          AS 毕设状态,
    sel.final_score     AS 最终成绩
FROM topic t
JOIN teacher tea ON t.teacher_id = tea.teacher_id
LEFT JOIN selection sel ON t.topic_id = sel.topic_id
LEFT JOIN student s    ON sel.stu_id = s.stu_id
LEFT JOIN class c      ON s.class_id = c.class_id
LEFT JOIN major m      ON c.major_id = m.major_id;

-- -------------------------------------------------------
-- 视图3：成绩排名视图（为窗口函数查询提供基础）
-- 用途：按系、专业统计毕业设计成绩排名
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_score_ranking AS
SELECT
    s.stu_id            AS 学号,
    s.stu_name          AS 姓名,
    d.dept_name         AS 系别,
    m.major_name        AS 专业,
    c.class_name        AS 班级,
    t.title             AS 课题名称,
    tea.teacher_name    AS 指导教师,
    sel.final_score     AS 最终成绩,
    sel.status          AS 毕设状态
FROM student s
JOIN class c      ON s.class_id = c.class_id
JOIN major m      ON c.major_id = m.major_id
JOIN department d ON m.dept_id = d.dept_id
JOIN selection sel ON s.stu_id = sel.stu_id
JOIN topic t      ON sel.topic_id = t.topic_id
JOIN teacher tea  ON t.teacher_id = tea.teacher_id;
