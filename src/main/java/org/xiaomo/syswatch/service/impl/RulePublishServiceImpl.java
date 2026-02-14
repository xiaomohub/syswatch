package org.xiaomo.syswatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.domain.entity.AlertLog;
import org.xiaomo.syswatch.domain.entity.AlertRule;
import org.xiaomo.syswatch.mapper.AlertRuleMapper;
import org.xiaomo.syswatch.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.xiaomo.syswatch.util.HashUtil.sha256;

@Service
public class RulePublishServiceImpl implements RulePublishService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private RuleRenderService ruleRenderService;

    @Resource
    private SshFileService sshFileService;

    @Resource
    private PrometheusService prometheusService;

    @Resource
    private AlertLogService alertLogService;

    @Override
    public void publishAll() {
        List<AlertRule> rules = alertRuleMapper.selectEnabled();
        List<String> resourceTypes = rules.stream()
                .map(AlertRule::getResourceType)
                .distinct()
                .toList();
        publishInternal(resourceTypes);
    }

    @Override
    public void publishByResourceType(String resourceType) {
        publishInternal(List.of(resourceType));
    }

    private void publishInternal(List<String> resourceTypes) {

        boolean needReload = false;

        for (String resourceType : resourceTypes) {

            String fileName = resourceType + "_rules.yaml";

            List<AlertRule> rules = alertRuleMapper.selectList(
                    new LambdaQueryWrapper<AlertRule>()
                            .eq(AlertRule::getEnabled, 1)
                            .eq(AlertRule::getResourceType, resourceType)
                            .orderByAsc(AlertRule::getId));

            // ★ 无启用规则 → 跳过
            if (rules.isEmpty()) {
                alertLogService.record(buildLog(
                        resourceType, fileName, "skip",
                        0, null,
                        null, null, false, false,
                        true, "无启用规则，跳过发布"
                ));
                continue;
            }
            String renderedYaml = null;
            try {
                // 1️⃣ 渲染新规则
                renderedYaml = ruleRenderService.render(resourceType, rules);

                // 2️⃣ 计算新 hash
                String newHash = sha256(renderedYaml);

                // 3️⃣ 读取远端旧文件
                String remoteContent = sshFileService.readRuleFile(fileName);
                String cleanRemote = null;

                if (remoteContent != null) {
                    cleanRemote = removePublishId(remoteContent);
                    String oldHash = sha256(cleanRemote);

                    // ★ 内容未变化 → 跳过，但仍记录 before/after 供确认
                    if (newHash.equals(oldHash)) {
                        alertLogService.record(buildLog(
                                resourceType, fileName, "skip",
                                rules.size(), null,
                                cleanRemote, renderedYaml,
                                false, false,
                                true, "规则内容未变化，跳过同步"
                        ));
                        continue;
                    }
                }

                // 4️⃣ 生成发布ID
                String publishId = generatePublishId();

                // 5️⃣ 添加发布标识 & 同步远端
                String finalContent = addPublishId(renderedYaml, publishId);
                syncToRemote(fileName, finalContent, publishId);

                needReload = true;

                // ★ 发布成功 — 记录变更前后内容
                alertLogService.record(buildLog(
                        resourceType, fileName, "publish",
                        rules.size(), publishId,
                        cleanRemote,      // before: 远端旧内容（可能为 null 表示新文件）
                        renderedYaml,     // after:  本次渲染内容
                        true, false,
                        true, null
                ));

            } catch (Exception e) {
                // ★ 发布失败 — 同样记录内容，方便排查
                String remoteFallback = null;
                try {
                    String rc = sshFileService.readRuleFile(fileName);
                    if (rc != null) remoteFallback = removePublishId(rc);
                } catch (Exception ignored) {}

                alertLogService.record(buildLog(
                        resourceType, fileName, "publish",
                        rules.size(), null,
                        remoteFallback, renderedYaml,
                        true, false,
                        false, e.getMessage()
                ));
                throw e;
            }
        }

        // 7️⃣ 统一 reload
        if (needReload) {
            try {
                prometheusService.reload();
                alertLogService.record(buildLog(
                        "ALL", "prometheus", "reload",
                        0, null, null, null,
                        false, true,
                        true, "Prometheus reload 成功"
                ));
            } catch (Exception e) {
                alertLogService.record(buildLog(
                        "ALL", "prometheus", "reload",
                        0, null, null, null,
                        false, true,
                        false, "Prometheus reload 失败: " + e.getMessage()
                ));
                throw e;
            }
        }
    }

    // ==================== 日志构建 ====================

    private AlertLog buildLog(String resourceType,
                              String fileName,
                              String action,
                              int rulesCount,
                              String publishId,
                              String contentBefore,
                              String contentAfter,
                              boolean contentChanged,
                              boolean reloaded,
                              boolean status,
                              String message) {

        AlertLog log = new AlertLog();
        log.setResourceType(resourceType);
        log.setFileName(fileName);
        log.setAction(action);
        log.setRulesCount(rulesCount);
        log.setPublishId(publishId);
        log.setContentBefore(contentBefore);
        log.setContentAfter(contentAfter);
        log.setContentChanged(contentChanged);
        log.setReloaded(reloaded);
        log.setOperator("admin");
        log.setStatus(status);
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        return log;
    }

    // ==================== 辅助方法 ====================

    private String generatePublishId() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String addPublishId(String content, String publishId) {
        return "# PUBLISH_ID: " + publishId + "\n" + content;
    }

    private String removePublishId(String content) {
        return content.replaceFirst("^# PUBLISH_ID: .*\\n", "");
    }

    private void syncToRemote(String fileName, String content, String publishId) {
        int retry = 0;
        int maxRetry = 3;
        while (retry < maxRetry) {
            sshFileService.writeRuleFile(fileName, content);
            String remoteContent = sshFileService.readRuleFile(fileName);
            if (remoteContent != null && remoteContent.contains(publishId)) {
                return;
            }
            retry++;
        }
        throw new IllegalStateException("规则文件同步失败：" + fileName);
    }
}