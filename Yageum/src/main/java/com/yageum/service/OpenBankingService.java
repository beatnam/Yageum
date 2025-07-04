package com.yageum.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.entity.Bank;
import com.yageum.entity.BankAccount;
import com.yageum.mapper.OpenbankingMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class OpenBankingService {
	
	// 객체 생성
	private final OpenBankingApiClient openBankingApiClient;
	private final OpenbankingMapper openbankingMapper;
	
	// 토큰 발급 요청
	public Map<String, String> requestToken(Map<String, String> map){
		
		return openBankingApiClient.requestToken(map);
	}
	
	// 사용자 정보 조회
	public Map<String, String> getUserInfo(Map<String, String> map) {
		
		return openBankingApiClient.getUserInfo(map);
	}

	public Map<String, Object> accountList(Map<String, String> map) {

		return openBankingApiClient.accountList(map);	
	}

	public void saveAccounts(List<String> selectedAccounts, List<String> accountNames, int memberIn) {
	    for (int i = 0; i < selectedAccounts.size(); i++) {
	        String[] parts = selectedAccounts.get(i).split(",");
	        String accountNum = parts[0];
	        String bankName = parts[1];
	        String accountName = accountNames.get(i);  // 사용자 입력값

	        // 1. 은행명으로 bank_in 조회 or insert
	        Bank bank = openbankingMapper.findByName(bankName);
	        if (bank == null) {
	            bank = new Bank();
	            bank.setBankName(bankName);
	            openbankingMapper.insertBank(bank);
	        }

	        // 2. 계좌 저장
	        BankAccount account = new BankAccount();
	        account.setMemberIn(memberIn);
	        account.setBankIn(bank.getBankIn());
	        account.setAccountNum(accountNum);
	        account.setAccountName(accountName);
	        account.setAccountAlias(null); // 별칭은 없음
	        account.setCreateDate(LocalDate.now());

	        openbankingMapper.insertBankAccount(account);
	    }
	}
	
////	등록계좌 조회
//	public Map<String, String> accountList(Map<String, String> map) {
//		
//		return openBankingApiClient.accountList(map);
//	}
	
}
