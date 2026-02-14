package org.xiaomo.syswatch.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_log")
public class AlertLog {

    /**
     * ★ 雪花ID超过 JS Number.MAX_SAFE_INTEGER (2^53 = 9007199254740993)
     *   必须序列化为 String，否则前端精度丢失，导致请求详情时 id 对不上
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 资源类型：system / app / custom / ALL */
    private String resourceType;

    /** 规则文件名 */
    private String fileName;

    /** 动作：publish / reload / skip */
    private String action;

    /** 本次涉及的规则数量 */
    private Integer rulesCount;

    /** 发布标识 */
    private String publishId;

    /** 变更前的规则内容（远端旧文件） */
    private String contentBefore;

    /** 变更后的规则内容（本次渲染结果） */
    private String contentAfter;

    /** 内容是否有变更 */
    private Boolean contentChanged;

    /** 是否触发了 Prometheus reload */
    private Boolean reloaded;

    /** 操作人 */
    private String operator;

    /** 1=成功 / 0=失败 */
    private Boolean status;

    /** 失败原因 / 备注 */
    private String message;

    /** 操作时间 */
    private LocalDateTime timestamp;
}