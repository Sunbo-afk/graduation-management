package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic")
public class Topic {

    @TableId(type = IdType.AUTO)
    private Integer topicId;

    private String title;

    private String description;

    private String teacherId;

    private String direction;

    private Integer status;

    private Integer maxSelect;

    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String teacherName;
}
