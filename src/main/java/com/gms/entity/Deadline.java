package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("deadline")
public class Deadline {

    @TableId(type = IdType.AUTO)
    private Integer deadlineId;

    private String stage;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String semester;
}
