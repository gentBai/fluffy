package com.halfhex.fluffy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halfhex.fluffy.entity.Blacklist;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlacklistMapper extends BaseMapper<Blacklist> {

    @Select("SELECT * FROM blacklist WHERE target_type = #{targetType} AND target_value = #{targetValue} AND deleted = 0 AND (expires_at IS NULL OR expires_at > NOW()) LIMIT 1")
    Blacklist checkIfBlacklisted(String targetType, String targetValue);

    @Select("SELECT * FROM blacklist WHERE deleted = 0 ORDER BY created_at DESC")
    List<Blacklist> findAll();
}