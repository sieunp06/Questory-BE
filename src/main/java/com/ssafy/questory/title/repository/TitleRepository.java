package com.ssafy.questory.title.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TitleRepository {
    String findNameById(@Param("titleId") Long titleId);
}
