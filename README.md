# 毕业设计管理系统

数据库应用课程设计小组项目。项目使用 JSP、Servlet、JDBC、DAO、Maven、Tomcat 和 MySQL 8 实现，前端只做基本表单、按钮和列表展示，重点放在数据库设计和高阶数据库技术。

## 运行步骤

1. 在 MySQL Workbench 中执行 `sql/init.sql`。
2. 再执行 `sql/seed_users.sql`，插入最小账号和基础数据。
3. 用 IntelliJ IDEA 打开本项目。
4. 检查 `src/main/java/db/DB.java` 中的数据库用户名和密码。
5. 配置 Tomcat 10，部署项目。
6. 访问 `http://localhost:8080/graduation-system/`。

常用维护脚本：

- `sql/clear_data.sql`：清空表数据，保留表结构。
- `sql/fix_statistics.sql`：修复教师指导人数统计和成绩排名逻辑。
- `sql/fix_submission_data_only.sql`：清理“未选题却已提交”的错误数据。
- `sql/fix_submission_rule.sql`：增加数据库触发器，限制未选题学生不能提交材料。如果本机 MySQL 创建触发器报 Error Code 7，可以不执行该脚本，因为 Java 后端已有同样校验。

## 初始账号

`seed_users.sql` 会创建三个账号：

- 管理员：`admin / 123456`
- 教师：`teacher1 / 123456`
- 学生：`student1 / 123456`

初始关系：

- `teacher1` 是 `student1` 的指导教师。
- 课题名称为 `project1`。
- `student1` 已选择 `project1`。
- `student1` 没有任何阶段提交。

管理员新增学生时，系统会自动创建学生账号：

- 用户名：学生姓名
- 初始密码：`123456`
- 角色：`student`

例如新增学生姓名为 `student2`，则登录账号为：

```text
student2 / 123456
```

管理员新增教师时，系统会自动创建教师账号：

- 用户名：教师姓名
- 初始密码：`123456`
- 角色：`teacher`

例如新增教师姓名为 `teacher2`，则登录账号为：

```text
teacher2 / 123456
```

学生和教师登录后可以进入“修改密码”页面修改自己的密码。

## 角色权限

管理员：

- 毕设管理
- 学生管理
- 教师管理
- 题目管理
- 公告管理
- 查询统计

教师：

- 题目管理
- 阶段提交查看
- 教师评阅
- 查询统计
- 修改密码

学生：

- 首页查看公告
- 题目选择
- 阶段提交
- 查询自己的学生进度
- 修改密码

学生端不显示“公告管理”，公告只在首页展示。学生端查询统计中不会显示审计日志 `operation_log`。

## 主要功能

- 登录与角色区分
- 管理员为指导教师分配毕业生
- 管理员设置各阶段提交期限
- 管理员发布毕业设计公告和要求
- 管理员新增学生并自动生成账号
- 管理员新增教师并自动生成账号
- 教师发布毕业设计题目
- 学生选择题目
- 学生提交阶段材料
- 学生上传 PDF、DOC、DOCX 文件
- 教师在评阅页面打开学生上传的文件
- 教师评阅提交内容并打分
- 查询统计学生进度、教师指导情况、成绩排名
- 学生和教师修改密码

## 高阶数据库技术

本项目实现了以下高阶数据库技术，满足课程设计“至少 5 项高阶数据库技术”的要求。

### 1. 存储过程

实现位置：`sql/init.sql`

使用的存储过程：

- `sp_select_topic`
- `sp_calc_final_score`

业务作用：

- `sp_select_topic` 用于学生选题。它会检查学生是否已经选题、题目人数是否已满，然后再插入选题记录。
- `sp_calc_final_score` 用于计算学生最终成绩。只有开题、中期、终稿三个阶段都完成评阅后，才计算总成绩。

验证方式：

- 学生选择题目时调用 `sp_select_topic`。
- 查询统计页面点击“调用存储过程计算总成绩”时调用 `sp_calc_final_score`。

### 2. 触发器

实现位置：`sql/init.sql`

使用的触发器：

- `trg_submission_insert_log`
- `trg_review_insert_log`
- `trg_deadline_update_log`
- `trg_submission_check_topic`

业务作用：

- 学生提交阶段材料后，自动写入 `operation_log`。
- 教师提交评阅后，自动写入 `operation_log`。
- 管理员修改阶段截止时间后，自动写入 `operation_log`。
- `trg_submission_check_topic` 用于限制未选题学生不能直接向 `submission` 表插入提交记录。

验证方式：

- 学生提交材料后，管理员或教师在查询统计页面查看审计日志。
- 教师评阅后，查看 `operation_log` 是否产生记录。
- 修改阶段期限后，查看 `operation_log` 是否产生记录。

### 3. 视图

实现位置：`sql/init.sql`

使用的视图：

- `v_student_progress`
- `v_teacher_guidance`
- `v_score_rank`

业务作用：

- `v_student_progress` 汇总学生、班级、专业、系、题目、指导教师、提交阶段、评阅成绩和评语。
- `v_teacher_guidance` 统计每个教师指导的学生人数和平均成绩。
- `v_score_rank` 使用成绩汇总表生成学生成绩排名。

验证方式：

- 查询统计页面展示“学生进度表”“教师指导统计表”“成绩排名表”。
- 学生只能看到自己的学生进度；管理员和教师可以看到整体统计。

### 4. 索引优化

实现位置：`sql/init.sql`

使用的索引：

- `idx_student_class`
- `idx_teacher_department`
- `idx_topic_teacher`
- `idx_selection_topic`
- `idx_submission_student_stage`
- `idx_review_teacher`
- `idx_log_time`
- `ft_topic_title_desc`

业务作用：

- 提高学生、教师、题目、选题、提交、评阅和日志查询速度。
- `ft_topic_title_desc` 是全文索引，用于题目标题和描述的关键词检索。

验证方式：

- 可以使用 `EXPLAIN` 对比常用查询在有索引时的执行计划。
- 可以对 `graduation_topic` 的标题和描述进行全文检索测试。

### 5. 事务与并发控制

实现位置：`sp_select_topic`

使用技术：

- `start transaction`
- `for update`
- `commit`
- `rollback`

业务作用：

学生选题时必须保证题目人数不超过上限。如果多个学生同时选择同一个题目，系统通过事务和 `for update` 锁定题目记录，防止超额选题。

验证方式：

- 给某个题目设置人数上限。
- 多次执行选题。
- 当人数已满时，存储过程返回 `topic is full`，不会插入新的选题记录。

### 6. 窗口函数

实现位置：`v_score_rank`

使用技术：

```sql
rank() over(partition by d.department_id order by ss.total_score desc)
```

业务作用：

按系对学生总成绩进行排名。

验证方式：

- 三个阶段都完成评阅后，调用成绩计算存储过程。
- 查询统计页面查看“成绩排名表”。

### 7. 审计日志

实现位置：

- `operation_log` 表
- `trg_submission_insert_log`
- `trg_review_insert_log`
- `trg_deadline_update_log`

业务作用：

记录关键数据操作，包括：

- 学生提交阶段材料
- 教师评阅
- 管理员修改阶段截止时间

验证方式：

- 管理员或教师进入查询统计页面，可以看到审计日志。
- 学生端不会显示审计日志。

### 8. 全文检索

实现位置：`sql/init.sql`

使用索引：

```sql
create fulltext index ft_topic_title_desc on graduation_topic(topic_title, topic_desc);
```

业务作用：

支持对毕业设计题目标题和描述进行关键词检索。

验证方式：

可以在 MySQL 中执行类似查询：

```sql
select *
from graduation_topic
where match(topic_title, topic_desc) against('project');
```

## 重要业务规则

1. 学生必须先选题，才能提交阶段材料。
2. 同一学生同一阶段只能提交一次。
3. 题目选择人数不能超过题目的 `max_students`。
4. 成绩排名表只显示三个阶段都完成评阅的学生。
5. 学生只能查看自己的学生进度。
6. 学生端不能查看审计日志。
7. 管理员新增学生后自动生成账号，初始密码为 `123456`。
