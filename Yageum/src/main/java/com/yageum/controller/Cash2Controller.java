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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.ChartDTO;
import com.yageum.domain.ResponseDTO;
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
		model.addAttribute("sum", sum);

		return "/cashbook/cashbook_chart";
	}
	
	@GetMapping("/api/chartMonth")
	@ResponseBody
	public ResponseDTO<Map<String, Object>> getChartMonthData(
	        @AuthenticationPrincipal UserDetails userDetails,
	        @RequestParam("year") int year,
	        @RequestParam("month") int month) {

		log.info("Cash2Controller getChartMonthData() - year: " + year + ", month: " + month);
		
		if (userDetails == null) {
            return ResponseDTO.fail("로그인이 필요합니다.");
        }

		String memberId = userDetails.getUsername();
		int memberIn = questService.searchMemberIn(memberId);

		ChartDTO chartDTO = new ChartDTO();
		chartDTO.setMemberIn(memberIn);
		chartDTO.setYear(year);
		chartDTO.setMonth(month);

		List<Map<String, Object>> sumExpense = chartService.sumExpenseByMember(chartDTO);
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

		Map<String, Object> chartData = new HashMap<>();
		chartData.put("sumExpense", sumExpense);
		chartData.put("plus", plus);
		chartData.put("minus", minus);
		chartData.put("sum", sum);

		return ResponseDTO.success("차트 데이터를 성공적으로 가져왔습니다.", chartData);
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