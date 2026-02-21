package org.xiaomo.syswatch.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiaomo.syswatch.domain.dto.AlertEventSilenceDTO;
import org.xiaomo.syswatch.domain.vo.AlertmanagerAlertVO;
import org.xiaomo.syswatch.domain.vo.AlertmanagerSilenceVO;
import org.xiaomo.syswatch.service.AlertmanagerSilenceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警静默控制器
 * 用于对正在告警的事件进行静默操作
 */
@Slf4j
@RestController
@RequestMapping("/api/alert/silence")
public class AlertSilenceController {

    @Resource
    private AlertmanagerSilenceService alertmanagerSilenceService;

    /**
     * 获取当前所有正在告警的事件
     * 包含是否已被静默的状态
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getFiringAlerts() {
        List<AlertmanagerAlertVO> alerts = alertmanagerSilenceService.getFiringAlerts();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", alerts);
        result.put("total", alerts.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 对告警事件创建静默
     * 静默后该告警在指定时间内不再发送通知
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSilence(
            @Valid @RequestBody AlertEventSilenceDTO dto) {

        String silenceId =
                alertmanagerSilenceService.createSilenceForAlert(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("silenceId", silenceId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", data);

        return ResponseEntity.ok(result);
    }


    /**
     * 快速静默（直接指定告警名称和实例）
     */
    @PostMapping("/quick")
    public ResponseEntity<Map<String, Object>> quickSilence(
            @RequestParam String alertName,
            @RequestParam(required = false) String instance,
            @RequestParam Integer durationMinutes,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String createdBy) {

        String silenceId =
                alertmanagerSilenceService.createSilence(
                        alertName, instance,
                        durationMinutes, comment, createdBy);

        Map<String, Object> data = new HashMap<>();
        data.put("silenceId", silenceId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", data);

        return ResponseEntity.ok(result);
    }

    /**
     * 取消静默（恢复告警通知）
     */
    @DeleteMapping("/{silenceId}")
    public ResponseEntity<Map<String, Object>> deleteSilence(
            @PathVariable String silenceId) {
        alertmanagerSilenceService.deleteSilence(silenceId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "静默已取消");

        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前所有活跃的静默规则
     */
    @GetMapping("/silences")
    public ResponseEntity<Map<String, Object>> getActiveSilences() {
        List<AlertmanagerSilenceVO> silences = alertmanagerSilenceService.getActiveSilences();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", silences);
        result.put("total", silences.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 检查指定告警是否被静默
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSilenced(
            @RequestParam String alertName,
            @RequestParam(required = false) String instance) {

        Map<String, String> labels = new HashMap<>();
        labels.put("alertname", alertName);

        if (instance != null) {
            labels.put("instance", instance);
        }

        AlertmanagerSilenceVO silence =
                alertmanagerSilenceService.getSilenceForAlert(labels);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("silenced", silence != null);
        result.put("silence", silence);

        return ResponseEntity.ok(result);
    }

}