package org.xiaomo.syswatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import java.util.List;

@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {

    // 自定义方法
    @Select("""
        SELECT
          id,
          rule_name AS ruleName,
          resource_type AS resourceType,
          metric_code AS metricCode,
          expr_template AS exprTemplate,
          comparator,
          threshold,
          duration,
          severity,
          enabled,
          annotations,
          create_time AS createTime,
          update_time AS updateTime,
          summary,
          description
        FROM alert_rule
        ORDER BY id ASC
    """)
    List<AlertRule> selectAllRules();


    @Select("""
    SELECT
      id,
      rule_name AS ruleName,
      resource_type AS resourceType,
      metric_code AS metricCode,
      expr_template AS exprTemplate,
      comparator,
      threshold,
      duration,
      severity,
      enabled,
      annotations,
      create_time AS createTime,
      update_time AS updateTime,
      summary,
      description
    FROM alert_rule
    WHERE enabled = 1
    ORDER BY id ASC
""")
    List<AlertRule> selectEnabled();

}