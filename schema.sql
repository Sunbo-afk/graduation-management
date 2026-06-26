-- ============================================================
-- 毕业管理系统 数据库建表脚本
-- ============================================================

-- 1. 导师表
CREATE TABLE 导师 (
    工号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    姓名 VARCHAR(50) NOT NULL,
    性别 VARCHAR(4),
    职称 VARCHAR(30),
    研究方向 VARCHAR(200),
    联系电话 VARCHAR(20),
    邮箱 VARCHAR(100)
) COMMENT '导师信息';

-- 2. 学生表
CREATE TABLE 学生 (
    学号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    姓名 VARCHAR(50) NOT NULL,
    性别 VARCHAR(4),
    出生日期 DATE,
    班级 VARCHAR(50),
    专业 VARCHAR(50),
    入学年份 INT,
    导师工号 VARCHAR(20) COMMENT '外键，关联导师',
    FOREIGN KEY (导师工号) REFERENCES 导师(工号)
) COMMENT '学生信息';

-- 3. 毕业论文表
CREATE TABLE 毕业论文 (
    论文编号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    题目 VARCHAR(200) NOT NULL,
    开题日期 DATE,
    提交日期 DATE,
    状态 VARCHAR(20) COMMENT '开题/撰写中/已完成/答辩通过/答辩未通过',
    学生学号 VARCHAR(20) NOT NULL COMMENT '外键',
    导师工号 VARCHAR(20) NOT NULL COMMENT '外键',
    FOREIGN KEY (学生学号) REFERENCES 学生(学号),
    FOREIGN KEY (导师工号) REFERENCES 导师(工号)
) COMMENT '毕业论文';

-- 4. 答辩委员会表
CREATE TABLE 答辩委员会 (
    委员会编号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    主席 VARCHAR(50) NOT NULL,
    成员 TEXT COMMENT '多名成员，逗号分隔',
    秘书 VARCHAR(50),
    答辩日期 DATE,
    地点 VARCHAR(100)
) COMMENT '答辩委员会';

-- 5. 答辩表
CREATE TABLE 答辩 (
    答辩编号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    论文编号 VARCHAR(20) NOT NULL COMMENT '外键',
    委员会编号 VARCHAR(20) NOT NULL COMMENT '外键',
    答辩日期 DATE,
    成绩 VARCHAR(10) COMMENT '优秀/良好/中等/及格/不及格',
    评语 TEXT,
    FOREIGN KEY (论文编号) REFERENCES 毕业论文(论文编号),
    FOREIGN KEY (委员会编号) REFERENCES 答辩委员会(委员会编号)
) COMMENT '答辩记录';

-- 6. 毕业审核表
CREATE TABLE 毕业审核 (
    审核编号 VARCHAR(20) PRIMARY KEY COMMENT '主键',
    学生学号 VARCHAR(20) NOT NULL COMMENT '外键',
    学分修读情况 VARCHAR(20) COMMENT '已修满/未修满',
    学费缴纳状态 VARCHAR(20) COMMENT '已缴清/未缴清',
    审核状态 VARCHAR(20) COMMENT '待审核/审核通过/审核未通过',
    审核日期 DATE,
    审核意见 TEXT,
    FOREIGN KEY (学生学号) REFERENCES 学生(学号)
) COMMENT '毕业资格审核';
