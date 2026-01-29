package org.xiaomo.syswatch.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrometheusService {

    /**
     * Mock: 查询资源状态
     */
    public List<Map<String, Object>> getAllResourceStatus() {
        return List.of(Map.of(
                "resourceId", "server-group",
                "cpuUsage", 0.0,
                "memUsage", 0.0,
                "status", 0
        ));
    }

    public Map<String, Object> getResourceDetails(String instance) {
        return Map.of(
                "resourceId", instance,
                "cpuUsage", 0.0,
                "memUsage", 0.0,
                "status", 0
        );
    }

    public List<Map<String, Object>> getResourceHistory(String instance, long start, long end, long step) {
        return Collections.emptyList();
    }

    /**
     * Mock: 不调用真实 Prometheus reload
     */
    public void reload() {
        System.out.println("Mock: Prometheus reload 成功");
    }
}
