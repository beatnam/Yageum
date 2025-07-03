package com.yageum.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class OpenBankingService {
	
	// 객체 생성
	private final OpenBankingApiClient openBankingApiClient;
	
	// 토큰 발급 요청
	public Map<String, String> requestToken(Map<String, String> map){
		
		return openBankingApiClient.requestToken(map);
	}
	
	// 사용자 정보 조회
	public Map<String, String> getUserInfo(Map<String, String> map) {
		
		return openBankingApiClient.getUserInfo(map);
	}
	
//	등록계좌 조회
	public Map<String, String> accountList(Map<String, String> map) {
		
		return openBankingApiClient.accountList(map);
	}
	
}
