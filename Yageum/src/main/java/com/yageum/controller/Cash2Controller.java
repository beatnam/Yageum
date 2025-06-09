package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/cashbook/*")
public class Cash2Controller {

	@GetMapping("/chart")
	public String chart() {
		log.info("Cash2Controller chart()");
		
		
		return "/cashbook/cashbook_chart";
	}
	@GetMapping("/quest")
	public String quest() {
		log.info("Cash2Controller quest()");
		
		
		return "/cashbook/cashbook_quest";
	}
	
	
	
	
}
