-- ============================================================
-- 毕业设计管理系统 — 窗口函数示例脚本
-- 题目37：RANK / DENSE_RANK / ROW_NUMBER / PERCENT_RANK
-- ============================================================
SET NAMES utf8mb4;
USE graduation_management;

-- ============================================================
-- 1. 按系对学生成绩排名（DENSE_RANK + PARTITION BY）
-- ============================================================
SELECT
    d.dept_name             AS 系别,
    m.major_name            AS 专业,
    s.stu_name              AS 姓名,
    sel.final_score         AS 成绩,
    DENSE_RANK() OVER (
        PARTITION BY d.dept_id
        ORDER BY sel.final_score DESC
    )                       AS 系内排名,
    DENSE_RANK() OVER (
        PARTITION BY m.major_id
        ORDER BY sel.final_score DESC
    )                       AS 专业内排名
FROM student s
JOIN selection sel ON s.stu_id = sel.stu_id
JOIN class c      ON s.class_id = c.class_id
JOIN major m      ON c.major_id = m.major_id
JOIN department d ON m.dept_id = d.dept_id
WHERE sel.final_score IS NOT NULL
ORDER BY d.dept_id, m.major_id, sel.final_score DESC;

-- ============================================================
-- 2. 教师指导学生数排名（RANK）
-- ============================================================
SELECT
    tea.teacher_name            AS 教师姓名,
    tea.title                   AS 职称,
    d.dept_name                 AS 系别,
    COUNT(sel.selection_id)     AS 指导学生数,
    RANK() OVER (
        ORDER BY COUNT(sel.selection_id) DESC
    )                           AS 指导学生数排名,
    GROUP_CONCAT(s.stu_name ORDER BY s.stu_name SEPARATOR ', ') AS 指导学生名单
FROM teacher tea
JOIN department d ON tea.dept_id = d.dept_id
LEFT JOIN topic t ON tea.teacher_id = t.teacher_id
LEFT JOIN selection sel ON t.topic_id = sel.topic_id
LEFT JOIN student s ON sel.stu_id = s.stu_id
GROUP BY tea.teacher_id, tea.teacher_name, tea.title, d.dept_name
ORDER BY 指导学生数 DESC;

-- ============================================================
-- 3. 各阶段通过率统计（PERCENT_RANK + 分区）
-- ============================================================
SELECT
    sub.stage                   AS 阶段,
    COUNT(*)                    AS 提交总数,
    SUM(CASE WHEN sub.status = '已通过' THEN 1 ELSE 0 END) AS 通过数,
    SUM(CASE WHEN sub.status = '需修改' THEN 1 ELSE 0 END) AS 需修改数,
    SUM(CASE WHEN sub.status = '待审阅' THEN 1 ELSE 0 END) AS 待审阅数,
    ROUND(
        SUM(CASE WHEN sub.status = '已通过' THEN 1 ELSE 0 END) / COUNT(*) * 100, 2
    )                           AS 通过率百分比
FROM submission sub
GROUP BY sub.stage
ORDER BY FIELD(sub.stage, '开题报告', '中期检查', '初稿', '终稿');

-- ============================================================
-- 4. 每个专业成绩最高/最低学生（ROW_NUMBER + 子查询）
-- ============================================================
SELECT
    专业,
    姓名,
    成绩,
    CASE WHEN rn_asc = 1 THEN '最低分' WHEN rn_desc = 1 THEN '最高分' END AS 标识
FROM (
    SELECT
        m.major_name            AS 专业,
        s.stu_name              AS 姓名,
        sel.final_score         AS 成绩,
        ROW_NUMBER() OVER (PARTITION BY m.major_id ORDER BY sel.final_score DESC) AS rn_desc,
        ROW_NUMBER() OVER (PARTITION BY m.major_id ORDER BY sel.final_score ASC)  AS rn_asc
    FROM selection sel
    JOIN student s ON sel.stu_id = s.stu_id
    JOIN class c  ON s.class_id = c.class_id
    JOIN major m  ON c.major_id = m.major_id
    WHERE sel.final_score IS NOT NULL
) t
WHERE rn_desc = 1 OR rn_asc = 1
ORDER BY 专业, 成绩 DESC;

-- ============================================================
-- 5. 成绩分段统计（CUME_DIST — 累积分布）
-- ============================================================
SELECT
    s.stu_name                          AS 姓名,
    sel.final_score                     AS 成绩,
    ROUND(
        CUME_DIST() OVER (ORDER BY sel.final_score DESC), 4
    )                                   AS 超过百分比,  -- 超过xx%的同学
    CASE
        WHEN sel.final_score >= 90 THEN '优秀'
        WHEN sel.final_score >= 80 THEN '良好'
        WHEN sel.final_score >= 70 THEN '中等'
        WHEN sel.final_score >= 60 THEN '及格'
        ELSE '不及格'
    END                                 AS 成绩等级
FROM selection sel
JOIN student s ON sel.stu_id = s.stu_id
WHERE sel.final_score IS NOT NULL
ORDER BY sel.final_score DESC;

-- ============================================================
-- 6. 最近一次提交序号（LAG 函数获取上一次提交时间）
-- ============================================================
SELECT
    s.stu_name                  AS 姓名,
    sub.stage                   AS 阶段,
    sub.submit_time             AS 提交时间,
    sub.version                 AS 版本号,
    LAG(sub.submit_time, 1) OVER (
        PARTITION BY sub.selection_id, sub.stage
        ORDER BY sub.version
    )                           AS 上次提交时间,
    CASE
        WHEN LAG(sub.submit_time, 1) OVER (
            PARTITION BY sub.selection_id, sub.stage
            ORDER BY sub.version
        ) IS NOT NULL
        THEN CONCAT('距上次提交 ', TIMESTAMPDIFF(HOUR,
            LAG(sub.submit_time, 1) OVER (
                PARTITION BY sub.selection_id, sub.stage ORDER BY sub.version
            ),
            sub.submit_time), ' 小时')
        ELSE '首次提交'
    END                         AS 提交间隔
FROM submission sub
JOIN selection sel ON sub.selection_id = sel.selection_id
JOIN student s    ON sel.stu_id = s.stu_id
ORDER BY s.stu_name, sub.stage, sub.version;

-- ============================================================
-- 7. 各系毕业设计完成进度对比
-- ============================================================
SELECT
    d.dept_name                                 AS 系别,
    COUNT(DISTINCT s.stu_id)                    AS 学生总数,
    COUNT(DISTINCT sel.stu_id)                  AS 已选题数,
    COUNT(DISTINCT CASE WHEN sel.status = '已完成' THEN sel.stu_id END) AS 已完成数,
    ROUND(
        COUNT(DISTINCT CASE WHEN sel.status = '已完成' THEN sel.stu_id END)
        / COUNT(DISTINCT s.stu_id) * 100, 2
    )                                           AS 完成率,
    ROUND(AVG(sel.final_score), 2)              AS 平均成绩,
    SUM(COUNT(DISTINCT s.stu_id)) OVER ()       AS 全校学生总数,
    ROUND(
        COUNT(DISTINCT s.stu_id) / SUM(COUNT(DISTINCT s.stu_id)) OVER () * 100, 2
    )                                           AS 学生占比
FROM department d
JOIN major m    ON d.dept_id = m.dept_id
JOIN class c    ON m.major_id = c.major_id
JOIN student s  ON c.class_id = s.class_id
LEFT JOIN selection sel ON s.stu_id = sel.stu_id
GROUP BY d.dept_id, d.dept_name
ORDER BY 完成率 DESC;
