package com.yageum.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.domain.QuestStateDTO;
import com.yageum.service.AdminService;
import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/quest/*")
public class QuestController {

	private final AdminService adminService;

	private final QuestService questService;

	@GetMapping("/list")
	public String listQuest(Model model, @AuthenticationPrincipal UserDetails userDetails) {

		// 접속중인 userDetails에서 Id 찾아서 memberIn 검색 해오기
		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);

		List<Map<Object, Object>> questList = questService.listQuest(memberIn);

		System.out.println(questList);

		model.addAttribute("questList", questList);
		return "/quest/list";
	}

	@GetMapping("/item")
	public String listItem() {
		return "/quest/item";
	}

	@PostMapping("/acceptQuest")
	public String acceptQuest(@RequestParam("questIn") int questIn, @AuthenticationPrincipal UserDetails userDetails) {

		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);
		System.out.println(memberIn);

		QuestStateDTO questStateDTO = new QuestStateDTO();
		questStateDTO.setQuestIn(questIn);
		questStateDTO.setMemberIn(memberIn);

		questService.acceptQuest(questStateDTO);

		return "redirect:/quest/list";
	}
}
