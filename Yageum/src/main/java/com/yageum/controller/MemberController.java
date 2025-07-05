package com.yageum.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.yageum.config.MyUserDetailsService;
import com.yageum.domain.ChartDTO;
import com.yageum.domain.MemberDTO;
import com.yageum.service.ChartService;
import com.yageum.service.ChatGPTService;
import com.yageum.service.MemberService;
import com.yageum.service.QuestService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/member/*")
public class MemberController {

	private final MemberService memberService;

	private final QuestService questService;

	private final ChartService chartService;

	private final ChatGPTService chatGPTService;

	private final MyUserDetailsService myUserDetailsService;

	private final PasswordEncoder passwordEncoder;

	// 네이버 클라이언트 아이디
	@Value("${naver.client-id}")
	private String nClientId;

	// 네이버 API 비밀번호
	@Value("${naver.client-secret}")
	private String nClientSecret;

	// 로고 클릭시 금값 안내
	@ResponseBody
	@GetMapping("/logoClick")
	public String logoClick(@AuthenticationPrincipal UserDetails userDetails) {
		log.info("logoClick");
		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);
		ChartDTO chartDTO = new ChartDTO();
		chartDTO.setMemberIn(memberIn);

		LocalDate today = LocalDate.now();
		chartDTO.setMonth(today.getMonthValue());
		chartDTO.setYear(today.getYear());
		int sumExpense = chartService.sumExpenseByMemberLogo(chartDTO);
		System.out.println(sumExpense);


		String result = "이번달 현재까지 사용한 금액은" + String.valueOf(sumExpense) + "원 입니다. \n차트를 보러 가시겠습니까?";

		return result;

	}

	// 아이디 중복 검사
	@ResponseBody
	@GetMapping("/idCheck")
	public String idCheck(@RequestParam("id") String id) {
		log.info("MemberController idCheck()");

		MemberDTO memberDTO = memberService.infoMember(id);
		String result = "";
		if (memberDTO != null) {
			// 아이디 있음, 아이디 중복
			result = "아이디 중복";
			result = "iddup";
			return result;
		} else {
			// 아이디 없음, 해당 아이디 사용 가능
			result = "아이디 사용 가능";
			result = "idok";
		}
		// 결과값 리턴
		return result;
	}

	@ResponseBody
	@GetMapping("/emailCheck")
	public String emailCheck(@RequestParam("email") String email) {
		log.info("MemberController idCheck()");

		MemberDTO memberDTO = memberService.infoMember2(email);
		String result = "";
		if (memberDTO != null) {
			// 아이디 있음, 아이디 중복
			result = "이메일 중복";
			result = "emaildup";
			return result;
		} else {
			// 아이디 없음, 해당 아이디 사용 가능
			result = "이메일 사용 가능";
			result = "emailok";
		}
		// 결과값 리턴
		return result;
	}

	@ResponseBody
	@GetMapping("/phoneCheck")
	public String phoneCheck(@RequestParam("phone") String phone) {
		log.info("MemberController idCheck()");

		MemberDTO memberDTO = memberService.infoMember3(phone);
		String result = "";
		if (memberDTO != null) {
			// 아이디 있음, 아이디 중복
			result = "휴대폰 중복";
			result = "phonedup";
			return result;
		} else {
			// 아이디 없음, 해당 아이디 사용 가능
			result = "이메일 사용 가능";
			result = "phoneok";
		}
		// 결과값 리턴
		return result;
	}

	// 네이버 로그인 URL을 만들면서
	@GetMapping("/login")
	public String login(Model model, HttpSession session,
			@CookieValue(value = "userID", defaultValue = "") String userID) {
		log.info("MemberController login()");
		String clientId = nClientId;

		String redirectUri = URLEncoder.encode("http://localhost:8080/member/login/callback", StandardCharsets.UTF_8);
		String state = UUID.randomUUID().toString();

		session.setAttribute("state", state);

		String naverUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code" + "&client_id=" + clientId
				+ "&redirect_uri=" + redirectUri + "&state=" + state;

		model.addAttribute("naverUrl", naverUrl);
		model.addAttribute("userID", userID);

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

			memberDTO.setLastLoginDate(LocalDate.now());
			memberService.updateDate(memberDTO);
			session.setAttribute("memberName", memberDTO.getMemberName());

			return "redirect:/cashbook/main";
		}
	}

	@GetMapping("/naver_join")
	public String naverLogin() {

		return "/member/naver_join";
	}

	public String getAccessToken(String code, String state) {

		String clientId = nClientId;
		String clientSecret = nClientSecret;

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
	public String loginPro(MemberDTO memberDTO, HttpSession session, HttpServletRequest request,
			HttpServletResponse response) {
		log.info("MemberController loginPro()");
		log.info(memberDTO.toString());

		String isChecked = request.getParameter("keepLogin");

		if (isChecked != null) {

			Cookie cookie = new Cookie("userID", memberDTO.getMemberId());

			cookie.setMaxAge(30 * 60);
			response.addCookie(cookie);
		} else {
			Cookie cookie = new Cookie("userID", null);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}

		MemberDTO memberDTO2 = memberService.loginMember(memberDTO.getMemberId());

//      System.out.println("memberDTO2 : "+memberDTO2.toString());

		if (memberDTO2 == null) {
			log.info("존재하지 않는 회원 ID");
			return "redirect:/member/login";
		} else {
			memberDTO2.setLastLoginDate(LocalDate.now());
			memberService.updateDate(memberDTO2);
		}

		boolean match = passwordEncoder.matches(memberDTO.getMemberPasswd(), memberDTO2.getMemberPasswd());

		if (match) {

			// SecurityContext 설정 추가
			UserDetails userDetails = myUserDetailsService.loadUserByUsername(memberDTO.getMemberId());
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);

			HttpSession httpSession = request.getSession(true);
			httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

			// 기존 session 처리 유지
			if (memberDTO2.getMemberRole().equals("ADMIN")) {

				session.setAttribute("memberName", memberDTO2.getMemberRole());
				return "redirect:/admin/user";
			} else {
				System.out.println(memberDTO2.toString());
				session.setAttribute("memberName", memberDTO2.getMemberName());
				return "redirect:/cashbook/main";
			}

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
	public String joinPro(MemberDTO memberDTO) {
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
