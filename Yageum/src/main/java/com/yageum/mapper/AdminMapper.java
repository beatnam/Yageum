package com.yageum.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.QuestDTO;

@Mapper
@Repository
public interface AdminMapper {

	List<CategoryMainDTO> showCategoryMain();

	List<CategorySubDTO> subCategorySelect(int cmIn);

	void insertQuest(QuestDTO questDTO);

	List<Map<Object, Object>> listQuest();

	QuestDTO questDetail(int questIn);

	void updateQuest(QuestDTO questDTO);

	void deleteQuest(int questIn);

}
