package com.yageum.controller;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yageum.domain.MemberDTO;
import com.yageum.entity.Member;
import com.yageum.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/mypage/*")
public class MypageController {
	
	private final MemberService memberService;
	
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
