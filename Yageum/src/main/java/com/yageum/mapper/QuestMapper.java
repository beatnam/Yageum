package com.yageum.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ItemDTO;
import com.yageum.domain.QuestStateDTO;
import com.yageum.domain.QuestSuccessDTO;
import com.yageum.domain.SumSearchDTO;

@Mapper
@Repository
public interface QuestMapper {

	int searchMemberIn(String memberId);

	void acceptQuest(QuestStateDTO questStateDTO);

	List<Map<Object, Object>> listQuest(int memberIn);

	List<Map<Object, Object>> myQuest(int memberIn);

	List<ItemDTO> listItem();

	int myReward(String memberId);

	void buyItemUser(Map<String, Object> buyItemUser);

	void buyItemTransaction(Map<String, Object> buyItemTransaction);

	ItemDTO getItemInfo(ItemDTO itemDTO);

	Map<Object, Object> questType2();

	List<Map<String, Object>> listQuestType2(int memberIn);
	
	List<Map<String, Object>> listQuestType3(int memberIn);

	void successQuestType(QuestSuccessDTO questSuccessDTOn);

	void memberReward(QuestSuccessDTO questSuccessDTO);

	List<Map<String, Object>> listQuestType4();

	int searchQuestCategory(int questIn);

	int sumFromExpense(SumSearchDTO sumSearchDTO);

	List<QuestStateDTO> successQuest(int qpIn);

	


}