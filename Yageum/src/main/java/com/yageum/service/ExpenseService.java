package com.yageum.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
import com.yageum.entity.Expense;
import com.yageum.repository.CategoryMainRepository;
import com.yageum.repository.CategorySubRepository;
import com.yageum.repository.ExpenseRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Transactional
@Service
@Log
@RequiredArgsConstructor
public class ExpenseService {

	 private final ExpenseRepository expenseRepository;
	 private final CategoryMainRepository categoryMainRepository;
	 private final CategorySubRepository categorySubRepository;

	    public void saveExpense(Expense expense) {
	        expenseRepository.save(expense);
	    }
	    
	 // 대분류 전체 가져오기
	    public List<CategoryMainDTO> getAllMainCategories() {
	        List<CategoryMain> mainList = categoryMainRepository.findAll();
	        List<CategoryMainDTO> dtoList = new ArrayList<>();

	        for (CategoryMain main : mainList) {
	            CategoryMainDTO cmDTO = new CategoryMainDTO();
	            cmDTO.setCmIn(main.getCmIn());
	            cmDTO.setCmName(main.getCmName());
	            dtoList.add(cmDTO);
	        }

	        return dtoList;
	    }

	    // 대분류 ID로 소분류 목록 가져오기
	    public List<CategorySubDTO> getSubCategoriesByCmIn(int cmIn) {
	        List<CategorySub> subList = categorySubRepository.findByCmIn(cmIn);
	        System.out.println("subList size: " + subList.size());
	        
	        List<CategorySubDTO> dtoList = new ArrayList<>();

	        for (CategorySub sub : subList) {
	            CategorySubDTO csDTO = new CategorySubDTO();
	            csDTO.setCsIn(sub.getCsIn());
	            csDTO.setCmIn(sub.getCmIn());
	            csDTO.setCsName(sub.getCsName());
	            dtoList.add(csDTO);
	        }

	        return dtoList;
	    }
	
	
}
