package org.xiaomo.syswatch.service.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.service.PrometheusService;

@Service
public class PrometheusServiceImpl implements PrometheusService {

    @Value("${prometheus.reload-url}")
    private String reloadUrl;

    @Resource
    private RestTemplate restTemplate;

    @Override
    @AlertLog(action="prometheus重载")
    public void reload() {
        try {
            restTemplate.postForEntity(reloadUrl, null, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Prometheus reload 失败", e);
        }
    }
}