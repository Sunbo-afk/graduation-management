package com.gms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gms.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    Teacher selectByTeacherIdWithInfo(@Param("teacherId") String teacherId);
}
