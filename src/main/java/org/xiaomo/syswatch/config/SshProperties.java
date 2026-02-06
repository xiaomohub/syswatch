package org.xiaomo.syswatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "syswatch.ssh")
public class SshProperties {
    private String host;
    private int port = 22;
    private String username;
    private String password;
}
