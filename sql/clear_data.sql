use graduation_system;

set foreign_key_checks = 0;

truncate table operation_log;
truncate table score_summary;
truncate table review;
truncate table submission;
truncate table deadline;
truncate table topic_selection;
truncate table graduation_topic;
truncate table notice;
truncate table user_account;
truncate table role;
truncate table teacher;
truncate table student;
truncate table class_info;
truncate table major;
truncate table department;
truncate table stage;

set foreign_key_checks = 1;
