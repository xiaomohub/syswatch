package org.xiaomo.syswatch.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.xiaomo.syswatch.domain.entity.AlertLog;

@Mapper
public interface AlertLogMapper {

    @Insert("INSERT INTO alert_log(module, alert_id, action, operator, timestamp, status, message) " +
            "VALUES(#{module}, #{alertId}, #{action}, #{operator}, #{timestamp}, #{status}, #{message})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AlertLog log);
}
