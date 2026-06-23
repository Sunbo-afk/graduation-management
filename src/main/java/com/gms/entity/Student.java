package com.gms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    @TableId
    private String stuId;
    private String stuName;
    private String password;
    private Integer classId;
    private String phone;
    private String email;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String className;
    @TableField(exist = false)
    private String majorName;
    @TableField(exist = false)
    private String deptName;
}
