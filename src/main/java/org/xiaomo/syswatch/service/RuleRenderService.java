package org.xiaomo.syswatch.service;

import org.xiaomo.syswatch.domain.entity.AlertRule;

import java.util.List;

public interface RuleRenderService {

    String render(String resourceType, List<AlertRule> rules);
}