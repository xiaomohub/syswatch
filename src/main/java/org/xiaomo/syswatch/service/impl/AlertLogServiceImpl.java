package org.xiaomo.syswatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
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

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void record(AlertLog log) {
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        if (log.getOperator() == null) {
            log.setOperator("admin");
        }
        logMapper.insert(log);

        // WebSocket 推送（推送时排除大文本，前端列表不需要）
        try {
            String beforeBak = log.getContentBefore();
            String afterBak = log.getContentAfter();
            log.setContentBefore(null);
            log.setContentAfter(null);

            String json = objectMapper.writeValueAsString(log);
            AlertLogWebSocketHandler.broadcast(json);

            // 还原（不影响后续逻辑）
            log.setContentBefore(beforeBak);
            log.setContentAfter(afterBak);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Page<AlertLog> listLogs(int pageNum, int pageSize) {
        Page<AlertLog> page = new Page<>(pageNum, pageSize);

        // ★ 列表查询排除 content_before / content_after，提升性能
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<AlertLog>()
                .select(AlertLog::getId,
                        AlertLog::getResourceType,
                        AlertLog::getFileName,
                        AlertLog::getAction,
                        AlertLog::getRulesCount,
                        AlertLog::getPublishId,
                        AlertLog::getContentChanged,
                        AlertLog::getReloaded,
                        AlertLog::getOperator,
                        AlertLog::getStatus,
                        AlertLog::getMessage,
                        AlertLog::getTimestamp)
                .orderByDesc(AlertLog::getTimestamp);

        return logMapper.selectPage(page, wrapper);
    }
}