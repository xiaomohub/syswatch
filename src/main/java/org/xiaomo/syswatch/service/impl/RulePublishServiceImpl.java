package org.xiaomo.syswatch.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.annotation.AlertLog;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.*;
import static org.xiaomo.syswatch.util.HashUtil.sha256;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        List<AlertRule> rules = alertRuleMapper.selectEnabled();

        Map<String, List<AlertRule>> groupMap =
                rules.stream().collect(Collectors.groupingBy(AlertRule::getResourceType));

        for (Map.Entry<String, List<AlertRule>> entry : groupMap.entrySet()) {

            String resourceType = entry.getKey();
            String fileName = resourceType + "_rules.yaml";

            // 1. 渲染
            String renderedYaml = ruleRenderService.render(resourceType, entry.getValue());

            // 2. 发布到 Nacos
            nacosConfigService.publish(fileName, renderedYaml);

            // 3. 从 Nacos 读取（唯一真源）
            String nacosContent = nacosConfigService.get(fileName);

            // 4. 生成发布标识
            String publishId = generatePublishId();

            // 5. 加入标识
            String finalContent = addPublishId(nacosContent, publishId);

            // 6. SSH 写入 + 确认
            boolean success = false;
            int retry = 0;
            int maxRetry = 3;

            while (!success && retry < maxRetry) {
                sshFileService.writeRuleFile(fileName, finalContent);

                String remoteContent = sshFileService.readRuleFile(fileName);
                if (remoteContent != null && remoteContent.contains(publishId)) {
                    success = true;
                } else {
                    retry++;
                }
            }

            if (!success) {
                throw new IllegalStateException(
                        "规则文件同步失败，无法确认写入成功：" + fileName
                );
            }
        }

        // 7. 全部确认成功后 reload
        prometheusService.reload();
    }

    private String generatePublishId() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }


    private String addPublishId(String content, String publishId) {
        return "# PUBLISH_ID: " + publishId + "\n" + content;
    }

}
