package org.xiaomo.syswatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaomo.syswatch.domain.entity.AlertEvent;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {

}