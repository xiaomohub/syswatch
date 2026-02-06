package org.xiaomo.syswatch.service;

import org.xiaomo.syswatch.domain.entity.AlertLog;

public interface AlertLogService {
    void record(String module, String alertId, String action, String operator, boolean status, String message);
}
