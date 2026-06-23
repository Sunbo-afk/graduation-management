package com.gms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Integer deptId;
    private String deptName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
