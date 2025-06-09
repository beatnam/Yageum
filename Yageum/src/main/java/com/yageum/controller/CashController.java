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
public class CashController {
	
	@GetMapping("/main")
	public String main() {
		log.info("CashController main()");
		
		return "/cashbook/cashbook_main";
	}
	
	@GetMapping("/list")
	public String list() {
		log.info("CashController list()");
		
		return "/cashbook/cashbook_list";
	}
	
	@GetMapping("/detail")
	public String detail() {
		log.info("CashController detail()");
		
		return "/cashbook/cashbook_detail";
	}
	
	@GetMapping("/update")
	public String update() {
		log.info("CashController update()");
		
		return "/cashbook/cashbook_update";
	}
	
	@GetMapping("/insert")
	public String insert() {
		log.info("CashController insert()");
		
		return "/cashbook/cashbook_insert";
	}
	
	

}
