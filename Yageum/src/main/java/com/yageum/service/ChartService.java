package com.yageum.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.domain.ChartDTO;
import com.yageum.mapper.ChartMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class ChartService {
	
	private final ChartMapper chartMapper;
	
	public List<Map<String, Object>> sumExpenseByMember(ChartDTO chartDTO) {
		// TODO Auto-generated method stub
		return chartMapper.sumExpenseByMember(chartDTO);
	}


	public Map<String, Object> plusMember(ChartDTO chartDTO) {
		// TODO Auto-generated method stub
	    Map<String, Object> result = chartMapper.plusMember(chartDTO);
	    if (result == null) result = new HashMap<>();
	    result.putIfAbsent("plus", 0);

		return result;
	}

	public Map<String, Object> minusMember(ChartDTO chartDTO) {
	    Map<String, Object> result = chartMapper.minusMember(chartDTO);
	    if (result == null) result = new HashMap<>();
	    result.putIfAbsent("minus", 0);

		return result;
	}



}
