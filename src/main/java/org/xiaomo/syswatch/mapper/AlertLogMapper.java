package org.xiaomo.syswatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaomo.syswatch.domain.entity.AlertLog;

@Mapper
public interface AlertLogMapper extends BaseMapper<AlertLog> {
    // 不需要自己写 insert 或 selectPage
}
