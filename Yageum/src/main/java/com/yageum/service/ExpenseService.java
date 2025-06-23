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
import com.yageum.mapper.CashbookMapper;
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

	    
	    // 내역 저장
	    public void saveExpense(Expense expense) {
	        expenseRepository.save(expense);
	    }

	    // 대분류 리스트
	    public List<CategoryMain> getMainCategoryList() {
	        return categoryMainRepository.findAll();
	    }

	    // 소분류 리스트 (대분류 id 기준)
	    public List<CategorySub> getSubCategoryList(int cmIn) {
	        return categorySubRepository.findByCmIn(cmIn);
	    }

	    // 카드 리스트 (수단과 회원번호 기준)
	    public List<Card> getCardList(int memberIn, int methodIn) {
	        return cardRepository.findCardsByMemberInAndMethodIn(memberIn, methodIn);
	    }

	    // 계좌 리스트 (회원번호 기준)
	    public List<BankAccount> getAccountList(int memberIn) {
	        return cashbookMapper.getAccountList(memberIn);
	    }

	    // 날짜별 내역 조회
	    public List<Expense> getExpensesByDate(int memberIn, String dateStr) {
	        LocalDate date = LocalDate.parse(dateStr);
	        return expenseRepository.findByMemberInAndDate(memberIn, date);
	    }

	    // 일일 수입 또는 지출 합계
	    public int getDailyTotal(int memberIn, String dateStr, int type) {
	        LocalDate date = LocalDate.parse(dateStr);
	        boolean isExpense = (type == 1);
	        return expenseRepository.sumExpenseByDateAndType(memberIn, date, isExpense);
	    }
	    
	    // 단일 상세 내역 조회
	    public ExpenseDTO getExpenseDetailById(int id) {
	        return cashbookMapper.getExpenseDetailById(id);
	    }

		public void update(ExpenseDTO expenseDTO) {

			cashbookMapper.updateExpense(expenseDTO);
		}

}
