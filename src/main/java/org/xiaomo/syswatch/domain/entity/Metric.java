package org.xiaomo.syswatch.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "metric")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "mem_usage")
    private Double memUsage;

    @Column(name = "disk_usage")
    private Double diskUsage;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "collect_time")
    private LocalDateTime collectTime;

    @PrePersist
    public void prePersist() {
        collectTime = LocalDateTime.now();
    }
}
