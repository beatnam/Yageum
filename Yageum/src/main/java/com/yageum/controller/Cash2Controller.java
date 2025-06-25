package com.yageum.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/cashbook/*")
public class Cash2Controller {

	private final QuestService questService;

	@GetMapping("/chart")
	public String chart() {
		log.info("Cash2Controller chart()");

		return "/cashbook/cashbook_chart";
	}

	@GetMapping("/quest")
	public String quest(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		log.info("Cash2Controller quest()");
		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);

		List<Map<Object, Object>> myQuest = questService.myQuest(memberIn);

		model.addAttribute("myQuest", myQuest);

		return "/cashbook/cashbook_quest";
	}

	

}
