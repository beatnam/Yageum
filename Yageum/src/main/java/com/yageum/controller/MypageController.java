package com.yageum.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.yageum.domain.MemberDTO;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;
import com.yageum.entity.CardCompany;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.Member;
import com.yageum.repository.MemberRepository;
import com.yageum.service.ExpenseService;
import com.yageum.service.MemberService;
import com.yageum.service.MypageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage/*")
public class MypageController {
	
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ExpenseService expenseService;
	private final MypageService mypageService;
	
	// 회원정보 수정 페이지
	@GetMapping("/update")
	public String update(HttpSession session, Model model, MemberDTO memberDTO) {
		log.info("MypageController update()");
		
		String id = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Optional<Member> member = memberService.findByMemberId(id);
		
		
		 if (member.isPresent()) {
			 
		        model.addAttribute("member", member.get());
		        
		        return "/mypage/mypage_update";
		    } else {
		        // 값이 없으면 예외 처리 또는 리다이렉트
		        return "/mypage/mypage_update";
		    }
		
		
	}
	
	// 회원정보 수정 로직
	@PostMapping("/updatePro")
	public String updatePro(MemberDTO memberDTO, Model model, HttpServletRequest request) {
		log.info("MypageController updatePro()");
		log.info("회원 정보 수정 요청: " + memberDTO);
		
		String id = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Optional<Member> optionalMember  = memberService.findByMemberId(id);
		Member member = optionalMember.get();
		
		//현재 비밀번호 맞는지 검증
		String inputPassword = request.getParameter("currentPassword");
		if (!passwordEncoder.matches(inputPassword, member.getMemberPasswd())) {
		    return "redirect:/mypage/update";
		}
		
		String newPassword = memberDTO.getNewPassword();
		if (newPassword != null && !newPassword.trim().isEmpty()) {
		    String encodedPassword = passwordEncoder.encode(newPassword);
		    member.setMemberPasswd(encodedPassword);
		}
		
		member.setMemberName(memberDTO.getMemberName());
	    member.setMemberPhone(memberDTO.getMemberPhone());
	    member.setMemberEmail(memberDTO.getMemberEmail());
	    member.setMemberGender(memberDTO.getMemberGender());
	    member.setMemberBirth(memberDTO.getMemberBirth());
	    member.setMemberAddress(memberDTO.getMemberAddress());
	    member.setEmailConsent(memberDTO.isEmailConsent());
	    
	    memberService.save(member);
		
	    
	    return "redirect:/mypage/update";
	}
	
	
	// 비밀번호 검증
	@PostMapping("/checkPassword")
	@ResponseBody
	public Map<String, Boolean> checkPassword(@RequestBody Map<String, String> requestBody) {
		log.info("MypageController checkPassword()");
	    String inputPassword = requestBody.get("password");
	    String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
	    
	    log.info("비밀번호 확인 요청 - 입력값: " + inputPassword);
	    log.info("로그인된 사용자 ID: " + loginId);


	    Optional<Member> optionalMember = memberService.findByMemberId(loginId);
	    boolean match = false;

	    if (optionalMember.isPresent()) {
	        Member member = optionalMember.get();
	        log.info("DB 저장된 비번 (암호화): " + member.getMemberPasswd());
	        match = passwordEncoder.matches(inputPassword, member.getMemberPasswd());
	        log.info("매치 결과: " + match);
	    } else {
	        log.info("해당 ID의 회원 정보 없음");
	    }

	    return Collections.singletonMap("match", match);
	}
	
	
	// 회원 탈퇴 전 비밀번호 확인
	@GetMapping("/deletepw")
	public String deletepw() {
		log.info("MypageController deletepw()");
		
		return "/mypage/mypage_delete_pw";
	}
	
	// 회원 탈퇴 페이지
	@GetMapping("/delete")
	public String delete() {
		log.info("MypageController delete()");
		
		return "/mypage/mypage_delete";
	}
	
	// 회원 탈퇴 로직
	@PostMapping("/deletePro")
	public String deletePro(HttpSession session, Principal principal) {
		log.info("MypageController deletePro()");
		String id = principal.getName();
		memberService.deleteByMemberId(id);
		
		session.invalidate();
				
		
		return "redirect:/member/login";
	}
	
	// 수단 리스트 페이지
	@GetMapping("/mlist")
	public String methodlist(Model model) {
		log.info("MypageController methodlist()");
		
		String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByMemberId(memberId);
		int memberIn = member.getMemberIn();
		
		// 카테고리
	    List<CategoryMain> mainList = expenseService.getMainCategoryList();
	    model.addAttribute("mainList", mainList);
	    
	    // 카드
	    List<Card> cardList = expenseService.getCardList(memberIn, 1);
	    cardList.addAll(expenseService.getCardList(memberIn, 2)); 
	    model.addAttribute("cardList", cardList);
	    
	    // 계좌
	    List<BankAccount> accountList = expenseService.getAccountList(memberIn);
	    model.addAttribute("accountList", accountList);
		    
		return "/mypage/mypage_method_list";
	}
	
	// 수단 삭제 로직
	@PostMapping("/mdelete")
	public String mdelete(@RequestParam("ids") List<String> ids, @RequestParam("types") List<String> types) {
		log.info("MypageController mdelete()");
	    log.info("삭제 요청: ids={}, types={}", ids, types);

	    for (int i = 0; i < ids.size(); i++) {
	        int id = Integer.parseInt(ids.get(i));
	        String type = types.get(i);

	        if (type.equals("account")) {
	        	mypageService.deleteAccountById(id);
	        } else if (type.equals("credit") || type.equals("check")) {
	        	mypageService.deleteCardById(id);
	        }
	    }
	    return "redirect:/mypage/mlist"; // 다시 목록으로
	}
	
	
	
	// 수단 추가
	@GetMapping("/minsert")
	public String methodinsert(Model model) {
		log.info("MypageController methodinsert()");
		
		// 카드회사 목록 조회
		List<CardCompany> companyList = mypageService.getCardCorporationList();
		model.addAttribute("companyList", companyList);
		
		return "/mypage/mypage_method_insert";
	}
	
	// 수단 추가 로직
	@PostMapping("/minsertPro")
	public String minsertPro(@RequestParam("card1") String card1,
	        @RequestParam("card2") String card2,
	        @RequestParam("card3") String card3,
	        @RequestParam("card4") String card4,
	        @RequestParam("expiryMM") String expiryMM,
	        @RequestParam("expiryYY") String expiryYY,
	        @RequestParam("cardHolder") String cardHolder,
	        @RequestParam("cvc") String cvc,
	        @RequestParam("cardType") int cardType,
	        @RequestParam("cardCompany") int cardCompany,
	        @RequestParam("cardName") String cardName) {
		log.info("MypageController minsertPro()");

		String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByMemberId(memberId);
		int memberIn = member.getMemberIn();

		String cardNumber = card1 + card2 + card3 + card4;

	    Card card = new Card();
	    card.setCardNum(cardNumber);
	    card.setCardMonth(expiryMM);
	    card.setCardYear(expiryYY);
	    card.setCardCvc(cvc);
	    card.setCardUsername(cardHolder);
	    card.setMethodIn(cardType);
	    card.setCcIn(cardCompany);
	    card.setCardName(cardName);
	    card.setMemberIn(memberIn);


		mypageService.minsertPro(card);

		return "redirect:/mypage/mlist";
	}
	
	//카드사 자동 매칭 api
	@GetMapping("/binlookup/{bin}")
	@ResponseBody
	public ResponseEntity<?> lookupBin(@PathVariable("bin") String bin) {
	    String url = "https://lookup.binlist.net/" + bin;
	    RestTemplate restTemplate = new RestTemplate();

	    try {
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Accept-Version", "3"); // binlist.net 권장 헤더
	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        ResponseEntity<String> response = restTemplate.exchange(
	            url, HttpMethod.GET, entity, String.class
	        );

	        return ResponseEntity.ok().body(response.getBody());
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("BIN 조회 실패: " + e.getMessage());
	    }
	}


}
