package org.xiaomo.syswatch.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.service.AlertRuleService;

import java.util.List;

@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    @Resource
    private AlertRuleService alertRuleService;

    /**
     * 查询所有规则
     */
    @GetMapping
    public List<AlertRule> listAll() {
        return alertRuleService.listAll();
    }

    /**
     * 新增规则（自动发布）
     */
    @PostMapping
    public void add(@RequestBody AlertRule rule) {
        alertRuleService.create(rule);
    }

    /**
     * 修改规则（自动发布）
     */
    @PutMapping
    public void update(@RequestBody AlertRule rule) {
        alertRuleService.update(rule);
    }

    /**
     * 删除规则（自动发布）
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        alertRuleService.delete(id);
    }

    /**
     * 启用 / 停用规则
     */
    @PostMapping("/{id}/enable")
    public void enable(@PathVariable Long id,
                       @RequestParam Integer enabled) {
        alertRuleService.enable(id, enabled);
    }

    /**
     * 手动发布（兜底）
     */
    @PostMapping("/publish")
    public void publishAll() {
        alertRuleService.publishAll();
    }
}