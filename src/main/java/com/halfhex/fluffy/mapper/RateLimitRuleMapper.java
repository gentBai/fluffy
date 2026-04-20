package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.RateLimitRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RateLimitRuleMapper extends BaseMapper<RateLimitRule> {

    @Select("SELECT * FROM rate_limit_rule WHERE route_id = #{routeId} AND deleted = 0 ORDER BY created_at DESC")
    List<RateLimitRule> findByRouteId(Long routeId);

    @Select("SELECT * FROM rate_limit_rule WHERE service_id = #{serviceId} AND deleted = 0 ORDER BY created_at DESC")
    List<RateLimitRule> findByServiceId(Long serviceId);

    @Select("SELECT * FROM rate_limit_rule WHERE deleted = 0 ORDER BY created_at DESC")
    List<RateLimitRule> findAllEnabled();
}