package com.yageum.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.entity.Bank;
import com.yageum.entity.BankAccount;
import com.yageum.mapper.OpenbankingMapper;
import com.yageum.repository.BankAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class OpenBankingService {
	
	// 객체 생성
	private final OpenBankingApiClient openBankingApiClient;
	private final OpenbankingMapper openbankingMapper;
	private final BankAccountRepository bankAccountRepository;
	
	// 토큰 발급 요청
	public Map<String, String> requestToken(Map<String, String> map){
		
		return openBankingApiClient.requestToken(map);
	}
	
	// 사용자 정보 조회
	public Map<String, String> getUserInfo(Map<String, String> map) {
		
		return openBankingApiClient.getUserInfo(map);
	}

	// 오픈뱅킹 계좌 목록 불러오기
	public Map<String, Object> accountList(Map<String, String> map) {

		return openBankingApiClient.accountList(map);	
	}

	// 오픈뱅킹 계좌 db에 저장
	public void saveAccounts(List<String> selectedAccounts, List<String> accountNames, int memberIn) {
		log.info("OpenBankingService saveAccounts()");
	    for (int i = 0; i < selectedAccounts.size(); i++) {
	        System.out.println("입력값: " + selectedAccounts.get(i));

	        String[] parts = selectedAccounts.get(i).split("\\|");
	        if (parts.length < 2) {
	            System.out.println("🔴split 실패 (parts.length < 2)");
	            continue;
	        }

	        String accountNum = parts[0].trim();
	        String bankName = parts[1].trim();
	        String accountName = (i < accountNames.size()) ? accountNames.get(i) : "";

	        System.out.println("🟢추출: " + accountNum + " / " + bankName + " / " + accountName);

	        // 1. 은행 처리
	        Bank bank = openbankingMapper.findByName(bankName);
	        if (bank == null) {
	            System.out.println("🔵은행 없음 → insert 시도");
	            bank = new Bank();
	            bank.setBankName(bankName);
	            openbankingMapper.insertBank(bank);
	            bank = openbankingMapper.findByName(bankName);
	        }

	        if (bank == null || bank.getBankIn() == 0) {
	            System.out.println("🔴은행 조회 실패 → 저장 중단");
	            continue;
	        }

	        // 2. 계좌 저장
	        BankAccount account = new BankAccount();
	        account.setMemberIn(memberIn);
	        account.setBankIn(bank.getBankIn());
	        account.setAccountNum(accountNum);
	        account.setAccountName(accountName);
	        account.setAccountAlias(null);
	        account.setCreateDate(LocalDate.now());

	        openbankingMapper.insertBankAccount(account);
	        System.out.println("✅저장 완료: " + accountNum + " / " + bankName + " / " + accountName);
	    }
	}

	// 계좌 중복 조회
	public boolean isAccountExists(String accountNum) {
		return bankAccountRepository.existsByAccountNum(accountNum);
	}
////	등록계좌 조회
//	public Map<String, String> accountList(Map<String, String> map) {
//		
//		return openBankingApiClient.accountList(map);
//	}
	
}
