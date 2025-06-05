package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
public class MainController {

	@GetMapping("/main")
	public String goMain() {
		log.info("MainController goMain()");
		return "/main";
	}

}
