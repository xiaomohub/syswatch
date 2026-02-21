package org.xiaomo.syswatch.service;

import org.xiaomo.syswatch.domain.dto.AlertEventSilenceDTO;
import org.xiaomo.syswatch.domain.vo.AlertmanagerAlertVO;
import org.xiaomo.syswatch.domain.vo.AlertmanagerSilenceVO;

import java.util.List;
import java.util.Map;

/**
 * Alertmanager 静默服务接口
 *
 * 直接调用 Alertmanager v2 API：
 * /api/v2/silences
 * /api/v2/alerts
 *
 * 注意：
 * Alertmanager 中 firing 对应 status.state = "active"
 */
public interface AlertmanagerSilenceService {

    /**
     * 对正在告警的事件创建静默
     * 调用：
     * POST /api/v2/silences
     *
     * @param dto 静默请求参数
     * @return Alertmanager 返回的 silenceId
     */
    String createSilenceForAlert(AlertEventSilenceDTO dto);

    /**
     * 根据告警标签创建静默
     *
     * @param alertName 告警名称 (label: alertname)
     * @param instance 实例 (label: instance)
     * @param durationMinutes 静默时长（分钟）
     * @param comment 备注
     * @param createdBy 创建人
     * @return silenceId
     */
    String createSilence(String alertName,
                         String instance,
                         Integer durationMinutes,
                         String comment,
                         String createdBy);

    /**
     * 取消静默
     *
     * 调用：
     * DELETE /api/v2/silences/{silenceId}
     *
     * @param silenceId Alertmanager silenceId
     */
    void deleteSilence(String silenceId);

    /**
     * 查询当前所有活跃（active）静默
     *
     * 调用：
     * GET /api/v2/silences
     *
     * 只返回 status.state = active 的静默
     *
     * @return 静默列表
     */
    List<AlertmanagerSilenceVO> getActiveSilences();

    /**
     * 查询指定告警是否被静默（推荐使用 labels 精确匹配）
     *
     * @param alertLabels 告警的完整 labels
     * @return 匹配的静默信息，未静默返回 null
     */
    AlertmanagerSilenceVO getSilenceForAlert(Map<String, String> alertLabels);

    /**
     * 获取当前所有 firing 告警
     *
     * 调用：
     * GET /api/v2/alerts
     *
     * 实际过滤规则：
     * status.state = active
     *
     * @return 当前活跃告警列表
     */
    List<AlertmanagerAlertVO> getFiringAlerts();
}
