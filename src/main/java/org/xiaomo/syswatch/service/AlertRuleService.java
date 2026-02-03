package org.xiaomo.syswatch.service;

import org.xiaomo.syswatch.domain.entity.AlertRule;

import java.util.List;

public interface AlertRuleService {

    List<AlertRule> listAll();

    void create(AlertRule rule);

    void update(AlertRule rule);

    void delete(Long id);

    void enable(Long id, Integer enabled);

    void publishAll();
}