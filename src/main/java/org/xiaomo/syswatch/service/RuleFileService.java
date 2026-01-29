package org.xiaomo.syswatch.service;

import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RuleFileService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    public void generateAllRules() {
        System.out.println("Mock: 生成 Prometheus 规则文件成功");
//        try {
//            // 1. 查所有启用规则
//            List<AlertRule> rules = alertRuleMapper.selectEnabled();
//
//            // 2. 按资源类型分组
//            Map<String, List<AlertRule>> groupMap =
//                    rules.stream().collect(Collectors.groupingBy(AlertRule::getResourceType));
//
//            // 3. 每个资源类型生成一个文件
//            for (Map.Entry<String, List<AlertRule>> entry : groupMap.entrySet()) {
//                String resourceType = entry.getKey();
//                List<AlertRule> ruleList = entry.getValue();
//
//                String yaml = buildYaml(resourceType, ruleList);
//
//                Path path = Paths.get("/etc/prometheus/rules/"
//                        + resourceType + "_rules.yml");
//
//                Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("生成 Prometheus 规则文件失败", e);
//        }
    }

    private String buildYaml(String resourceType, List<AlertRule> rules) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("groups:\n");
//        sb.append("- name: ").append(resourceType).append("_rules\n");
//        sb.append("  rules:\n");
//
//        for (AlertRule r : rules) {
//            sb.append("  - alert: ").append(r.getRuleName()).append("\n");
//            sb.append("    expr: ").append(r.getExprTemplate()).append("\n");
//            sb.append("    for: ").append(r.getDuration()).append("\n");
//            sb.append("    labels:\n");
//            sb.append("      severity: ").append(r.getSeverity()).append("\n");
//            sb.append("    annotations:\n");
//            sb.append("      summary: \"").append(r.getRuleName()).append("\"\n");
//        }
          return "生成ok";
    }
}