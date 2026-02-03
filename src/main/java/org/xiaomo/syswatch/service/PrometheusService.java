package org.xiaomo.syswatch.service;

public interface PrometheusService {

    /**
     * 触发 Prometheus 热加载规则
     */
    void reload();
}