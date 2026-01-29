package org.xiaomo.syswatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.PrometheusService;
import org.xiaomo.syswatch.service.RuleFileService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private RuleFileService ruleFileService;

    @Resource
    private PrometheusService prometheusService;


    /**
     * 查询所有规则（不区分启用状态）
     */
    @GetMapping
    public List<AlertRule> listAll() {
        return alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .orderByAsc(AlertRule::getId)
        );
    }



    /**
     * 新增规则（自动发布）
     */
    @PostMapping
    public String add(@RequestBody AlertRule rule) {
        alertRuleMapper.insert(rule);
        publishRules();
        return "OK";
    }

    /**
     * 修改规则（自动发布）
     */
    @PutMapping
    public String update(@RequestBody AlertRule rule) {
        alertRuleMapper.updateById(rule);
        publishRules();
        return "OK";
    }

    /**
     * 删除规则（自动发布）
     */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        alertRuleMapper.deleteById(id);
        publishRules();
        return "OK";
    }

    /**
     * 启用/停用规则
     * enabled: 1 = 启用, 0 = 停用
     */
    @PostMapping("/{id}/enable")
    public Map<String, Object> enable(@PathVariable Long id,
                                      @RequestParam Integer enabled) {
        Map<String, Object> result = new HashMap<>();
        try {
            AlertRule rule = new AlertRule();
            rule.setId(id);
            rule.setEnabled(enabled);
            alertRuleMapper.update(
                    rule,
                    new LambdaUpdateWrapper<AlertRule>().eq(AlertRule::getId, id)
            );

            publishRules();

            result.put("success", true);
            result.put("enabled", enabled);
        } catch (Exception e) {
            e.printStackTrace();  // <--- 关键，看看真实异常
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }



    /**
     * 手动发布所有规则（兜底）
     */
    @PostMapping("/publish")
    public String publishAll() {
        publishRules();
        return "OK";
    }

    /**
     * 发布规则统一方法
     */
    private void publishRules() {
        ruleFileService.generateAllRules();
        prometheusService.reload();
    }
}
