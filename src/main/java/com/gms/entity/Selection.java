package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("selection")
public class Selection {

    @TableId(type = IdType.AUTO)
    private Integer selectionId;

    private String stuId;

    private Integer topicId;

    private LocalDateTime selectTime;

    private String status;

    private BigDecimal finalScore;

    private LocalDateTime completedAt;

    @TableField(exist = false)
    private String stuName;

    @TableField(exist = false)
    private String topicTitle;

    @TableField(exist = false)
    private String teacherName;
}
