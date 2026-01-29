package org.xiaomo.syswatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaomo.syswatch.domain.entity.User;
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 就可以直接用 selectOne、insert、updateById 等方法
}
