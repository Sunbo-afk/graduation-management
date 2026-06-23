package com.gms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("class")
public class ClassInfo {
    @TableId(type = IdType.AUTO)
    private Integer classId;
    private String className;
    private Integer majorId;
    private String grade;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
