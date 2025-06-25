package com.yageum.controller;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

import com.yageum.domain.ExpenseDTO;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
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
	private final MemberRepository memberRepository;
	
	// id 가져오기 공통메서드
	private int getLoginMemberIn() {
	    String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
	    Member member = memberRepository.findByMemberId(memberId);
	    return member.getMemberIn();
	}
	
	//메인 페이지
	@GetMapping("/main")
	public String main() {
		log.info("ExpenseController main()");
		

		return "/cashbook/cashbook_main";
	}

	//달력에 로그인한 아이디의 일별 expense 정보 출력
	@GetMapping("/monthsum")
	@ResponseBody
	public List<Map<String, Object>> getMonthlySum(@RequestParam("year") int year, @RequestParam("month") int month){
		log.info("ExpenseController monthsum() : {}년 {}월", year, month);
		
		int memberIn = getLoginMemberIn();
		
		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
	    
	    return expenseService.getMonthlySum(memberIn, start, end);
	}
	
	
	//일별 내역 페이지
	@GetMapping("/list")
	public String list(@RequestParam("date") String date, Model model) {
	    log.info("ExpenseController list() - date: {}", date);

	    // 로그인된 사용자 ID 가져오기 (Spring Security)
	    int memberIn = getLoginMemberIn();
	    log.info("memberIn : ", memberIn);

	    // 서비스 호출
	    List<Expense> expenses = expenseService.getExpensesByDate(memberIn, date);
	    int incomeTotal = expenseService.getDailyTotal(memberIn, date, 0);
	    int expenseTotal = expenseService.getDailyTotal(memberIn, date, 1);
	    int balance = incomeTotal - expenseTotal;

	    // 모델에 담기
	    model.addAttribute("expenses", expenses);
	    model.addAttribute("incomeTotal", incomeTotal);
	    model.addAttribute("expenseTotal", expenseTotal);
	    model.addAttribute("balance", balance);

		return "/cashbook/cashbook_list";
	}

	//상세 내역 페이지
	@GetMapping("/detail")
    public String detail(@RequestParam("id") int id, @RequestParam("date") String date, Model model) {
        log.info("ExpenseController detail()");
        ExpenseDTO expense = expenseService.getExpenseDetailById(id);
        log.info("조회된 expense: {}", expense);

        model.addAttribute("expense", expense);
        model.addAttribute("date", date);
        return "/cashbook/cashbook_detail";
    }


	//가계부 수정하기
	@GetMapping("/update")
	public String update(@RequestParam("id") int id, @RequestParam("date") String date, Model model) {
		log.info("ExpenseController update()");
		ExpenseDTO expense = expenseService.getExpenseDetailById(id);
	    
		List<CategoryMain> mainList = expenseService.getMainCategoryList();
		log.info("expense = {}", expense);
	    model.addAttribute("mainList", mainList);
		model.addAttribute("expense", expense);
		model.addAttribute("date", date);

		return "/cashbook/cashbook_update";
	}
	
	//수정 로직
	@PostMapping("/updatePro")
	public String updatePro(ExpenseDTO expenseDTO) {
		log.info("ExpenseController updatePro()");
		
		expenseService.update(expenseDTO);
		
		return "redirect:/cashbook/detail?id=" + expenseDTO.getExpenseIn() + "&date=" + expenseDTO.getExpenseDate();
	}

	//가계부 수기 입력 로직 시작 하는 부분=================================
	@GetMapping("/insert")
	public String insert(Model model) {
		log.info("ExpenseController insert()");
		
		List<CategoryMain> mainList = expenseService.getMainCategoryList();
		model.addAttribute("mainList", mainList);
		model.addAttribute("selectedDate", LocalDate.now());

		return "/cashbook/cashbook_insert";
	}
	
	//카테고리 대분류 - 소분류 매칭
	@GetMapping("/category/{cmIn}")
	@ResponseBody
	public List<CategorySub> getSubCategories(@PathVariable("cmIn") int cmIn) {
        return expenseService.getSubCategoryList(cmIn);
    }
	
	//카드 목록 가져오기 (신용 / 체크)
	@GetMapping("/cards/byMethod/{methodIn}")
	@ResponseBody
	 public List<Card> getCardsByMethod(@PathVariable("methodIn") int methodIn) {
		int memberIn = getLoginMemberIn();
        return expenseService.getCardList(memberIn, methodIn);
    }
	 
	//계좌 목록 가져오기
	@GetMapping("/accounts")
	@ResponseBody
	public List<BankAccount> getAccounts() {
		int memberIn = getLoginMemberIn();
        return expenseService.getAccountList(memberIn);
    }

	
	// 쉼표 제거 (금액 입력 시)
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
							@RequestParam(value = "method2", required = false) String method2) {
	    log.info("ExpenseController insertPro()");
	    log.info("입력된 값: " + expense.toString());

	    int memberIn = getLoginMemberIn();

        expense.setExpenseSum(Integer.parseInt(rawSum.replace(",", "")));
        expense.setMemberIn(memberIn);
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

	//월별 내역 / 검색 페이지
	@GetMapping("/search")
	public String search() {
	    log.info("ExpenseController search()");

	    return "/cashbook/cashbook_search";
	}
	
	@GetMapping("/monthList")
	@ResponseBody
	public List<ExpenseDTO> getMonthList(@RequestParam("year") int year, @RequestParam("month") int month){
		log.info("ExpenseController monthList() : {}년 {}월", year, month);
		
		int memberIn = getLoginMemberIn();
		
		LocalDate start = LocalDate.of(year, month, 1);
	    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
		
	    return expenseService.getMonthList(memberIn, start, end);
	}

}
