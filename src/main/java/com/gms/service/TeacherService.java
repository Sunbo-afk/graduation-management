package com.gms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gms.entity.Teacher;

public interface TeacherService extends IService<Teacher> {

    /**
     * 统计某位教师当前指导的学生数
     * @param teacherId 教师工号
     * @return 当前指导学生数
     */
    int countStudentsForTeacher(String teacherId);
}
