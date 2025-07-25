package com.yageum.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.domain.ItemDTO;
import com.yageum.domain.QuestStateDTO;
import com.yageum.domain.QuestSuccessDTO;
import com.yageum.domain.SumSearchDTO;
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

	public List<Map<Object, Object>> listQuest(int memberIn) {
		log.info("QuestService listQuest()");
		return questMapper.listQuest(memberIn);
	}

	public List<Map<Object, Object>> myQuest(int memberIn) {
		log.info("QuestService myQuest()");
		return questMapper.myQuest(memberIn);
	}

	public List<ItemDTO> listItem() {
		log.info("QuestService listItem()");
		return questMapper.listItem();

	}

	public int myReward(String memberId) {
		// TODO Auto-generated method stub
		return questMapper.myReward(memberId);
	}

	public void buyItemUser(Map<String, Object> buyItemUser) {

		questMapper.buyItemUser(buyItemUser);
	}

	public void buyItemTransaction(Map<String, Object> buyItemTransaction) {
		questMapper.buyItemTransaction(buyItemTransaction);

	}

	public ItemDTO getItemInfo(ItemDTO itemDTO) {
		// TODO Auto-generated method stub
		return questMapper.getItemInfo(itemDTO);
	}

	public Map<Object, Object> questType2() {
		// TODO Auto-generated method stub
		return questMapper.questType2();
	}

	public List<Map<String, Object>> listQuestType2(int memberIn) {
		// TODO Auto-generated method stub
		return questMapper.listQuestType2(memberIn);
	}

	public void successQuestType(QuestSuccessDTO questSuccessDTO) {

		questMapper.successQuestType(questSuccessDTO);

	}

	public void memberReward(QuestSuccessDTO questSuccessDTO) {
		
		questMapper.memberReward(questSuccessDTO);
		
	}

	public List<Map<String, Object>> listQuestType3(int memberIn) {
		// TODO Auto-generated method stub
		return questMapper.listQuestType3(memberIn);
	}



	public List<Map<String, Object>> listQuestType4() {
		// TODO Auto-generated method stub
		return questMapper.listQuestType4();
	}

	public int searchQuestCategory(int questIn) {
		// TODO Auto-generated method stub
		return questMapper.searchQuestCategory(questIn);
	}

	public int sumFromExpense(SumSearchDTO sumSearchDTO) {
		// TODO Auto-generated method stub
		return questMapper.sumFromExpense(sumSearchDTO);
	}



}
