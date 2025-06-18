package com.yageum.service;

import org.springframework.stereotype.Service;

import com.yageum.domain.QuestStateDTO;
import com.yageum.mapper.QuestMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class QuestService {

	private final QuestMapper questMapper;

	public int searchMemberIn(String memberId) {

		return questMapper.searchMemberIn(memberId);
	}

	public void acceptQuest(QuestStateDTO questStateDTO) {
		log.info("QuestService acceptQuest()");
		questStateDTO.setQpIn(2);
		questStateDTO.setQsSuccessDate(null);
		questStateDTO.setRewardDate(null);

		questMapper.acceptQuest(questStateDTO);
	}

}
