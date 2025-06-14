package com.yageum.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.MemberDTO;
import com.yageum.entity.Member;
import com.yageum.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/mypage/*")
public class MypageController {
	
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	
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
	
	
	
	@PostMapping("/checkPassword")
	@ResponseBody
	public Map<String, Boolean> checkPassword(@RequestBody Map<String, String> requestBody) {
	    String inputPassword = requestBody.get("password");
	    String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
	    
	    log.info("🔐 비밀번호 확인 요청 - 입력값: " + inputPassword);
	    log.info("🧑 로그인된 사용자 ID: " + loginId);


	    Optional<Member> optionalMember = memberService.findByMemberId(loginId);
	    boolean match = false;

	    if (optionalMember.isPresent()) {
	        Member member = optionalMember.get();
	        log.info("🗝️ DB 저장된 비번 (암호화): " + member.getMemberPasswd());
	        match = passwordEncoder.matches(inputPassword, member.getMemberPasswd());
	        log.info("🔍 매치 결과: " + match);
	    } else {
	        log.warning("❌ 해당 ID의 회원 정보 없음");
	    }

	    return Collections.singletonMap("match", match);
	}
	
	
	
	@GetMapping("/deletepw")
	public String deletepw() {
		log.info("MypageController deletepw()");
		
		return "/mypage/mypage_delete_pw";
	}
	
	@GetMapping("/delete")
	public String delete() {
		log.info("MypageController delete()");
		
		return "/mypage/mypage_delete";
	}
	
	@PostMapping("/deletePro")
	public String deletePro(HttpSession session, Principal principal) {
		String id = principal.getName();
		memberService.deleteByMemberId(id);
		
		session.invalidate();
				
		
		return "redirect:/member/login";
	}
	
	@GetMapping("/mlist")
	public String methodlist() {
		log.info("MypageController methodlist()");
		
		return "/mypage/mypage_method_list";
	}
	
	@GetMapping("/minsert")
	public String methodinsert() {
		log.info("MypageController methodinsert()");
		
		return "/mypage/mypage_method_insert";
	}
	

	

}
