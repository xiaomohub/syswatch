package org.xiaomo.syswatch.domain.entity;

/**
 * @author xiaomo
 * @createTime 2026/2/4
 */

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_event")
public class AlertEvent {

    @TableId
    private Long id;

    private String alertName;
    private String severity;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String summary;
    private String description;
    private String labels;      // JSON string
    private String annotations; // JSON string

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
