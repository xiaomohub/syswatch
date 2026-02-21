package org.xiaomo.syswatch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaomo.syswatch.config.AlertmanagerProperties;
import org.xiaomo.syswatch.domain.dto.AlertEventSilenceDTO;
import org.xiaomo.syswatch.domain.entity.AlertEvent;
import org.xiaomo.syswatch.domain.vo.AlertmanagerAlertVO;
import org.xiaomo.syswatch.domain.vo.AlertmanagerSilenceVO;
import org.xiaomo.syswatch.mapper.AlertEventMapper;
import org.xiaomo.syswatch.service.AlertmanagerSilenceService;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class AlertmanagerSilenceServiceImpl implements AlertmanagerSilenceService {

    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Resource
    private AlertmanagerProperties alertmanagerProperties;

    @Resource
    private AlertEventMapper alertEventMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==============================
    // 创建静默
    // ==============================

    @Override
    public String createSilenceForAlert(AlertEventSilenceDTO dto) {

        String alertName;
        String instance;

        if (dto.getAlertEventId() != null) {
            AlertEvent event = alertEventMapper.selectById(dto.getAlertEventId());
            if (event == null) {
                throw new IllegalArgumentException("告警事件不存在: " + dto.getAlertEventId());
            }
            alertName = event.getAlertName();
            instance = extractInstanceFromLabels(event.getLabels());
        } else if (dto.getAlertName() != null) {
            alertName = dto.getAlertName();
            instance = dto.getInstance();
        } else {
            throw new IllegalArgumentException("必须提供 alertEventId 或 alertName");
        }

        return createSilence(alertName, instance,
                dto.getDurationMinutes(),
                dto.getComment(),
                dto.getCreatedBy());
    }

    @Override
    public String createSilence(String alertName,
                                String instance,
                                Integer durationMinutes,
                                String comment,
                                String createdBy) {

        OffsetDateTime now = OffsetDateTime.now(CHINA_ZONE);
        OffsetDateTime endsAt = now.plusMinutes(durationMinutes);

        Map<String, Object> request = new HashMap<>();
        request.put("startsAt", now.format(ISO_FORMATTER));
        request.put("endsAt", endsAt.format(ISO_FORMATTER));
        request.put("createdBy", createdBy == null ? "syswatch" : createdBy);
        request.put("comment", comment == null ? "通过SysWatch创建的静默" : comment);

        List<Map<String, Object>> matchers = new ArrayList<>();

        matchers.add(buildMatcher("alertname", alertName));

        if (instance != null && !instance.isEmpty()) {
            matchers.add(buildMatcher("instance", instance));
        }

        request.put("matchers", matchers);

        String url = alertmanagerProperties.getApiBaseUrl() + "/silences";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity =
                    new HttpEntity<>(JSON.toJSONString(request), headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject result = JSON.parseObject(response.getBody());
                String silenceId = result.getString("silenceID");

                log.info("创建静默成功 silenceId={}", silenceId);
                return silenceId;
            }

            throw new RuntimeException("创建静默失败: " + response.getBody());

        } catch (Exception e) {
            log.error("调用 Alertmanager 创建静默失败", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> buildMatcher(String name, String value) {
        Map<String, Object> matcher = new HashMap<>();
        matcher.put("name", name);
        matcher.put("value", value);
        matcher.put("isRegex", false);
        matcher.put("isEqual", true);
        return matcher;
    }

    // ==============================
    // 删除静默
    // ==============================

    @Override
    public void deleteSilence(String silenceId) {

        String url = alertmanagerProperties.getApiBaseUrl()
                + "/silence/" + silenceId;

        try {
            restTemplate.delete(url);
            log.info("删除静默成功 {}", silenceId);
        } catch (Exception e) {
            log.error("删除静默失败", e);
            throw new RuntimeException(e);
        }
    }

    // ==============================
    // 查询静默
    // ==============================

    @Override
    public List<AlertmanagerSilenceVO> getActiveSilences() {

        String url = alertmanagerProperties.getApiBaseUrl() + "/silences";

        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return Collections.emptyList();
            }

            JSONArray array = JSON.parseArray(response.getBody());
            List<AlertmanagerSilenceVO> result = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                AlertmanagerSilenceVO silence =
                        parseSilence(array.getJSONObject(i));

                if ("active".equalsIgnoreCase(silence.getStatus())) {
                    result.add(silence);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("查询静默失败", e);
            return Collections.emptyList();
        }
    }

    // ==============================
    // 查询告警
    // ==============================

    @Override
    public List<AlertmanagerAlertVO> getFiringAlerts() {

        String url = alertmanagerProperties.getApiBaseUrl() + "/alerts";

        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return Collections.emptyList();
            }

            JSONArray array = JSON.parseArray(response.getBody());
            List<AlertmanagerAlertVO> result = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {

                AlertmanagerAlertVO alert =
                        parseAlert(array.getJSONObject(i));

                if ("active".equalsIgnoreCase(alert.getStatus())) {
                    result.add(alert);
                }
            }

            // 判断是否被静默
            List<AlertmanagerSilenceVO> silences = getActiveSilences();

            for (AlertmanagerAlertVO alert : result) {

                boolean silenced = false;

                for (AlertmanagerSilenceVO silence : silences) {
                    if (matchesSilence(silence, alert.getLabels())) {
                        silenced = true;
                        alert.setSilenceId(silence.getId());
                        break;
                    }
                }

                alert.setSilenced(silenced);
            }

            return result;

        } catch (Exception e) {
            log.error("查询告警失败", e);
            return Collections.emptyList();
        }
    }

    // ==============================
    // 匹配静默
    // ==============================

    private boolean matchesSilence(AlertmanagerSilenceVO silence,
                                   Map<String, String> alertLabels) {

        if (silence.getMatchers() == null || alertLabels == null) {
            return false;
        }

        for (AlertmanagerSilenceVO.Matcher matcher : silence.getMatchers()) {

            String labelValue = alertLabels.get(matcher.getName());
            if (labelValue == null) {
                return false;
            }

            if (Boolean.TRUE.equals(matcher.getIsRegex())) {
                if (!labelValue.matches(matcher.getValue())) {
                    return false;
                }
            } else {
                if (!labelValue.equals(matcher.getValue())) {
                    return false;
                }
            }
        }

        return true;
    }

    // ==============================
    // 解析 Alert
    // ==============================

    private AlertmanagerAlertVO parseAlert(JSONObject obj) {

        AlertmanagerAlertVO alert = new AlertmanagerAlertVO();

        alert.setFingerprint(obj.getString("fingerprint"));

        JSONObject status = obj.getJSONObject("status");
        if (status != null) {
            alert.setStatus(status.getString("state"));
        }

        JSONObject labels = obj.getJSONObject("labels");
        if (labels != null) {
            Map<String, String> map =
                    labels.toJavaObject(Map.class);
            alert.setLabels(map);
        }

        JSONObject annotations = obj.getJSONObject("annotations");
        if (annotations != null) {
            alert.setAnnotations(
                    annotations.toJavaObject(Map.class));
        }

        parseTime(obj, alert);

        alert.setGeneratorURL(obj.getString("generatorURL"));

        return alert;
    }

    private void parseTime(JSONObject obj,
                           AlertmanagerAlertVO alert) {

        try {
            String startsAt = obj.getString("startsAt");
            if (startsAt != null) {
                alert.setStartsAt(
                        OffsetDateTime.parse(startsAt));
            }

            String endsAt = obj.getString("endsAt");
            if (endsAt != null) {
                alert.setEndsAt(
                        OffsetDateTime.parse(endsAt));
            }
        } catch (Exception ignore) {
        }
    }

    // ==============================
    // 解析 Silence
    // ==============================

    private AlertmanagerSilenceVO parseSilence(JSONObject obj) {

        AlertmanagerSilenceVO silence =
                new AlertmanagerSilenceVO();

        silence.setId(obj.getString("id"));

        JSONObject status = obj.getJSONObject("status");
        if (status != null) {
            silence.setStatus(status.getString("state"));
        }

        JSONArray matchersArray =
                obj.getJSONArray("matchers");

        if (matchersArray != null) {

            List<AlertmanagerSilenceVO.Matcher> list =
                    new ArrayList<>();

            for (int i = 0; i < matchersArray.size(); i++) {

                JSONObject m = matchersArray.getJSONObject(i);

                AlertmanagerSilenceVO.Matcher matcher =
                        new AlertmanagerSilenceVO.Matcher();

                matcher.setName(m.getString("name"));
                matcher.setValue(m.getString("value"));
                matcher.setIsRegex(m.getBoolean("isRegex"));
                matcher.setIsEqual(m.getBoolean("isEqual"));

                list.add(matcher);
            }

            silence.setMatchers(list);
        }

        try {
            String startsAt = obj.getString("startsAt");
            if (startsAt != null) {
                silence.setStartsAt(
                        OffsetDateTime.parse(startsAt));
            }

            String endsAt = obj.getString("endsAt");
            if (endsAt != null) {
                silence.setEndsAt(
                        OffsetDateTime.parse(endsAt));
            }

            String createdAt = obj.getString("createdAt");
            if (createdAt != null) {
                silence.setCreatedAt(
                        OffsetDateTime.parse(createdAt));
            }
        } catch (Exception ignore) {
        }

        silence.setCreatedBy(obj.getString("createdBy"));
        silence.setComment(obj.getString("comment"));

        return silence;
    }

    // ==============================
    // 工具方法
    // ==============================

    @SuppressWarnings("unchecked")
    private String extractInstanceFromLabels(String labelsJson) {

        if (labelsJson == null || labelsJson.isEmpty()) {
            return null;
        }

        try {
            Map<String, String> labels =
                    JSON.parseObject(labelsJson, Map.class);
            return labels.get("instance");
        } catch (Exception e) {
            return null;
        }
    }
    // ==============================
// 查询指定告警是否被静默
// ==============================

    @Override
    public AlertmanagerSilenceVO getSilenceForAlert(Map<String, String> alertLabels) {

        if (alertLabels == null || alertLabels.isEmpty()) {
            return null;
        }

        List<AlertmanagerSilenceVO> silences = getActiveSilences();

        for (AlertmanagerSilenceVO silence : silences) {
            if (matchesSilence(silence, alertLabels)) {
                return silence;
            }
        }

        return null;
    }

}
