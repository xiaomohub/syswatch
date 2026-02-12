package org.xiaomo.syswatch.service;

public interface SshFileService {

    /**
     * 将规则文件写入 Prometheus 主机
     *
     * @param fileName 规则文件名，例如 cpu_rules.yaml
     * @param content  YAML 内容
     */
    void writeRuleFile(String fileName, String content);


    String readRuleFile(String fileName);
}
