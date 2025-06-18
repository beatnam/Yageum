package com.yageum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.QuestStateDTO;

@Mapper
@Repository
public interface QuestMapper {

	int searchMemberIn(String memberId);

	void acceptQuest(QuestStateDTO questStateDTO);

}
