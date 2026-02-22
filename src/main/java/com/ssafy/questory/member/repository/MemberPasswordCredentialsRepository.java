package com.ssafy.questory.member.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberPasswordCredentialsRepository {
    int register(@Param("memberId") Long memberId, @Param("passwordHash") String password);
}
