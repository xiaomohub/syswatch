package org.xiaomo.syswatch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class PrometheusService {

    private final RestTemplate restTemplate;

    @Value("${prometheus.url:http://localhost:9090}")
    private String prometheusUrl;

    public PrometheusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 查询所有资源状态（示例：CPU 和内存）
     */
    public List<Map<String, Object>> getAllResourceStatus() {
        String queryCpu = "avg(rate(node_cpu_seconds_total{mode!=\"idle\"}[5m])) * 100";
        String queryMem = "(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100";

        Map<String, Object> cpuResult = queryPrometheus(queryCpu);
        Map<String, Object> memResult = queryPrometheus(queryMem);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of(
                "resourceId", "server-group",
                "cpuUsage", extractValue(cpuResult),
                "memUsage", extractValue(memResult),
                "status", 0
        ));
        return list;
    }

    /**
     * 查询单个资源详情
     */
    public Map<String, Object> getResourceDetails(String instance) {
        String cpuQuery = String.format(
                "avg(rate(node_cpu_seconds_total{instance=\"%s\",mode!=\"idle\"}[5m])) * 100",
                instance
        );
        String memQuery = String.format(
                "(1 - (node_memory_MemAvailable_bytes{instance=\"%s\"} / node_memory_MemTotal_bytes{instance=\"%s\"})) * 100",
                instance,instance
        );

        Map<String, Object> cpu = queryPrometheus(cpuQuery);
        Map<String, Object> mem = queryPrometheus(memQuery);

        return Map.of(
                "resourceId", instance,
                "cpuUsage", extractValue(cpu),
                "memUsage", extractValue(mem),
                "status", 0
        );
    }

    /**
     * 查询历史数据（区间查询）
     */
    public List<Map<String, Object>> getResourceHistory(String instance, long start, long end, long step) {
        String cpuRangeQuery = String.format(
                "avg(rate(node_cpu_seconds_total{instance=\"%s\",mode!=\"idle\"}[5m])) * 100",
                instance
        );

        String url = String.format(
                "%s/api/v1/query_range?query=%s&start=%d&end=%d&step=%d",
                prometheusUrl, cpuRangeQuery, start, end, step
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> history = new ArrayList<>();
        try {
            List<?> results = (List<?>) ((Map<?, ?>) response.get("data")).get("result");
            if (!results.isEmpty()) {
                List<?> values = (List<?>) ((Map<?, ?>) results.get(0)).get("values");
                for (Object v : values) {
                    List<?> arr = (List<?>) v;
                    history.add(Map.of(
                            "timestamp", arr.get(0),
                            "cpuUsage", Double.parseDouble(arr.get(1).toString()) * 100
                    ));
                }
            }
        } catch (Exception ignored) {}

        return history;
    }

    /**
     * 通用查询
     */
    private Map<String, Object> queryPrometheus(String query) {
        String url = String.format("%s/api/v1/query?query=%s", prometheusUrl, query);
        return restTemplate.getForObject(url, Map.class);
    }

    /**
     * 提取 Prometheus 单值指标
     */
    private Double extractValue(Map<String, Object> result) {
        try {
            List<?> results = (List<?>) ((Map<?, ?>) result.get("data")).get("result");
            if (!results.isEmpty()) {
                List<?> arr = (List<?>) ((Map<?, ?>) results.get(0)).get("value");
                return Double.parseDouble(arr.get(1).toString());
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}
