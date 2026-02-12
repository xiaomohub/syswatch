package org.xiaomo.syswatch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xiaomo.syswatch.domain.entity.AlertLog;

public interface AlertLogService {
    void record(String module, String alertId, String action, String operator, boolean status, String message);

    Page<AlertLog> listLogs(int pageNum, int pageSize);
}
