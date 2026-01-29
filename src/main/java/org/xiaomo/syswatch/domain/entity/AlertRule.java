package org.xiaomo.syswatch.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("rule_name")
    private String ruleName;

    @TableField("resource_type")
    private String resourceType;

    @TableField("metric_code")
    private String metricCode;

    @TableField("expr_template")
    private String exprTemplate;

    private String comparator;

    private Double threshold;

    private String duration;

    private String severity;

    /**
     * tinyint(1) -> Integer
     * 0 = 停用, 1 = 启用
     */
    private Integer enabled;

    private Map<String, Object> annotations;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("summary")
    private String summary;      // 规则摘要信息

    @TableField("description")
    private String description;  // 规则详细描述

}
