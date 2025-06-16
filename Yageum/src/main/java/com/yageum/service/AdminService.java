package com.yageum.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
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

}
