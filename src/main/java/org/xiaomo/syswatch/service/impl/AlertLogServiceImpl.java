package org.xiaomo.syswatch.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertLog;
import org.xiaomo.syswatch.handler.AlertLogWebSocketHandler;
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
        // 推送前端
        try {
            String json = new ObjectMapper().writeValueAsString(log);
            AlertLogWebSocketHandler.broadcast(json);
        } catch (Exception e) {
            e.printStackTrace(); // 日志推送失败不影响业务
        }

    }

    @Override
    public Page<AlertLog> listLogs(int pageNum, int pageSize) {
        Page<AlertLog> page = new Page<>(pageNum, pageSize);
        // MyBatis-Plus 可以直接分页查询全部
        return logMapper.selectPage(page, null);
    }
}
