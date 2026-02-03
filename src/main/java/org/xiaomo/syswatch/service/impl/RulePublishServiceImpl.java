package org.xiaomo.syswatch.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RulePublishServiceImpl implements RulePublishService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private RuleRenderService ruleRenderService;

    @Resource
    private NacosConfigService nacosConfigService;

    @Resource
    private PrometheusService prometheusService;

    @Override
    public void publishAll() {

        // 1. 查询所有启用规则
        List<AlertRule> rules = alertRuleMapper.selectEnabled();

        // 2. 按资源类型分组
        Map<String, List<AlertRule>> groupMap =
                rules.stream().collect(Collectors.groupingBy(AlertRule::getResourceType));

        // 3. 渲染 + 发布到 Nacos
        for (Map.Entry<String, List<AlertRule>> entry : groupMap.entrySet()) {
            String yaml = ruleRenderService.render(entry.getKey(), entry.getValue());

            nacosConfigService.publish(entry.getKey() + "_rules.yaml", yaml);
        }

        // 4. Reload Prometheus
        prometheusService.reload();
    }
}