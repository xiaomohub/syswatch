package org.xiaomo.syswatch.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertLog;
import org.xiaomo.syswatch.mapper.AlertLogMapper;
import org.xiaomo.syswatch.service.AlertLogService;

import java.time.LocalDateTime;

@Service
public class AlertLogServiceImpl implements AlertLogService {

    @Resource
    private AlertLogMapper logMapper;

    @Override
    public void record(String module, String alertId, String action, String operator, boolean status, String message) {
        AlertLog log = new AlertLog();
        log.setModule(module);
        log.setAlertId(alertId);
        log.setAction(action);
        log.setOperator(operator);
        log.setTimestamp(LocalDateTime.now());
        log.setStatus(status);
        log.setMessage(message);

        logMapper.insert(log);
    }
}
