package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.GatewayService;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GatewayServiceMapper extends BaseMapper<GatewayService> {

    @Select("SELECT * FROM gateway_service WHERE deleted = 0 ORDER BY created_at DESC")
    List<GatewayService> findAll();
}