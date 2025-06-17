package com.yageum.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yageum.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/quest/*")
public class QuestController {

	private final AdminService adminService;
	
	@GetMapping("/list")
	public String listQuest(Model model) {

		List<Map<Object, Object>> questList = adminService.listQuest();

		System.out.println(questList);
		
		model.addAttribute("questList", questList);
		return "/quest/list";
	}
}
