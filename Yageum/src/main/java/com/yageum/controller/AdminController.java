package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/admin/*")
public class AdminController {

	// 회원 관리 페이지
	@GetMapping("/user")
	public String user() {
		log.info("AdminController user()");

		return "/admin/admin_user";
	}

	// 회원 관리 페이지

	// 통계 / 리포트 페이지
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
