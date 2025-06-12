package com.yageum.controller;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

	private final PasswordEncoder passwordEncoder;

	@GetMapping("/login")
	public String login() {
		log.info("MemberController login()");
		return "/member/login";
	}

	@PostMapping("/loginPro")
	public String loginPro(MemberDTO memberDTO, HttpSession session) {
		log.info("MemberController loginPro()");
		log.info(memberDTO.toString());

		MemberDTO memberDTO2 = memberService.loginMember(memberDTO.getMemberId());

		System.out.println("memberDTO2 = " + memberDTO2);

		if (memberDTO2 == null) {
			log.info("존재하지 않는 회원 ID");
			return "redirect:/member/login";
		}
		// 비밀번호 비교
		System.out.println(memberDTO2);
		boolean match = passwordEncoder.matches(memberDTO.getMemberPasswd(), memberDTO2.getMemberPasswd());
		System.out.println(match);
		if (match == true) {

			return "redirect:/cashbook/main";

		} else {

			return "redirect:/member/login";
		}

	}

	@GetMapping("/terms")
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

		memberService.joinMember(memberDTO);

		return "redirect:/member/login";
	}

}
