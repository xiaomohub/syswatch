package org.xiaomo.syswatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.AlertRuleService;
import org.xiaomo.syswatch.service.RulePublishService;

import java.util.List;

@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private RulePublishService rulePublishService;

    @Override
    public List<AlertRule> listAll() {
        return alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .orderByAsc(AlertRule::getId)
        );
    }

    @Override
    @Transactional
    public void create(AlertRule rule) {
        rule.setEnabled(1);
        alertRuleMapper.insert(rule);
        rulePublishService.publishAll();
    }

    @Override
    @Transactional
    public void update(AlertRule rule) {
        alertRuleMapper.updateById(rule);
        rulePublishService.publishAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        alertRuleMapper.deleteById(id);
        rulePublishService.publishAll();
    }

    @Override
    @Transactional
    public void enable(Long id, Integer enabled) {
        AlertRule rule = new AlertRule();
        rule.setId(id);
        rule.setEnabled(enabled);
        alertRuleMapper.updateById(rule);
        rulePublishService.publishAll();
    }

    @Override
    public void publishAll() {
        rulePublishService.publishAll();
    }
}