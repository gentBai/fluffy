package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.ServiceInstance;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ServiceInstanceMapper extends BaseMapper<ServiceInstance> {

    @Select("SELECT * FROM service_instance WHERE service_id = #{serviceId} AND healthy = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<ServiceInstance> findHealthyByServiceId(Long serviceId);

    @Select("SELECT * FROM service_instance WHERE service_id = #{serviceId} AND deleted = 0 ORDER BY created_at DESC")
    List<ServiceInstance> findByServiceId(Long serviceId);

    @Select("SELECT * FROM service_instance WHERE deleted = 0 ORDER BY created_at DESC")
    List<ServiceInstance> findAll();
}