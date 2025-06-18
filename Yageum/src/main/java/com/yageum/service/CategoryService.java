package com.yageum.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
import com.yageum.repository.CategoryMainRepository;
import com.yageum.repository.CategorySubRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Transactional
@Service
@Log
@RequiredArgsConstructor
public class CategoryService {
	
	private final CategoryMainRepository categoryMainRepository;
	private final CategorySubRepository categorySubRepository;
	
	//카테고리 메인
	public List<CategoryMain> cateMFindAll() {
		log.info("CategoryService cateMFindAll()");
		
		
		return categoryMainRepository.findAll();
	}
	
	
	//카테고리 서브
	public List<CategorySub> cateSFindAll() {
		log.info("CategoryService cateSFindAll()");

		
		return categorySubRepository.findAll();
	}


	public void save(String cmName) {
		log.info("CategoryService save()");

		
		CategoryMain categoryMain = new CategoryMain();
		categoryMain.setCmName(cmName);
		
		categoryMainRepository.save(categoryMain);
		
	}


	public void save2(CategorySub categorySub) {
		log.info("CategoryService save2()");

		categorySubRepository.save(categorySub);
		
		
		
		
	}


	public Optional<CategoryMain> findById1(int cmIn) {
		log.info("CategoryService findById1()");

		
		
		return categoryMainRepository.findById(cmIn);
	}


	public Optional<CategorySub> findById2(int csIn) {
		log.info("CategoryService findById2()");

		
		return categorySubRepository.findById(csIn);
	}






	public CategoryMain find(int cmIn) {
		
		
		return categoryMainRepository.findById(cmIn);
	}



	
}
