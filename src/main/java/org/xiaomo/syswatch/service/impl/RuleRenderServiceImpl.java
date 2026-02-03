package org.xiaomo.syswatch.service.impl;

import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.service.RuleRenderService;

import java.util.List;

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
            sb.append("    expr: ").append(r.getExprTemplate()).append("\n");
            sb.append("    for: ").append(r.getDuration()).append("\n");
            sb.append("    labels:\n");
            sb.append("      severity: ").append(r.getSeverity()).append("\n");
            sb.append("    annotations:\n");
            sb.append("      summary: \"").append(r.getRuleName()).append("\"\n");
        }
        return sb.toString();
    }
}