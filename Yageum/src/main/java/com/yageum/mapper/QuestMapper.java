package com.yageum.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ItemDTO;
import com.yageum.domain.QuestStateDTO;

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

}
