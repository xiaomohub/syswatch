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

    @Value("${nacos.server-addr}")
    private String serverAddr;

    @Value("${nacos.namespace}")
    private String namespace;

    @Value("${nacos.group}")
    private String defaultGroup;

    private ConfigService configService;

    @PostConstruct
    public void init() throws Exception {
        Properties props = new Properties();
        props.put("serverAddr", serverAddr);
        props.put("namespace", namespace);
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
}