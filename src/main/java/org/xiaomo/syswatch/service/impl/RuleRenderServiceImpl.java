package org.xiaomo.syswatch.service.impl;

import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.service.RuleRenderService;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

@Service
public class RuleRenderServiceImpl implements RuleRenderService {

    @Override
    @AlertLog(action = "渲染文件内容")
    public String render(String resourceType, List<AlertRule> rules) {

        // ====== 构造 YAML 数据结构 ======

        Map<String, Object> root = new LinkedHashMap<>();

        List<Map<String, Object>> groups = new ArrayList<>();
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("name", resourceType + "_rules");

        List<Map<String, Object>> ruleList = new ArrayList<>();

        for (AlertRule r : rules) {

            Map<String, Object> rule = new LinkedHashMap<>();

            // alert 名称
            rule.put("alert", r.getRuleName());

            // expr
            String expr = r.getExprTemplate();
            if (r.getComparator() != null && r.getThreshold() != null) {
                expr = expr + " " + r.getComparator() + " " + r.getThreshold();
            }
            rule.put("expr", expr);

            // for
            rule.put("for", r.getDuration() != null ? r.getDuration() : "0m");

            // labels
            Map<String, Object> labels = new LinkedHashMap<>();
            if (r.getSeverity() != null) {
                labels.put("severity", r.getSeverity());
            }

            labels.put("instance", "{{ $labels.instance }}");
            rule.put("labels", labels);


            // annotations
            Map<String, Object> annotations = new LinkedHashMap<>();

            String thresholdStr = r.getThreshold() != null ? r.getThreshold().toString() : "";

            annotations.put(
                    "summary",
                    "当前值 {{ printf \"%.2f\" $value }}%"
            );

            annotations.put(
                    "description",
                    "当前值 {{ printf \"%.2f\" $value }}%，阈值 " + thresholdStr + "%"
            );

            // 如果数据库里有自定义注解，覆盖默认值
            if (r.getAnnotations() != null) {
                annotations.putAll(r.getAnnotations());
            }

            rule.put("annotations", annotations);


            if (r.getDescription() != null) {
                annotations.put("description", r.getDescription());
            }

            // 自定义 annotations
            if (r.getAnnotations() != null) {
                annotations.putAll(r.getAnnotations());
            }

            rule.put("annotations", annotations);

            ruleList.add(rule);
        }

        group.put("rules", ruleList);
        groups.add(group);
        root.put("groups", groups);

        // ====== YAML 输出配置 ======

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);


        Yaml yaml = new Yaml(options);

        return yaml.dump(root);
    }
}
