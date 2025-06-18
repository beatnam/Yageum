package com.yageum.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.QuestDTO;
import com.yageum.mapper.AdminMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class AdminService {

	private final AdminMapper adminMapper;

	public List<CategoryMainDTO> showCategoryMain() {
		log.info("AdminService showCategoryMain()");
		return adminMapper.showCategoryMain();
	}

	public List<CategorySubDTO> subCategorySelect(int cmIn) {
		log.info("AdminService subCategorySelect()");
		return adminMapper.subCategorySelect(cmIn);
	}

	public void insertQuest(QuestDTO questDTO) {
		log.info("AdminService insertQuest()");

		adminMapper.insertQuest(questDTO);
	}

	public List<Map<Object, Object>> listQuest() {
		// TODO Auto-generated method stub
		return adminMapper.listQuest();
	}

	public QuestDTO questDetail(int questIn) {
		// TODO Auto-generated method stub
		return adminMapper.questDetail(questIn);
	}

	public void updateQuest(QuestDTO questDTO) {

		adminMapper.updateQuest(questDTO);

	}

	public void deleteQuest(int questIn) {
		
		adminMapper.deleteQuest(questIn);// TODO Auto-generated method stub

	}

	public void changeQuestVaild() {
		adminMapper.changeQuestValid();
		
	}

}
