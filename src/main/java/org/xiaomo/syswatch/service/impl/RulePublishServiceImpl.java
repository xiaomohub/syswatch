package org.xiaomo.syswatch.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.*;
import static org.xiaomo.syswatch.util.HashUtil.sha256;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RulePublishServiceImpl implements RulePublishService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private RuleRenderService ruleRenderService;

    @Resource
    private NacosConfigService nacosConfigService;

    @Resource
    private SshFileService sshFileService;

    @Resource
    private PrometheusService prometheusService;

    @Override
    public void publishAll() {

        // 1. 查询启用规则
        List<AlertRule> rules = alertRuleMapper.selectEnabled();

        // 2. 按资源类型分组
        Map<String, List<AlertRule>> groupMap =
                rules.stream().collect(Collectors.groupingBy(AlertRule::getResourceType));

        // 3. 渲染 + 发布
        for (Map.Entry<String, List<AlertRule>> entry : groupMap.entrySet()) {

            String resourceType = entry.getKey();
            String fileName = resourceType + "_rules.yaml";

            // 3.1 渲染规则
            String renderedYaml = ruleRenderService.render(resourceType, entry.getValue());

            // 3.2 发布到 Nacos（快照）
            nacosConfigService.publish(fileName, renderedYaml);

            // 3.3 从 Nacos 读取（唯一真源）
            String nacosContent = nacosConfigService.get(fileName);

            // 3.4 本地 hash（对 nacosContent）
            String localHash = sha256(nacosContent);

            // 3.5 SSH 写入 Prometheus 主机
            sshFileService.writeRuleFile(fileName, nacosContent);

            // 3.6 远端 hash 校验
            String remoteHash = sshFileService.getRemoteSha256(fileName);
            if (!localHash.equals(remoteHash)) {
                throw new IllegalStateException(
                        "规则文件内容不一致，拒绝 reload：" + fileName
                );
            }
        }

        // 4. 所有规则文件校验通过，才 reload
        prometheusService.reload();
    }

}
