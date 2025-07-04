package com.yageum.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.service.OpenBankingService;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Member;
import com.yageum.repository.MemberRepository;
import com.yageum.service.ExpenseService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/openbanking/*")
public class OpenBankingController {
	
	private final OpenBankingService openBankingService;
	private final ExpenseService expenseService;
	private final MemberRepository memberRepository;
	

//    private final String REDIRECT_URI = "http://localhost:8080/openbanking/callback"; // 실제 등록된 redirect_uri
//    private final String AUTH_URL = "https://testapi.openbanking.or.kr/oauth/2.0/authorize";
//    private final String SCOPE = "login inquiry transfer";
//    private final String STATE = "20132676"; 
//    
    
	@GetMapping("/callback")
	public String callback(@RequestParam Map<String, String> map, HttpSession session) {
		// 인증 후 응답 메시지
		System.out.println(map);
		
		// 토큰발급 => 처리 OpenBankingService
		Map<String, String> map2 = openBankingService.requestToken(map);
		
		// 세션에 저장
	    session.setAttribute("access_token", map2.get("access_token"));
	    session.setAttribute("user_seq_no", map2.get("user_seq_no"));
		
		// 이동
		return "redirect:/openbanking/userInfo";
	}
	
	
	
	@GetMapping("/userInfo")
	public String getUserInfo(HttpSession session, Model model) {
	    String accessToken = (String) session.getAttribute("access_token");
	    String userSeqNo = (String) session.getAttribute("user_seq_no");

	    log.info("access_token: " + accessToken);
	    log.info("user_seq_no: " + userSeqNo);

	    Map<String, String> param = Map.of(
	        "access_token", accessToken,
	        "user_seq_no", userSeqNo
	    );

	    Map<String, String> userInfo = openBankingService.getUserInfo(param);
	    model.addAttribute("map2", userInfo);
	    model.addAttribute("access_token", accessToken); 
	    model.addAttribute("user_seq_no", userSeqNo);
	    
	    String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByMemberId(memberId);
		int memberIn = member.getMemberIn();
		
		
		 // 계좌
	    List<BankAccount> accountList = expenseService.getAccountList(memberIn);
	    model.addAttribute("accountList", accountList);
	    
	    
	    
	    return "mypage/mypage_open_result";
	}
	
	
}