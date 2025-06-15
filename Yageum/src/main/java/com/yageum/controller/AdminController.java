package com.yageum.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.entity.Member;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/admin/*")
public class AdminController {
	
	private final MemberService memberService;

	// 회원 관리 페이지
	@GetMapping("/user")
	public String user(Model model) {
		log.info("AdminController user()");

		List<Member> member = memberService.adminInfo();
		
		model.addAttribute("member", member);
		
		log.info(member.toString());
		

		return "/admin/admin_user";
	}

	@GetMapping("/state")
	public String state() {
		log.info("AdminController state()");

		return "/admin/admin_state";
	}

	// 통계 / 리포트 페이지

	// 사이트 설정 - 카테고리 설정 페이지
	@GetMapping("/category")
	public String category() {
		log.info("AdminController category()");

		return "/admin/admin_category";
	}

	// 사이트 설정 - 카테고리 설정 페이지
	
	

	// 사이트 설정 - 퀘스트 설정 페이지

	@GetMapping("/quest")
	public String quest() {
		log.info("AdminController quest()");

		return "/admin/admin_quest";
	}
	
	@GetMapping("/quest_gener")
	public String gener() {
		log.info("AdminController gener()");
		
		
		return "/admin/quest_gener";
	}
	
	@GetMapping("/quest_update")
	public String update() {
		log.info("AdminController update()");
		
		
		return "/admin/quest_update";
	}
	// 사이트 설정 - 퀘스트 설정 페이지

	
	
	// 사이트 설정 - 공지사항 페이지

	@GetMapping("/noticfication")
	public String noticfication() {
		log.info("AdminController noticfication()");

		return "/admin/admin_noticfication";
	}

	// 사이트 설정 - 공지사항 페이지

	// 사이트 설정 - 상품 설정 페이지

	@GetMapping("/item")
	public String item() {
		log.info("AdminController item()");

		return "/admin/admin_item";
	}

	// 사이트 설정 - 상품 설정 페이지

}
