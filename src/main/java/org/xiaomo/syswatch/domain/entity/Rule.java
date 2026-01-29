package org.xiaomo.syswatch.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rule") // 对应数据库表名
public class Rule {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("metric_type")
    private String metricType;

    private Double threshold;

    private Integer level;

    @TableField("enable_flag")
    private Integer enableFlag;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
