package org.xiaomo.syswatch.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("metric") // 对应数据库表名
public class Metric {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("cpu_usage")
    private Double cpuUsage;

    @TableField("mem_usage")
    private Double memUsage;

    @TableField("disk_usage")
    private Double diskUsage;

    @TableField("response_time")
    private Integer responseTime;

    @TableField(value = "collect_time", fill = FieldFill.INSERT)
    private LocalDateTime collectTime;
}
