package org.xiaomo.syswatch.service.impl;

import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.service.RuleRenderService;

import java.util.List;
import java.util.Map;

@Service
public class RuleRenderServiceImpl implements RuleRenderService {

    @Override
    public String render(String resourceType, List<AlertRule> rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("groups:\n");
        sb.append("- name: ").append(resourceType).append("_rules\n");
        sb.append("  rules:\n");

        for (AlertRule r : rules) {
            sb.append("  - alert: ").append(r.getRuleName()).append("\n");

            // 拼接 expr，如果有 comparator 和 threshold，可以组装
            String expr = r.getExprTemplate();
            if (r.getComparator() != null && r.getThreshold() != null) {
                expr = expr + " " + r.getComparator() + " " + r.getThreshold();
            }
            sb.append("    expr: ").append(expr).append("\n");

            sb.append("    for: ").append(r.getDuration() != null ? r.getDuration() : "0m").append("\n");

            // labels
            sb.append("    labels:\n");
            if (r.getSeverity() != null) {
                sb.append("      severity: ").append(r.getSeverity()).append("\n");
            }

            // annotations
            sb.append("    annotations:\n");
            if (r.getSummary() != null) {
                sb.append("      summary: \"").append(r.getSummary()).append("\"\n");
            } else {
                sb.append("      summary: \"").append(r.getRuleName()).append("\"\n");
            }
            if (r.getDescription() != null) {
                sb.append("      description: \"").append(r.getDescription()).append("\"\n");
            }

            // 自定义 annotations map
            Map<String, Object> customAnno = r.getAnnotations();
            if (customAnno != null) {
                for (Map.Entry<String, Object> entry : customAnno.entrySet()) {
                    sb.append("      ").append(entry.getKey()).append(": \"").append(entry.getValue()).append("\"\n");
                }
            }
        }
        return sb.toString();
    }
}