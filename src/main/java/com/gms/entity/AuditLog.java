package com.gms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private String tableName;

    private String operation;

    private String recordId;

    private String oldValue;

    private String newValue;

    private String operatedBy;

    private LocalDateTime operationTime;

    private String ipAddress;
}
