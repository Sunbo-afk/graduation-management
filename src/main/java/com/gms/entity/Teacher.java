package com.gms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {

    @TableId
    private String teacherId;

    private String teacherName;

    private String password;

    private Integer deptId;

    private String title;

    private String researchDirection;

    private String phone;

    private String email;

    private Integer maxStudents;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String deptName;
}
