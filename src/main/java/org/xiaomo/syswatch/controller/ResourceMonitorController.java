package org.xiaomo.syswatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.service.PrometheusService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceMonitorController {

    @Autowired
    private PrometheusService prometheusService;

    /**
     * 获取所有资源的当前状态
     * GET /api/resources/status
     */
    @GetMapping("/status")
    public List<Map<String, Object>> getAllResourceStatus() {
        // prometheusService 返回 List<Map>，每个 Map 包含 resourceId、cpuUsage、memUsage、diskUsage、status
        return prometheusService.getAllResourceStatus();
    }

    /**
     * 获取某个资源的历史状态
     * GET /api/resources/history?resourceId=xxx&start=时间戳&end=时间戳&step=秒
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getResourceHistory(
            @RequestParam String resourceId,
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "60") long step
    ) {
        // prometheusService 返回 List<Map>，每个 Map 包含 timestamp、cpuUsage、memUsage、diskUsage
        return prometheusService.getResourceHistory(resourceId, start, end, step);
    }

    /**
     * 获取单个资源的详细状态
     * GET /api/resources/{resourceId}/details
     */
    @GetMapping("/{resourceId}/details")
    public Map<String, Object> getResourceDetails(@PathVariable String resourceId) {
        // 返回最新状态
        return prometheusService.getResourceDetails(resourceId);
    }
}
