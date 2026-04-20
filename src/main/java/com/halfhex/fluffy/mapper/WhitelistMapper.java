package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.Whitelist;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WhitelistMapper extends BaseMapper<Whitelist> {

    @Select("SELECT * FROM whitelist WHERE target_type = #{targetType} AND target_value = #{targetValue} AND deleted = 0 LIMIT 1")
    Whitelist checkIfWhitelisted(String targetType, String targetValue);

    @Select("SELECT * FROM whitelist WHERE deleted = 0 ORDER BY created_at DESC")
    List<Whitelist> findAll();
}