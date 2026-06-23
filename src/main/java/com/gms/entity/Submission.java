package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submission")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Integer submissionId;

    private Integer selectionId;

    private String stage;

    private String filePath;

    private String description;

    private LocalDateTime submitTime;

    private String status;

    private Integer version;
}
