package com.yageum.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.service.OpenBankingService;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Member;
import com.yageum.mapper.MypageMapper;
import com.yageum.repository.MemberRepository;
import com.yageum.service.ExpenseService;
import com.yageum.service.MemberService;
import com.yageum.service.MypageService;

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
	
//    private final String CLIENT_ID = "bc683374-e833-4c42-b28f-9b29115656b0";
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
	
	
	

	
//	@GetMapping("/callback")
//	public String callback(@RequestParam Map<String, String> map, Model model) {
//		// 인증 후 응답 메시지
//		System.out.println(map);
////			code=VAcpxt4sJikXPx2KknUx6b8PNhTb6X&
////			scope=inquiry login transfer&
////			state=12345678123456781234567812345678
//		
//		// 토큰발급 => 처리 OpenBankingService
//		Map<String, String> map2 = openBankingService.requestToken(map);
//		
//		// model 결과 담아서
//		model.addAttribute("map2", map2);
//		
//		// 이동
//		return "redirect:/openbanking/userInfo";
//	}
	
	
//	// 사용자 정보 조회
//	@GetMapping("/userInfo")
//	public String getUserInfo(@RequestParam Map<String, String> map, Model model) {
//		Map<String, String> map2 = openBankingService.getUserInfo(map);
//		model.addAttribute("map2", map2);
//		model.addAttribute("access_token", map.get("access_token"));
//		
//		return "mypage/mypage_openbanking";
//	}
//    
	

    

//	@GetMapping("/auth")
//	public String realOrMockAuth(HttpSession session,
//	                             @RequestParam(value = "mock", defaultValue = "true") boolean useMock) throws UnsupportedEncodingException {
//	    
//		//실제 연결 아닌 mock
//		if (useMock) {
//	        session.setAttribute("access_token", "mock_access_token_1234");
//	        return "redirect:/mypage/openbanking";
//	    }
//
//			//실제 연결 로직
//    	    String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, "UTF-8");
//    	    String encodedScope = URLEncoder.encode(SCOPE, "UTF-8");
//
//    	    String url = AUTH_URL + "?" +
//    	            "response_type=code" +
//    	            "&client_id=" + CLIENT_ID +
//    	            "&redirect_uri=" + encodedRedirectUri +
//    	            "&scope=" + encodedScope +
//    	            "&state=" + STATE +
//    	            "&auth_type=0";
//
//    	    return "redirect:" + url;
//    	}
//    	
//    	
//    @GetMapping("/callback")
//    public String callback(@RequestParam("code") String code, HttpSession session) {
//        // 실제 토큰 발급은 생략하고 mock 저장
//        String mockAccessToken = "mock_access_token_1234";
//        session.setAttribute("access_token", mockAccessToken);
//        return "redirect:/mypage/openbanking";
//    }
//    
//    @GetMapping("/mock-auth")
//    public String showMockAuthPage(HttpSession session, Model model) {
//    	 String memberName = (String) session.getAttribute("memberName");
//    	    model.addAttribute("memberName", memberName);
//    	
//        return "/mypage/mypage_openbanking_mock";  
//    }
//
//    @PostMapping("/mock-auth/submit")
//    public ResponseEntity<Void> submitMockAuth(HttpSession session) {
//        session.setAttribute("access_token", "mock_access_token_1234");
//        return ResponseEntity.ok().build(); 
//    }
//    
//    @GetMapping("/fetchAccounts")
//    @ResponseBody
//    public Map<String, Object> fetchAccounts(@RequestParam(name = "access_token", required = false) String token, HttpSession session) {
//        Map<String, Object> result = new HashMap<>();
//
//        if (token == null || !token.equals("mock_access_token_1234")) {
//            result.put("error", "인증되지 않은 사용자입니다.");
//            return result;
//        }
//
//        String userName = (String) session.getAttribute("memberName");
//        if (userName == null) userName = "이름없음";
//
//        result.put("user_name", userName);
//
//        List<Map<String, String>> accountList = new ArrayList<>();
//
//        accountList.add(Map.of(
//            "bank_name", "카카오뱅크",
//            "account_num_masked", "3333031234252"
//        ));
//
//        accountList.add(Map.of(
//            "bank_name", "국민은행",
//            "account_num_masked", "58210242524381"
//        ));
//
//        result.put("res_list", accountList);
//        return result;
//    }
//    
//    @GetMapping("/tokenCheck")
//    @ResponseBody
//    public String tokenCheck(HttpSession session) {
//        Object token = session.getAttribute("access_token");
//        return token != null ? "토큰 있음: " + token : "세션 없음!";
//    }
//    
//    @PostMapping("/saveAccounts")
//    @ResponseBody
//    public String saveAccountsToBankTable(@RequestBody List<Map<String, String>> accounts,
//                                          HttpSession session) {
//    	String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
// 	    Member member = memberRepository.findByMemberId(memberId);
// 	    int memberIn = member.getMemberIn();
//
//
//        for (Map<String, String> acc : accounts) {
//            BankAccount account = new BankAccount();
//            account.setMemberIn(memberIn);
//            account.setAccountNum(acc.get("account_num_masked").replace("*", "0")); // 예시
//            account.setBankIn(getBankCodeFromName(acc.get("bank_name"))); 
//            account.setFinnum("123456");
//            account.setAccountName(acc.get("bank_name") + "입출금통장");
//            account.setAccountAlias(null);
//
//            mypageMapper.insertBankAccount(account);
//        }
//
//        return "계좌 저장 완료!";
//    }
//    
//    private int getBankCodeFromName(String bankName) {
//        return switch (bankName) {
//            case "카카오뱅크" -> 2;
//            case "국민은행" -> 19;
//            case "신한은행" -> 3;
//            default -> 99;  // 기타
//        };
//    }
    
    
}
