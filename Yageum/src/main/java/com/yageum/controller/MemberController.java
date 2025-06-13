package com.yageum.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.yageum.config.MyUserDetailsService;
import com.yageum.domain.MemberDTO;
import com.yageum.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/member/*")
public class MemberController {

	private final MemberService memberService;

	private final MyUserDetailsService myUserDetailsService;

	private final PasswordEncoder passwordEncoder;

	// 네이버 로그인 URL을 만들면서
	@GetMapping("/login")
	public String login(Model model, HttpSession session) {
		log.info("MemberController login()");
		String clientId = "w8JhqTuW8SjkPuyt0fP1";
		String redirectUri = URLEncoder.encode("http://localhost:8080/member/login/callback", StandardCharsets.UTF_8);
		String state = UUID.randomUUID().toString();

		session.setAttribute("state", state);

		String naverUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code" + "&client_id=" + clientId
				+ "&redirect_uri=" + redirectUri + "&state=" + state;

		model.addAttribute("naverUrl", naverUrl);

		return "/member/login";
	}

	@GetMapping("/login/callback")
	public String naverCallback(@RequestParam("code") String code, @RequestParam("state") String state,
			HttpSession session, Model model, HttpServletRequest request) {

		String accessToken = getAccessToken(code, state);

		// 사용자 정보 요청
		Map<String, Object> userInfo = getUserInfo(accessToken);

		System.out.println(userInfo);

		String id = userInfo.get("id").toString();
		String name = userInfo.get("name").toString();
		String email = userInfo.get("email").toString();
		String gender = userInfo.get("gender").toString();
		String phone = userInfo.get("mobile").toString();
		String birth = userInfo.get("birthday").toString();

		MemberDTO memberDTO = memberService.infoMember(id);

		if (memberDTO == null) {
			model.addAttribute("id", id);
			model.addAttribute("name", name);
			model.addAttribute("email", email);
			model.addAttribute("gender", gender);
			model.addAttribute("phone", phone);
			model.addAttribute("birth", birth);

			return "/member/naver_join";

		} else {

			UserDetails userDetails = myUserDetailsService.loadUserByUsername(id);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());

			// SecurityContextHolder 설정
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);

			// ★ 세션에 SecurityContext 저장
			HttpSession httpSession = request.getSession(true);
			httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

			session.setAttribute("memberName", memberDTO.getMemberName());

			return "redirect:/cashbook/main";
		}
	}

	@GetMapping("/naver_join")
	public String naverLogin() {

		return "/member/naver_join";
	}

	public String getAccessToken(String code, String state) {
		String clientId = "w8JhqTuW8SjkPuyt0fP1";
		String clientSecret = "0QBmHnPoNY";
		String redirectUri = URLEncoder.encode("http://localhost:8080/member/login/callback", StandardCharsets.UTF_8);

		String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code" + "&client_id=" + clientId
				+ "&client_secret=" + clientSecret + "&redirect_uri=" + redirectUri + "&code=" + code + "&state="
				+ state;

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map> response = restTemplate.getForEntity(apiURL, Map.class);

		Map body = response.getBody();
		return (String) body.get("access_token");
	}

	public Map<String, Object> getUserInfo(String accessToken) {
		String apiURL = "https://openapi.naver.com/v1/nid/me";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		HttpEntity<String> entity = new HttpEntity<>("", headers);

		ResponseEntity<Map> response = restTemplate.exchange(apiURL, HttpMethod.GET, entity, Map.class);
		Map<String, Object> responseBody = response.getBody();

		return (Map<String, Object>) responseBody.get("response");
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

			String memberName = memberDTO2.getMemberName();
			session.setAttribute("memberName", memberName);
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

	@PostMapping("/naverJoinPro")
	public String naverJoinPro(MemberDTO memberDTO, HttpSession Session) {
		log.info("MemberController joinPro()");

		System.out.println(memberDTO);
		memberService.naverJoinMember(memberDTO);

		return "redirect:/member/login";
	}
}
