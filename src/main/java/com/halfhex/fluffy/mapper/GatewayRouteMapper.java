package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.GatewayRoute;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GatewayRouteMapper extends BaseMapper<GatewayRoute> {

    @Select("SELECT * FROM gateway_route WHERE deleted = 0 ORDER BY priority DESC")
    List<GatewayRoute> findAllEnabled();

    @Select("SELECT * FROM gateway_route WHERE path_pattern = #{pathPattern} AND http_method = #{method} AND deleted = 0")
    GatewayRoute findByPathAndMethod(String pathPattern, String method);
}