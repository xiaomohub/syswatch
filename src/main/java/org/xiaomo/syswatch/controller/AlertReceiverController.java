package org.xiaomo.syswatch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.domain.dto.AlertmanagerWebhookDTO;
import org.xiaomo.syswatch.domain.entity.AlertEvent;
import org.xiaomo.syswatch.service.AlertEventService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/alerts")
public class AlertReceiverController {

    @Resource
    private AlertEventService alertEventService;

    @PostMapping("/receive")
    public void receive(@RequestBody AlertmanagerWebhookDTO payload) {
        alertEventService.handle(payload);
    }

    // 新增分页查询接口
    @GetMapping("/history")
    public Page<AlertEvent> history(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String alertName,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        return alertEventService.queryAlertEvents(pageNum, pageSize, alertName, severity, startTime, endTime);
    }
}