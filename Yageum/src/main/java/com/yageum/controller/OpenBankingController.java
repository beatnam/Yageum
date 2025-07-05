package com.yageum.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
//    주현
    
	// 오픈뱅킹 콜백 (토큰 발급, 세션 저장)
	@GetMapping("/callback")
	public String callback(@RequestParam Map<String, String> map, HttpSession session) {
		log.info("OpenBankingController callback()");
		
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
	
	
	// 가져온 정보로 사용자 이름
	@GetMapping("/userInfo")
	public String getUserInfo(HttpSession session, Model model) {
		log.info("OpenBankingController getUserInfo()");
		
	    String accessToken = (String) session.getAttribute("access_token");
	    String userSeqNo = (String) session.getAttribute("user_seq_no");

	    Map<String, String> userInfo = openBankingService.getUserInfo(Map.of(
	        "access_token", accessToken,
	        "user_seq_no", userSeqNo
	    ));
	    model.addAttribute("map2", userInfo);

	    Map<String, String> accountParam = Map.of(
	        "access_token", accessToken,
	        "user_seq_no", userSeqNo,
	        "include_cancel_yn", "N",
	        "sort_order", "D"
	    );
	    Map<String, Object> accountResponse = openBankingService.accountList(accountParam);
	    List<Map<String, Object>> accountList = (List<Map<String, Object>>) accountResponse.get("res_list");
	    model.addAttribute("accountList", accountList);

	    return "mypage/mypage_open_result";
	}
	
	// 오픈뱅킹으로 가져온 계좌 db에 저장
	@PostMapping("/saveAccounts")
	public String saveSelectedAccounts(@RequestParam("selectedAccounts") List<String> selectedAccounts,
	                                   @RequestParam("accountNames") List<String> accountNames) {
		log.info("OpenBankingController saveSelectedAccounts()");
		
		log.info("accountNames: " + accountNames);
		log.info("selectedAccounts: " + selectedAccounts);
	    String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
	    Member member = memberRepository.findByMemberId(loginId);
	    int memberIn = member.getMemberIn();

	    openBankingService.saveAccounts(selectedAccounts, accountNames, memberIn);
	    return "redirect:/mypage/mlist";
	}
	
	//계좌 중복 확인
	@GetMapping("/checkAccount")
	@ResponseBody
	public Map<String, Boolean> checkAccount(@RequestParam("accountNum") String accountNum) {
	    log.info("OpenBankingController checkAccount(), accountNum={}", accountNum);

	    boolean exists = openBankingService.isAccountExists(accountNum);
	    Map<String, Boolean> result = new HashMap<>();
	    result.put("duplicate", exists);
	    return result;
	}
	
	//db계좌 출력 로직
//	@GetMapping("/userInfo")
//	public String getUserInfo(HttpSession session, Model model) {
//	    String accessToken = (String) session.getAttribute("access_token");
//	    String userSeqNo = (String) session.getAttribute("user_seq_no");
//
//	    log.info("access_token: " + accessToken);
//	    log.info("user_seq_no: " + userSeqNo);
//
//	    Map<String, String> param = Map.of(
//	        "access_token", accessToken,
//	        "user_seq_no", userSeqNo
//	    );
//
//	    Map<String, String> userInfo = openBankingService.getUserInfo(param);
//	    model.addAttribute("map2", userInfo);
//	    model.addAttribute("access_token", accessToken); 
//	    model.addAttribute("user_seq_no", userSeqNo);
//	    
//	    String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
//		Member member = memberRepository.findByMemberId(memberId);
//		int memberIn = member.getMemberIn();
//		
//		
//		 // 계좌
//	    List<BankAccount> accountList = expenseService.getAccountList(memberIn);
//	    model.addAttribute("accountList", accountList);
//	    
//	    
//	    
//	    return "mypage/mypage_open_result";
//	}
	
	
}