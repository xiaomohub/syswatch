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
    private SshFileService sshFileService;

    @Resource
    private PrometheusService prometheusService;

    @Override
    public void publishAll() {

        // 1. 查询启用规则
        List<AlertRule> rules = alertRuleMapper.selectEnabled();

        // 2. 按资源类型分组
        Map<String, List<AlertRule>> groupMap =
                rules.stream().collect(Collectors.groupingBy(AlertRule::getResourceType));

        // 3. 渲染 + 发布到 Nacos
        for (Map.Entry<String, List<AlertRule>> entry : groupMap.entrySet()) {

            String fileName = entry.getKey() + "_rules.yaml";
            String yaml = ruleRenderService.render(entry.getKey(), entry.getValue());

            // 3.1 写入 Nacos（作为配置快照）
            nacosConfigService.publish(fileName, yaml);

            // 3.2 从 Nacos 拉取（保证一致来源）
            String nacosContent = nacosConfigService.get(fileName);

            // 3.3 SSH 写到 Prometheus 主机
            sshFileService.writeRuleFile(fileName, nacosContent);
        }

        // 4. reload Prometheus
        prometheusService.reload();
    }
}
