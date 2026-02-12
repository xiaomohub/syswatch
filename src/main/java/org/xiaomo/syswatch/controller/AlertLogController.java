package org.xiaomo.syswatch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaomo.syswatch.domain.entity.AlertLog;
import org.xiaomo.syswatch.service.AlertLogService;

@RestController
@RequestMapping("/api/logs")
public class AlertLogController {

    @Resource
    private AlertLogService alertLogService;

    @GetMapping
    public Page<AlertLog> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return alertLogService.listLogs(page, size);
    }
}
