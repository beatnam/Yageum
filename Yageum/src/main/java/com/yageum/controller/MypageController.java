package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/mypage/*")
public class MypageController {
	
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
