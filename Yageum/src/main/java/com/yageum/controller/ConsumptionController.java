package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/consumption/*")
public class ConsumptionController {
	
	@GetMapping("/eanalysis")
	public String eanalysis() {
		log.info("ConsumptionController eanalysis()");
		return "/consumption/expense_analysis";
	}
	
	@GetMapping("/efeedback")
	public String efeedback() {
		log.info("ConsumptionController efeed()");
		return "/consumption/expense_feedback";
	}
	
	@GetMapping("/canalysis")
	public String canalysis() {
		log.info("ConsumptionController canalysis()");
		return "/consumption/consumption_analysis";
	}
	
	@GetMapping("/bplanner")
	public String bplanner() {
		log.info("ConsumptionController bplanner()");
		return "/consumption/budget_planner";
	}
	
}
