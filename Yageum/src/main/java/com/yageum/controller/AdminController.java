package com.yageum.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.entity.Member;
import com.yageum.service.AdminService;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/admin/*")
public class AdminController {

	private final MemberService memberService;

	private final AdminService adminService;

	// 회원 관리 페이지
	@GetMapping("/user")
	public String user(Model model) {
		log.info("AdminController user()");

		List<Member> member = memberService.adminInfo();

		model.addAttribute("member", member);

		log.info(member.toString());

		return "/admin/admin_user";
	}

	@GetMapping("/user_detail")
	public String user_update(@RequestParam("memberId") String memberId, Model model) {
		log.info("AdminController user_detail()");
		Optional<Member> member = memberService.findByMemberId(memberId);

		model.addAttribute("member", member);
		// html 페이지로 가서 상세보기 페이지에서 표시해줄 정보 입력해주기

		return "/admin/user_detail";
	}

	// 회원 관리 페이지

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
	public String questgener(Model model) {
		log.info("AdminController questgener()");

		List<CategoryMainDTO> categoryMainList = adminService.showCategoryMain();

		model.addAttribute("categoryMainList", categoryMainList);
		return "/admin/quest_gener";
	}

	@PostMapping("/insert_Q")
	@ResponseBody
	public String insertQuest(@RequestParam Map<Object, Object> quest) {
		log.info("AdminController insertQuest()");
		// 입력값 확인
		System.out.println(quest);

		adminService.insertQuest(quest);
		
		return "redirect:/admin/admin_quest";
	}

	@GetMapping("/categorySelect")
	@ResponseBody
	public List<CategorySubDTO> subCategorySelect(@RequestParam("cmIn") int cmIn) {
		log.info("소분류 카테고리를 불러오기 위한 ajax");

		List<CategorySubDTO> result = adminService.subCategorySelect(cmIn);
		System.out.println(result);
		return result;
	}

	@GetMapping("/quest_update")
	public String questupdate() {
		log.info("AdminController questupdate()");

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
