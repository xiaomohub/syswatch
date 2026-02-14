package org.xiaomo.syswatch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.domain.entity.AlertLog;
import org.xiaomo.syswatch.mapper.AlertLogMapper;
import org.xiaomo.syswatch.service.AlertLogService;

@RestController
@RequestMapping("/api/logs")
public class AlertLogController {

    @Resource
    private AlertLogService alertLogService;

    @Resource
    private AlertLogMapper alertLogMapper;

    /** 分页列表（不含 content_before / content_after） */
    @GetMapping
    public Page<AlertLog> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return alertLogService.listLogs(page, size);
    }

    /** ★ 详情接口 — 前端点击「查看变更」时调用，返回完整内容含 before/after */
    @GetMapping("/{id}")
    public AlertLog detail(@PathVariable Long id) {
        return alertLogMapper.selectById(id);
    }
}