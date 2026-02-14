package org.xiaomo.syswatch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xiaomo.syswatch.domain.entity.AlertLog;

public interface AlertLogService {

    void record(AlertLog log);

    Page<AlertLog> listLogs(int pageNum, int pageSize);
}