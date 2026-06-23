package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Integer reviewId;

    private Integer submissionId;

    private String teacherId;

    private BigDecimal score;

    private String comment;

    private LocalDateTime reviewTime;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String stuName;
}
