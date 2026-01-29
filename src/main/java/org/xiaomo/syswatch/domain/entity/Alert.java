package org.xiaomo.syswatch.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert") // 对应数据库表名
public class Alert {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("metric_type")
    private String metricType;

    @TableField("metric_value")
    private Double metricValue;

    private Integer level;

    private Integer status;

    private String message;

    @TableField(value = "alert_time", fill = FieldFill.INSERT)
    private LocalDateTime alertTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
