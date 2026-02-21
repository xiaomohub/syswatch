package org.xiaomo.syswatch.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "syswatch.ssh")
public class SshProperties {

    private String host;

    private Integer port = 22;

    private String username;

    /**
     * 通过环境变量注入
     */
    private String password;

    @PostConstruct
    public void validate() {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("syswatch.ssh.host must be configured");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("syswatch.ssh.username must be configured");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("syswatch.ssh.password must be configured");
        }
    }
}