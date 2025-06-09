package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/member/*")
public class MemberController {

	@GetMapping("/login")
	public String login() {
		log.info("MemberController login()");
		return "/member/login";
	}
	
	@PostMapping("/loginPro")
	public String loginPro() {
		
		return "redirect:/cashback/main";
	}

	@GetMapping("/terms_join")
	public String termsJoin() {
		log.info("MemberController termsJoin()");
		return "/member/terms_join";
	}

	@PostMapping("/termsAgree")
	public String termsAgree() {
		log.info("MemberController termsAgree()");

		return "redirect:/member/join";
	}

	@GetMapping("/join")
	public String join() {
		log.info("MemberController join");
		return "/member/join";
	}

	@PostMapping("/joinPro")
	public String joinPro() {
		log.info("MemberController joinPro()");
		return "/login";
	}
	
	
}
