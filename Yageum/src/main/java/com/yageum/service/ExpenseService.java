package com.yageum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yageum.domain.BankAccountDTO;
import com.yageum.domain.CardDTO;
import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.ExpenseDTO;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
import com.yageum.entity.Expense;
import com.yageum.repository.BankAccountRepository;
import com.yageum.repository.CardRepository;
import com.yageum.repository.CategoryMainRepository;
import com.yageum.repository.CategorySubRepository;
import com.yageum.repository.ExpenseRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ExpenseService {

	 private final ExpenseRepository expenseRepository;
	 private final CategoryMainRepository categoryMainRepository;
	 private final CategorySubRepository categorySubRepository;
	 private final CardRepository cardRepository;
	 private final BankAccountRepository bankAccountRepository;
	 private final CashbookMapper cashbookMapper;

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

	    // 대분류 id로 소분류 목록 가져오기
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

	    //수단에서 카드 선택하면 카드 리스트 / 계좌 선택하면 계좌 리스트 출력
	    public List<Map<String, Object>> findMethodListByTypeAndMember(String type, int memberIn) {
	        if ("card".equals(type)) {
	            List<Card> cards = cardRepository.findByMemberIn(memberIn);
	            if (cards == null) return Collections.emptyList();

	            return cards.stream()
	                .map(card -> {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("id", card.getCardIn());
	                    map.put("name", card.getCardName());
	                    return map;
	                })
	                .collect(Collectors.toList());

	        } else if ("account".equals(type)) {
	            List<BankAccount> accounts = bankAccountRepository.findByMemberIn(memberIn);
	            if (accounts == null) return Collections.emptyList();

	            return accounts.stream()
	                .map(acc -> {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("id", acc.getAccountIn());
	                    map.put("name", acc.getBankIn() + " (" + acc.getAccountNum() + ")");
	                    return map;
	                })
	                .collect(Collectors.toList());
	        }

	        return Collections.emptyList(); // 현금일때
	    }

	    //카드 리스트
	    public List<CardDTO> findCardListByMethodAndMember(int methodIn, int memberIn) {
	    	log.info("ExpenseService findCardListByMethodAndMember()");
	    	log.info("쿼리 전 memberIn={}, methodIn={}", memberIn, methodIn);
	    	
	    	List<Card> cards = cardRepository.findCardsByMemberInAndMethodIn(memberIn, methodIn);
	    	
	    	log.info("쿼리 후 카드 리스트: {}", cards);
	        log.info("카드 개수: " + cards.size());  
	        
	        return cards.stream()
	                    .map(CardDTO::new)
	                    .collect(Collectors.toList());
	    }

	    //계좌 리스트
		public List<BankAccountDTO> findAccountListByMember(int memberIn) {
			log.info("ExpenseService findAccountListByMember()");
			log.info("쿼리 전 memberIn={}", memberIn);
			
			List<BankAccount> accounts = bankAccountRepository.findByMemberIn(memberIn);
		    
			log.info("쿼리 후 계좌 리스트: {}", accounts);
	        log.info("계좌 개수: " + accounts.size()); 
			
			return accounts.stream()
		                   .map(BankAccountDTO::new)
		                   .collect(Collectors.toList());
			
		}
		
		// 날짜별 전체 수입/지출 내역 가져오기
		public List<Expense> findByDate(int memberIn, String dateStr) {
			LocalDate date = LocalDate.parse(dateStr);
		    return expenseRepository.findByMemberInAndDate(memberIn, date);
		}

		// 날짜별 수입 or 지출 합계 구하기 (expenseType: 0=수입, 1=지출)
		public int getDailyTotal(int memberIn, String dateStr, int type) {
			LocalDate date = LocalDate.parse(dateStr);
			boolean expenseType = (type == 1); // 1이면 지출(true), 0이면 수입(false)
		    return expenseRepository.sumExpenseByDateAndType(memberIn, date, expenseType);
		}

		public ExpenseDTO getExpenseDetailById(int id) {
			return cashbookMapper.selectExpenseDetail(id);
		}
	
	    
	
}
