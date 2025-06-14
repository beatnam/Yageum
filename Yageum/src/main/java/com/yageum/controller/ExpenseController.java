package com.yageum.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.entity.Expense;
import com.yageum.entity.Member;
import com.yageum.service.ExpenseService;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/cashbook/*")
public class ExpenseController {
	
	private final ExpenseService expenseService;
	private final MemberService memberService;
	
	@GetMapping("/main")
	public String main() {
		log.info("ExpenseController main()");
		

		return "/cashbook/cashbook_main";
	}

	@GetMapping("/list")
	public String list() {
		log.info("ExpenseController list()");

		return "/cashbook/cashbook_list";
	}

	@GetMapping("/detail")
	public String detail() {
		log.info("ExpenseController detail()");

		return "/cashbook/cashbook_detail";
	}

	@GetMapping("/update")
	public String update() {
		log.info("ExpenseController update()");

		return "/cashbook/cashbook_update";
	}

	@GetMapping("/insert")
	public String insert(Model model) {
		log.info("ExpenseController insert()");
		 List<CategoryMainDTO> mainList = expenseService.getAllMainCategories();
		    model.addAttribute("mainList", mainList);

		return "/cashbook/cashbook_insert";
	}
	
	 @GetMapping("/category/{cmIn}")
	 @ResponseBody
    public List<CategorySubDTO> getSubCategories(@PathVariable("cmIn") int cmIn) {
        return expenseService.getSubCategoriesByCmIn(cmIn);  
    }
	
	@PostMapping("/insertPro")
	public String insertPro(Expense expense, @RequestParam("method1") String method1, @RequestParam("method2") String method2, Principal principal) {
	    log.info("ExpenseController insertPro()");
	    log.info("입력된 값: " + expense.toString());

	    String loginId = principal.getName(); 
	    Member member = memberService.findByMemberId(loginId).orElseThrow();
	    expense.setMemberIn(member.getMemberIn());

	    if ("card".equals(method1)) {
	        expense.setCardIn(Integer.parseInt(method2));
	    } else if ("account".equals(method1)) {
	        expense.setAccountIn(Integer.parseInt(method2));
	    }

	    expenseService.saveExpense(expense);
	    return "redirect:/cashbook/main"; 
	}

	@GetMapping("/search")
	public String search() {
		log.info("ExpenseController search()");

		return "/cashbook/cashbook_search";
	}

}
