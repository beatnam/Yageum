package com.yageum.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ChartDTO;



@Mapper
@Repository
public interface ChartMapper {

	List<Map<String, Object>> sumExpenseByMember(ChartDTO chartDTO);

	Map<String, Object> plusMember(ChartDTO chartDTO);

	Map<String, Object> minusMember(ChartDTO chartDTO);

	
	

	
}
