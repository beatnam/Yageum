package com.yageum.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.yageum.entity.Card;
import com.yageum.entity.CardCompany;
import com.yageum.mapper.MypageMapper;
import com.yageum.repository.CardCorporationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class MypageService {
	
	private final MypageMapper mypageMapper;
	private final CardCorporationRepository cardCorporationRepository;
	
	public void deleteAccountById(int id) {
		log.info("ExpenseService deleteAccountById()");
		
		mypageMapper.deleteAccountById(id);
		
	}

	public void deleteCardById(int id) {
		log.info("ExpenseService deleteCardById()");
		
		mypageMapper.deleteCardById(id);
	
	}

	public List<CardCompany> getCardCorporationList() {
		log.info("ExpenseService getCardCorporationList()");
		
		return cardCorporationRepository.findAll();
	}

	public void minsertPro(Card card) {
		log.info("MypageService minsertPro() - {}", card);
		
		mypageMapper.minsertPro(card);
	}

	

	  

}
