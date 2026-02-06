package org.xiaomo.syswatch.service.impl;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.service.NacosConfigService;

import java.util.Properties;

@Service
public class NacosConfigServiceImpl implements NacosConfigService {

    // ✅ 改这里
    @Value("${spring.cloud.nacos.server-addr}")
    private String serverAddr;

    // 如果你没有 namespace 可以给默认值
    @Value("${spring.cloud.nacos.config.namespace:}")
    private String namespace;

    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String defaultGroup;

    private ConfigService configService;

    @PostConstruct
    public void init() throws Exception {
        Properties props = new Properties();
        props.put("serverAddr", serverAddr);

        if (namespace != null && !namespace.isEmpty()) {
            props.put("namespace", namespace);
        }

        this.configService = NacosFactory.createConfigService(props);
    }

    @Override
    public void publish(String dataId, String content) {
        try {
            boolean success = configService.publishConfig(
                    dataId,
                    defaultGroup,
                    content
            );
            if (!success) {
                throw new RuntimeException("Nacos publish failed: " + dataId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Publish config to nacos error", e);
        }
    }

    public String get(String dataId) {
        try {
            return configService.getConfig(dataId, "DEFAULT_GROUP", 3000);
        } catch (Exception e) {
            throw new RuntimeException("获取 Nacos 配置失败", e);
        }
    }

}