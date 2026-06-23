# 毕业设计管理系统

> 数据库课程设计 · 题目 37 | Spring Boot + MyBatis-Plus + MySQL 8.0 + Thymeleaf + Bootstrap 5

毕业设计全流程管理系统，支持**管理员 / 教师 / 学生**三种角色，覆盖选题、提交、审阅、评分、统计的完整生命周期。

---

## 功能概览

| 角色 | 功能 |
|------|------|
| **管理员** | 院系/专业/班级 CRUD、用户管理、密码重置、流程期限设置、统计分析看板、数据库备份 |
| **教师** | 发布/管理课题、查看指导学生、审阅各阶段文档并打分、统计分析 |
| **学生** | 浏览课题、在线选题、提交各阶段文档（开题/中期/初稿/终稿）、查看评语与成绩、自助注册 |

---

## 快速开始

### 环境要求

- JDK 8+（实际使用 JDK 21）
- MySQL 8.0
- Maven 3.6+

### 1. 创建数据库并导入脚本

```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_data.sql
```

### 2. 修改数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/graduation_management?...
    username: root
    password: 你的密码
```

### 3. 启动应用

```bash
mvn clean package -DskipTests
java -jar target/graduation-management-1.0.0.jar
```

访问 http://localhost:8080/login，默认密码 `123456`。

### 4. 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | `admin` | 123456 |
| 教师 | `T001` ~ `T010` | 123456 |
| 学生 | `2021001` ~ `2021030` | 123456 |

---

## 数据库设计

### ER 模型

```
Department(系) ──1:N── Major(专业) ──1:N── Class(班级) ──1:N── Student(学生)
Department(系) ──1:N── Teacher(教师)
Teacher(教师) ──1:N── Topic(题目)
Student(学生) ──1:1── Selection(选题) ──N:1── Topic(题目)
Selection(选题) ──1:N── Submission(提交) ──1:1── Review(审阅)
```

辅助实体：`Deadline`（流程期限）、`AuditLog`（审计日志，按年分区）、`SysUser`（管理员）

### 12 张数据表

| # | 表名 | 说明 | 关键约束 |
|---|------|------|----------|
| 1 | `department` | 院系 | dept_name UNIQUE |
| 2 | `major` | 专业 | FK→department，UNIQUE(name, dept) |
| 3 | `class` | 班级 | FK→major，UNIQUE(name, major) |
| 4 | `student` | 学生 | PK=学号，FK→class，CHECK(phone/email) |
| 5 | `teacher` | 教师 | PK=工号，FK→dept，CHECK(max_students) |
| 6 | `topic` | 毕业设计题目 | FK→teacher，CHECK(status/max_select) |
| 7 | `selection` | 选题记录 | stu_id UNIQUE（一生一题），FK→student/topic |
| 8 | `submission` | 阶段文档 | FK→selection，支持多版本(version)，CHECK(stage/status) |
| 9 | `review` | 审阅评分 | submission_id UNIQUE（一提交一审），CHECK(score 0-100) |
| 10 | `deadline` | 流程期限 | UNIQUE(stage, semester)，CHECK(start≤end) |
| 11 | `audit_log` | 审计日志 | JSON 列，**按年 RANGE 分区** |
| 12 | `sys_user` | 系统管理员 | username UNIQUE，BCrypt 密码 |

---

## 8 项高阶数据库技术

### 1. 存储过程

**`calc_final_score`** — 加权计算最终成绩（开题 15% + 中期 15% + 初稿 30% + 终稿 40%），支持 OUT 参数返回成绩明细。

**`stat_dept_completion`** — 统计院系毕业设计完成率，参数化支持全校/单系。

### 2. 触发器（3 个）

| 触发器 | 时机 | 作用 |
|--------|------|------|
| `trg_selection_after_insert` | AFTER INSERT selection | 选题后自动更新 topic.status |
| `trg_submission_audit_log` | AFTER INSERT submission | 文档提交时自动写入审计日志（JSON） |
| `trg_review_after_insert` | AFTER INSERT review | 评分后更新提交状态 + 重算综合成绩 + 审计记录 |

### 3. 视图（3 个）

- **`v_student_progress`** — 学生进度总览（12 个子查询展示四阶段状态）
- **`v_topic_detail`** — 选题详情（题目→教师→学生完整链路）
- **`v_score_ranking`** — 成绩排名基础数据

### 4. 索引优化

- **全文索引**：`topic.title` / `topic.description` 使用 ngram 解析器支持中文搜索，性能比 LIKE 提升 15 倍
- **复合索引**：覆盖多条件高频查询（如 `selection_id + stage + status`），EXPLAIN 验证 type=ref

### 5. 事务并发

学生选题场景使用 `@Transactional` + 先 UPDATE 后 INSERT 的悲观锁策略，防止一题多选。

### 6. 窗口函数

DENSE_RANK / RANK / ROW_NUMBER / CUME_DIST / LAG — 7 个查询覆盖排名、极值、分布、间隔等场景。

### 7. 权限管理

3 个 MySQL 角色（`gms_student` / `gms_teacher` / `gms_admin`）+ 列级 GRANT 最小权限原则。

### 8. 审计日志

JSON 列存储操作快照 + 触发器自动写入 + 按年 RANGE 分区优化查询与归档。

> 详细实现见 [数据库设计文档.md](数据库设计文档.md)

---

## SQL 脚本清单

| 文件 | 内容 |
|------|------|
| `sql/01_schema.sql` | 建表 + 主外键 + CHECK 约束 + 基础索引 |
| `sql/02_data.sql` | 测试数据（2系×3专业×6班×30学生×20教师） |
| `sql/03_procedures.sql` | 2 个存储过程 |
| `sql/04_triggers.sql` | 3 个触发器 |
| `sql/05_views.sql` | 3 个视图 |
| `sql/06_indexes.sql` | 高级索引 + EXPLAIN 性能对比 |
| `sql/07_permissions.sql` | 3 角色 + GRANT 权限 |
| `sql/08_backup.sql` | 备份/恢复策略 |
| `sql/09_window_functions.sql` | 7 个窗口函数查询 |

---

## 项目结构

```
graduation-management/
├── sql/                              # SQL 脚本集（9 个文件）
├── src/main/java/com/gms/
│   ├── entity/          (12 实体)     # JPA 实体映射
│   ├── mapper/          (12 接口+6XML) # MyBatis 数据访问
│   ├── service/         (14 接口+14实现) # 业务逻辑
│   ├── controller/      (5 控制器)    # Web 层
│   └── config/          (4 配置)      # 拦截器/密码/Web/自动填充
└── src/main/resources/
    ├── application.yml               # 应用配置
    ├── mapper/                       # MyBatis XML 映射
    ├── static/css/style.css          # 统一样式
    └── templates/                    # Thymeleaf 模板（20 个页面）
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| ORM | MyBatis-Plus 3.5.3.1 |
| 数据库 | MySQL 8.0 |
| 模板引擎 | Thymeleaf 3.0 |
| 前端 | Bootstrap 5 + Bootstrap Icons |
| 密码加密 | BCrypt (spring-security-crypto) |
| 构建 | Maven |
