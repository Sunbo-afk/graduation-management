package com.gms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("major")
public class Major {
    @TableId(type = IdType.AUTO)
    private Integer majorId;
    private String majorName;
    private Integer deptId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
