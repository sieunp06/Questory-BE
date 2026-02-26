package com.ssafy.questory.title.repository;

import com.ssafy.questory.title.dto.response.TitleResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberTitleRepository {
    List<TitleResponseDto> findOwnedTitles(Long memberId);
    boolean existsByMemberIdAndTitleId(@Param("memberId") Long memberId,
                                       @Param("titleId") Long titleId);
    int insertMemberTitle(@Param("memberId") Long memberId,
                          @Param("titleId") Long titleId);
}
