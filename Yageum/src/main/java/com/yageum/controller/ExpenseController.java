package com.yageum.controller;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.BankAccountDTO;
import com.yageum.domain.CardDTO;
import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.ExpenseDTO;
import com.yageum.entity.Expense;
import com.yageum.entity.Member;
import com.yageum.repository.MemberRepository;
import com.yageum.service.ExpenseService;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cashbook/*")
public class ExpenseController {
	
	private final ExpenseService expenseService;
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	
	@GetMapping("/main")
	public String main() {
		log.info("ExpenseController main()");
		

		return "/cashbook/cashbook_main";
	}

	@GetMapping("/list")
	public String list(@RequestParam("date") String date, Model model) {
	    log.info("ExpenseController list() - date: {}", date);

	    // 로그인된 사용자 ID 가져오기 (Spring Security)
	    String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
	    Member member = memberRepository.findByMemberId(memberId);

	    int memberIn = member.getMemberIn();

	    // 서비스 호출
	    List<Expense> expenses = expenseService.findByDate(memberIn, date);
	    int incomeTotal = expenseService.getDailyTotal(memberIn, date, 0);
	    int expenseTotal = expenseService.getDailyTotal(memberIn, date, 1);
	    int balance = incomeTotal - expenseTotal;

	    // 모델에 담기
	    model.addAttribute("expenses", expenses);
	    model.addAttribute("incomeTotal", incomeTotal);
	    model.addAttribute("expenseTotal", expenseTotal);
	    model.addAttribute("balance", balance);
	    
	    log.info("조회된 수입합계: " + incomeTotal);
	    log.info("조회된 지출합계: " + expenseTotal);
	    log.info("전체 리스트: {}", expenses);

		return "/cashbook/cashbook_list";
	}

	@GetMapping("/detail")
	public String detail(@RequestParam("id") int id, @RequestParam("date") String date, Model model) {
		log.info("ExpenseController detail()");

		ExpenseDTO expense = expenseService.getExpenseDetailById(id);
		
		log.info("조회 요청 id = {}", id);
		log.info("결과 expense = {}", expense);
		
	    model.addAttribute("expense", expense);

	    model.addAttribute("date", date);

		return "/cashbook/cashbook_detail";
	}

	@GetMapping("/update")
	public String update(@RequestParam("id") int id, @RequestParam("date") String date, Model model) {
		log.info("ExpenseController update()");
		
		log.info("조회 요청 id = {}", id);
	    ExpenseDTO expense = expenseService.getExpenseDetailById(id);
	    log.info("결과 expense = {}", expense);
	    log.info("accountIn: {}", expense.getAccountIn());
	    log.info("methodIn: {}", expense.getMethodIn());
	    
		List<CategoryMainDTO> mainList = expenseService.getAllMainCategories();
	    model.addAttribute("mainList", mainList);
		model.addAttribute("expense", expense);
		model.addAttribute("date", date);

		return "/cashbook/cashbook_update";
	}

	//가계부 수기 입력 로직 시작 하는 부분=================================
	@GetMapping("/insert")
	public String insert(Model model) {
		log.info("ExpenseController insert()");
		 List<CategoryMainDTO> mainList = expenseService.getAllMainCategories();
		    model.addAttribute("mainList", mainList);

		return "/cashbook/cashbook_insert";
	}
	
		//카테고리 대분류 - 소분류 매칭
	@GetMapping("/category/{cmIn}")
	@ResponseBody
    public List<CategorySubDTO> getSubCategories(@PathVariable("cmIn") int cmIn) {
        return expenseService.getSubCategoriesByCmIn(cmIn);  
    }
	
		//카드 목록 가져오기 (신용 / 체크)
	@GetMapping("/cards/byMethod/{methodIn}")
	@ResponseBody
	public List<CardDTO> getCardsByMethod(@PathVariable("methodIn") int methodIn, Principal principal) {
		log.info("ExpenseController getCardsByMethod()");
	    String loginId = principal.getName();
	    Member member = memberService.findByMemberId(loginId).orElseThrow();
	    log.info("요청 들어온 memberIn={}, methodIn={}", member.getMemberIn(), methodIn);
	    return expenseService.findCardListByMethodAndMember(methodIn, member.getMemberIn());
	}
	 
		//계좌 목록 가져오기
	@GetMapping("/accounts")
	@ResponseBody
	public List<BankAccountDTO> getAccounts(Principal principal) {
	    String loginId = principal.getName();
	    Member member = memberService.findByMemberId(loginId).orElseThrow();
	    return expenseService.findAccountListByMember(member.getMemberIn());
	}
	
	// expenseSum 커스텀 변환기 등록 (,를 빼줌)
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.registerCustomEditor(Integer.class, "expenseSum", new PropertyEditorSupport() {
	        @Override
	        public void setAsText(String text) {
	            if (text != null) {
	                setValue(Integer.parseInt(text.replace(",", "")));
	            }
	        }
	    });
	}
	
	@PostMapping("/insertPro")
	public String insertPro(Expense expense, @RequestParam("expenseSum") String rawSum,  @RequestParam("method_in") int methodIn,  @RequestParam("cs_in") int csIn, 
							@RequestParam(value = "method2", required = false) String method2, Principal principal) {
	    log.info("ExpenseController insertPro()");
	    log.info("입력된 값: " + expense.toString());

	    // 수동으로 쉼표 제거하고 다시 세팅
	    int cleanSum = Integer.parseInt(rawSum.replace(",", ""));
	    expense.setExpenseSum(cleanSum);
	    
	    String loginId = principal.getName(); 
	    Member member = memberService.findByMemberId(loginId).orElseThrow();
	    expense.setMemberIn(member.getMemberIn());
	    expense.setCsIn(csIn);
	    expense.setMethodIn(methodIn);

	    // 카드 or 계좌라면 method2 세팅
	    if (methodIn == 1 || methodIn == 2) { // 신용 or 체크이면 cardIn에 들어가게 됨
	        expense.setCardIn(Integer.parseInt(method2));
	    } else if (methodIn == 4) { // 계좌면 acccountIn에 들어가게 됨
	        expense.setAccountIn(Integer.parseInt(method2));
	    }
	    
	    LocalDate inputDate = expense.getExpenseDate();

	    expenseService.saveExpense(expense);
	    return "redirect:/cashbook/list?date=" + inputDate; 
	}
	//가계부 수기 입력 로직 끝나는 부분=================================

	@GetMapping("/search")
	public String search() {
		log.info("ExpenseController search()");

		return "/cashbook/cashbook_search";
	}

}
