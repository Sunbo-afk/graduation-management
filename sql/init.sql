drop database if exists graduation_system;
create database graduation_system default character set utf8mb4 collate utf8mb4_unicode_ci;
use graduation_system;

create table department (
    department_id int primary key auto_increment,
    department_name varchar(50) not null unique
) engine=InnoDB default charset=utf8mb4;

create table major (
    major_id int primary key auto_increment,
    department_id int not null,
    major_name varchar(50) not null,
    constraint fk_major_department foreign key (department_id) references department(department_id),
    constraint uk_major_name unique (department_id, major_name)
) engine=InnoDB default charset=utf8mb4;

create table class_info (
    class_id int primary key auto_increment,
    major_id int not null,
    class_name varchar(50) not null,
    grade_year int not null,
    constraint fk_class_major foreign key (major_id) references major(major_id),
    constraint uk_class_name unique (major_id, class_name)
) engine=InnoDB default charset=utf8mb4;

create table student (
    student_id int primary key auto_increment,
    class_id int not null,
    student_no varchar(30) not null unique,
    student_name varchar(50) not null,
    gender varchar(10),
    phone varchar(30),
    email varchar(100),
    constraint fk_student_class foreign key (class_id) references class_info(class_id)
) engine=InnoDB default charset=utf8mb4;

create table teacher (
    teacher_id int primary key auto_increment,
    department_id int not null,
    teacher_no varchar(30) not null unique,
    teacher_name varchar(50) not null,
    research_direction varchar(200),
    phone varchar(30),
    email varchar(100),
    constraint fk_teacher_department foreign key (department_id) references department(department_id)
) engine=InnoDB default charset=utf8mb4;

create table role (
    role_id int primary key auto_increment,
    role_name varchar(30) not null unique
) engine=InnoDB default charset=utf8mb4;

create table user_account (
    user_id int primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    role_id int not null,
    related_id int,
    status varchar(20) not null default 'normal',
    constraint fk_user_role foreign key (role_id) references role(role_id)
) engine=InnoDB default charset=utf8mb4;

create table graduation_topic (
    topic_id int primary key auto_increment,
    teacher_id int not null,
    topic_title varchar(100) not null,
    topic_desc text,
    required_skill varchar(300),
    max_students int not null default 1,
    selected_count int not null default 0,
    status varchar(20) not null default 'open',
    constraint fk_topic_teacher foreign key (teacher_id) references teacher(teacher_id),
    constraint ck_topic_count check (max_students > 0 and selected_count >= 0)
) engine=InnoDB default charset=utf8mb4;

create table topic_selection (
    selection_id int primary key auto_increment,
    student_id int not null unique,
    topic_id int not null,
    select_time datetime not null,
    status varchar(20) not null default 'selected',
    constraint fk_selection_student foreign key (student_id) references student(student_id),
    constraint fk_selection_topic foreign key (topic_id) references graduation_topic(topic_id)
) engine=InnoDB default charset=utf8mb4;

create table stage (
    stage_id int primary key auto_increment,
    stage_name varchar(50) not null unique,
    stage_order int not null unique
) engine=InnoDB default charset=utf8mb4;

create table deadline (
    deadline_id int primary key auto_increment,
    department_id int not null,
    stage_id int not null,
    start_time datetime not null,
    end_time datetime not null,
    constraint fk_deadline_department foreign key (department_id) references department(department_id),
    constraint fk_deadline_stage foreign key (stage_id) references stage(stage_id),
    constraint uk_deadline unique (department_id, stage_id),
    constraint ck_deadline_time check (end_time > start_time)
) engine=InnoDB default charset=utf8mb4;

create table submission (
    submission_id int primary key auto_increment,
    student_id int not null,
    stage_id int not null,
    title varchar(100),
    content text,
    file_path varchar(255),
    submit_time datetime not null,
    status varchar(20) not null default 'submitted',
    constraint fk_submission_student foreign key (student_id) references student(student_id),
    constraint fk_submission_stage foreign key (stage_id) references stage(stage_id),
    constraint uk_submission unique (student_id, stage_id)
) engine=InnoDB default charset=utf8mb4;

create table review (
    review_id int primary key auto_increment,
    submission_id int not null unique,
    teacher_id int not null,
    comment text,
    score decimal(5,2),
    review_time datetime,
    constraint fk_review_submission foreign key (submission_id) references submission(submission_id),
    constraint fk_review_teacher foreign key (teacher_id) references teacher(teacher_id),
    constraint ck_review_score check (score is null or (score >= 0 and score <= 100))
) engine=InnoDB default charset=utf8mb4;

create table notice (
    notice_id int primary key auto_increment,
    publisher_id int not null,
    title varchar(100) not null,
    content text not null,
    publish_time datetime not null,
    status varchar(20) not null default 'published',
    constraint fk_notice_user foreign key (publisher_id) references user_account(user_id)
) engine=InnoDB default charset=utf8mb4;

create table operation_log (
    log_id int primary key auto_increment,
    operator_id int,
    operation_type varchar(30) not null,
    target_table varchar(50),
    target_id int,
    operation_time datetime not null,
    detail text,
    constraint fk_log_user foreign key (operator_id) references user_account(user_id)
) engine=InnoDB default charset=utf8mb4;

create table score_summary (
    summary_id int primary key auto_increment,
    student_id int not null unique,
    opening_score decimal(5,2),
    middle_score decimal(5,2),
    final_score decimal(5,2),
    total_score decimal(5,2),
    grade_level varchar(20),
    update_time datetime,
    constraint fk_summary_student foreign key (student_id) references student(student_id)
) engine=InnoDB default charset=utf8mb4;

create index idx_student_class on student(class_id);
create index idx_teacher_department on teacher(department_id);
create index idx_topic_teacher on graduation_topic(teacher_id);
create index idx_selection_topic on topic_selection(topic_id);
create index idx_submission_student_stage on submission(student_id, stage_id);
create index idx_review_teacher on review(teacher_id);
create index idx_log_time on operation_log(operation_time);
create fulltext index ft_topic_title_desc on graduation_topic(topic_title, topic_desc);

create view v_student_progress as
select
    s.student_id,
    s.student_no,
    s.student_name,
    c.class_name,
    m.major_name,
    d.department_name,
    gt.topic_title,
    t.teacher_name,
    st.stage_name,
    sub.submit_time,
    sub.status as submit_status,
    r.score,
    r.comment
from student s
join class_info c on s.class_id = c.class_id
join major m on c.major_id = m.major_id
join department d on m.department_id = d.department_id
left join topic_selection ts on s.student_id = ts.student_id
left join graduation_topic gt on ts.topic_id = gt.topic_id
left join teacher t on gt.teacher_id = t.teacher_id
left join submission sub on s.student_id = sub.student_id
left join stage st on sub.stage_id = st.stage_id
left join review r on sub.submission_id = r.submission_id;

create view v_teacher_guidance as
select
    t.teacher_id,
    t.teacher_name,
    d.department_name,
    count(distinct ts.student_id) as student_count,
    avg(r.score) as avg_score
from teacher t
join department d on t.department_id = d.department_id
left join graduation_topic gt on t.teacher_id = gt.teacher_id
left join topic_selection ts on gt.topic_id = ts.topic_id
left join submission sub on ts.student_id = sub.student_id
left join review r on sub.submission_id = r.submission_id
group by t.teacher_id, t.teacher_name, d.department_name;

create view v_score_rank as
select
    s.student_id,
    s.student_no,
    s.student_name,
    d.department_name,
    ss.total_score,
    rank() over(partition by d.department_id order by ss.total_score desc) as department_rank
from score_summary ss
join student s on ss.student_id = s.student_id
join class_info c on s.class_id = c.class_id
join major m on c.major_id = m.major_id
join department d on m.department_id = d.department_id
where ss.total_score is not null;

delimiter //

create trigger trg_submission_insert_log
after insert on submission
for each row
begin
    insert into operation_log(operator_id, operation_type, target_table, target_id, operation_time, detail)
    values(null, 'INSERT', 'submission', new.submission_id, now(), concat('insert submission, student_id=', new.student_id));
end//

create trigger trg_submission_check_topic
before insert on submission
for each row
begin
    declare v_count int default 0;

    select count(*) into v_count
    from topic_selection
    where student_id = new.student_id;

    if v_count = 0 then
        signal sqlstate '45000' set message_text = 'student must select topic before submission';
    end if;
end//

create trigger trg_review_insert_log
after insert on review
for each row
begin
    insert into operation_log(operator_id, operation_type, target_table, target_id, operation_time, detail)
    values(null, 'INSERT', 'review', new.review_id, now(), concat('insert review, score=', new.score));
end//

create trigger trg_deadline_update_log
after update on deadline
for each row
begin
    insert into operation_log(operator_id, operation_type, target_table, target_id, operation_time, detail)
    values(null, 'UPDATE', 'deadline', new.deadline_id, now(), concat('deadline changed from ', old.end_time, ' to ', new.end_time));
end//

create procedure sp_select_topic(
    in p_student_id int,
    in p_topic_id int,
    out p_message varchar(100)
)
begin
    declare v_max int;
    declare v_selected int;
    declare v_exists int;

    start transaction;

    select count(*) into v_exists from topic_selection where student_id = p_student_id;

    if v_exists > 0 then
        set p_message = 'student already selected topic';
        rollback;
    else
        select max_students, selected_count into v_max, v_selected
        from graduation_topic
        where topic_id = p_topic_id
        for update;

        if v_selected >= v_max then
            set p_message = 'topic is full';
            rollback;
        else
            insert into topic_selection(student_id, topic_id, select_time, status)
            values(p_student_id, p_topic_id, now(), 'selected');

            update graduation_topic
            set selected_count = selected_count + 1
            where topic_id = p_topic_id;

            set p_message = 'select topic success';
            commit;
        end if;
    end if;
end//

create procedure sp_calc_final_score(
    in p_student_id int,
    out p_total_score decimal(5,2)
)
begin
    declare v_opening decimal(5,2);
    declare v_middle decimal(5,2);
    declare v_final decimal(5,2);
    declare v_level varchar(20);

    select avg(r.score) into v_opening
    from review r
    join submission sub on r.submission_id = sub.submission_id
    where sub.student_id = p_student_id and sub.stage_id = 1;

    select avg(r.score) into v_middle
    from review r
    join submission sub on r.submission_id = sub.submission_id
    where sub.student_id = p_student_id and sub.stage_id = 2;

    select avg(r.score) into v_final
    from review r
    join submission sub on r.submission_id = sub.submission_id
    where sub.student_id = p_student_id and sub.stage_id = 3;

    if v_opening is null or v_middle is null or v_final is null then
        set p_total_score = null;
        set v_level = 'incomplete';
    else
        set p_total_score = v_opening * 0.2 + v_middle * 0.3 + v_final * 0.5;

        if p_total_score >= 90 then
            set v_level = 'excellent';
        elseif p_total_score >= 80 then
            set v_level = 'good';
        elseif p_total_score >= 60 then
            set v_level = 'pass';
        else
            set v_level = 'fail';
        end if;
    end if;

    insert into score_summary(student_id, opening_score, middle_score, final_score, total_score, grade_level, update_time)
    values(p_student_id, v_opening, v_middle, v_final, p_total_score, v_level, now())
    on duplicate key update
        opening_score = v_opening,
        middle_score = v_middle,
        final_score = v_final,
        total_score = p_total_score,
        grade_level = v_level,
        update_time = now();
end//

delimiter ;
