package com.yageum.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.ChartDTO;
import com.yageum.service.ChartService;
import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/cashbook/*")
public class Cash2Controller {

	private final QuestService questService;

	private final ChartService chartService;

	@GetMapping("/chart")
	public String chart(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		log.info("Cash2Controller chart()");
		String memberId = userDetails.getUsername();
		int memberIn = questService.searchMemberIn(memberId);

		ChartDTO chartDTO = new ChartDTO();
		chartDTO.setMemberIn(memberIn);

		LocalDate today = LocalDate.now();
		chartDTO.setMonth(today.getMonthValue());
		chartDTO.setYear(today.getYear());
		int month = chartDTO.getMonth();
		int year = chartDTO.getYear();
		List<Map<String, Object>> sumExpense = chartService.sumExpenseByMember(chartDTO);
		List<String> colors = Arrays.asList(
			"#FF6B6B", "#4ECDC4", "#45B7D1", "#F9CA24", "#6C5CE7", "#A29BFE", "#FD79A8", "#00B894", "#E17055");

		Map<String, Object> plus = chartService.plusMember(chartDTO);
		Map<String, Object> minus = chartService.minusMember(chartDTO);

		if (plus == null) {
			plus = new HashMap<>();
			plus.put("plus", 0);
		}
		if (minus == null) {
			minus = new HashMap<>();
			minus.put("minus", 0);
		}

		Number plusVal = (Number) plus.get("plus");
		Number minusVal = (Number) minus.get("minus");

		int plusInt = plusVal != null ? plusVal.intValue() : 0;
		int minusInt = minusVal != null ? minusVal.intValue() : 0;

		int sum = plusInt - minusInt;

		model.addAttribute("minus", minus);
		model.addAttribute("plus", plus);
		model.addAttribute("sumExpense", sumExpense);
		model.addAttribute("month", month);
		model.addAttribute("year", year);
		model.addAttribute("colors", colors);
		model.addAttribute("sum", sum); // 필요 시

		return "/cashbook/cashbook_chart";
	}
	
//	@GetMapping("/chartMonth")
//	@ResponseBody
//	public String chartMonth(Model model, @AuthenticationPrincipal UserDetails userDetails, @RequestParam("")) {
//		log.info("Cash2Controller chart()");
//		String memberId = userDetails.getUsername();
//		int memberIn = questService.searchMemberIn(memberId);
//
//		ChartDTO chartDTO = new ChartDTO();
//		chartDTO.setMemberIn(memberIn);
//
//		LocalDate today = LocalDate.now();
//		chartDTO.setMonth(today.getMonthValue());
//		chartDTO.setYear(today.getYear());
//		int month = chartDTO.getMonth();
//		int year = chartDTO.getYear();
//		List<Map<String, Object>> sumExpense = chartService.sumExpenseByMember(chartDTO);
//		List<String> colors = Arrays.asList(
//			"#FF6B6B", "#4ECDC4", "#45B7D1", "#F9CA24", "#6C5CE7", "#A29BFE", "#FD79A8", "#00B894", "#E17055");
//
//		Map<String, Object> plus = chartService.plusMember(chartDTO);
//		Map<String, Object> minus = chartService.minusMember(chartDTO);
//
//		if (plus == null) {
//			plus = new HashMap<>();
//			plus.put("plus", 0);
//		}
//		if (minus == null) {
//			minus = new HashMap<>();
//			minus.put("minus", 0);
//		}
//
//		Number plusVal = (Number) plus.get("plus");
//		Number minusVal = (Number) minus.get("minus");
//
//		int plusInt = plusVal != null ? plusVal.intValue() : 0;
//		int minusInt = minusVal != null ? minusVal.intValue() : 0;
//
//		int sum = plusInt - minusInt;
//
//		model.addAttribute("minus", minus);
//		model.addAttribute("plus", plus);
//		model.addAttribute("sumExpense", sumExpense);
//		model.addAttribute("month", month);
//		model.addAttribute("year", year);
//		model.addAttribute("colors", colors);
//		model.addAttribute("sum", sum); // 필요 시
//
//		return "/cashbook/cashbook_chart";
//	}

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
