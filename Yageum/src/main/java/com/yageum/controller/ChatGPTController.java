package com.yageum.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.service.ChatGPTService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
@Controller
public class ChatGPTController {
	
	private final ChatGPTService chatGPTService;
	
	@GetMapping("/ask")
	public String ask() {
		
		return "member/chatgpt";
	}
	
	@PostMapping("/ask2")
	public String ask2(@RequestParam("prompt") String prompt, Model model) {
		System.out.println("ChatGPTController ask2()");
		System.out.println("질문 : " + prompt);
		
		String result = chatGPTService.askChatGpt(prompt);
		
		System.out.println("결과 : " + result);
		
		model.addAttribute("prompt", prompt);
		model.addAttribute("result", result);
		
		return "member/chatgpt";
	}
	
	
	
}
