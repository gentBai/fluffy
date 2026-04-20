package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.ApiKey;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApiKeyMapper extends BaseMapper<ApiKey> {

    @Select("SELECT * FROM api_key WHERE key_value = #{keyValue} AND deleted = 0")
    ApiKey findByKeyValue(String keyValue);

    @Select("SELECT * FROM api_key WHERE user_id = #{userId} AND active = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<ApiKey> findActiveByUserId(Long userId);

    @Select("SELECT * FROM api_key WHERE deleted = 0 ORDER BY created_at DESC")
    List<ApiKey> findAll();
}