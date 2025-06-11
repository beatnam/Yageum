package com.yageum.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.domain.MemberDTO;
import com.yageum.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/member/*")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/login")
	public String login() {
		log.info("MemberController login()");
		return "/member/login";
	}

	@PostMapping("/loginPro")
	public String loginPro() {

		return "redirect:/cashbook/main";
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
	public String joinPro(MemberDTO memberDTO, HttpSession Session) {
		log.info("MemberController joinPro()");
		memberDTO.setMemberConsent(true);
		memberDTO.setCreateDate(LocalDate.now());
		memberDTO.setMemberState("정상");
		memberDTO.setMemberIsFirst(true);

		memberService.joinMember(memberDTO);

		return "redirect:/member/login";
	}

}
