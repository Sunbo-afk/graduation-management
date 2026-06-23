use graduation_system;

insert into department(department_name)
values ('Computer Department');

set @department_id = last_insert_id();

insert into major(department_id, major_name)
values (@department_id, 'Software Engineering');

set @major_id = last_insert_id();

insert into class_info(major_id, class_name, grade_year)
values (@major_id, 'Software2201', 2022);

set @class_id = last_insert_id();

insert into student(class_id, student_no, student_name, gender, phone, email)
values (@class_id, 'student1', 'student1', 'M', '', '');

set @student_id = last_insert_id();

insert into teacher(department_id, teacher_no, teacher_name, research_direction, phone, email)
values (@department_id, 'teacher1', 'teacher1', 'graduation project management', '', '');

set @teacher_id = last_insert_id();

insert into role(role_name)
values ('admin'), ('teacher'), ('student');

select role_id into @admin_role_id from role where role_name = 'admin';
select role_id into @teacher_role_id from role where role_name = 'teacher';
select role_id into @student_role_id from role where role_name = 'student';

insert into user_account(username, password_hash, role_id, related_id)
values ('admin', '123456', @admin_role_id, null);

insert into user_account(username, password_hash, role_id, related_id)
values ('teacher1', '123456', @teacher_role_id, @teacher_id);

insert into user_account(username, password_hash, role_id, related_id)
values ('student1', '123456', @student_role_id, @student_id);

insert into graduation_topic(teacher_id, topic_title, topic_desc, required_skill, max_students, selected_count, status)
values (@teacher_id, 'project1', 'project1', 'Java Web, MySQL', 1, 1, '可选');

set @topic_id = last_insert_id();

insert into topic_selection(student_id, topic_id, select_time, status)
values (@student_id, @topic_id, now(), 'selected');

insert into stage(stage_name, stage_order)
values ('opening report', 1), ('midterm check', 2), ('final submission', 3);
