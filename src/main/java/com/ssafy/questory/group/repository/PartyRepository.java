package com.ssafy.questory.group.repository;

import com.ssafy.questory.group.domain.Party;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartyRepository {
    void insert(Party party);
}
