package org.xiaomo.syswatch.service;

public interface NacosConfigService {

    void publish(String dataId, String content);
    
}