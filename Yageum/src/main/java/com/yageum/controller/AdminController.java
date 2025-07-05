package com.yageum.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.ItemDTO;
import com.yageum.domain.NoticeDTO;
import com.yageum.domain.QuestDTO;
import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
import com.yageum.entity.Expense;
import com.yageum.entity.Member;
import com.yageum.repository.CategoryMainRepository;
import com.yageum.service.AdminService;
import com.yageum.service.CategoryService;
import com.yageum.service.ConsumptionService;
import com.yageum.service.ExpenseService;
import com.yageum.service.ItemService;
import com.yageum.service.MemberService;
import com.yageum.service.NoticeService;
import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/admin/*")
public class AdminController {

	//로깅(Logging)
	//프로그램 오류 데이터 콘솔에 기록하는 도구
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	//service
	private final MemberService memberService;
	private final CategoryService categoryService;
	private final AdminService adminService;
	private final ItemService itemService;
	private final NoticeService noticeService;
	private final ConsumptionService consumptionService;
	private final ExpenseService expenseService;
	private final QuestService questService;
	
	//Repository			
	private final CategoryMainRepository categoryMainRepository;

	// 회원 관리 페이지
	@GetMapping("/user")
	public String user(Model model) {
		log.info("AdminController user()");

		List<Member> member = memberService.adminInfo();

		model.addAttribute("member", member);

		log.info(member.toString());

		return "/admin/admin_user";
	}

	@GetMapping("/user_detail")
	public String user_update(@RequestParam("memberId") String memberId, Model model) {
		log.info("AdminController user_detail()");
		Optional<Member> member = memberService.findByMemberId(memberId);
		
		
		//퀘스트, 카드, 계좌 정보 가지고 오기
		if(member.isPresent()) {
			Member member2 = member.get();
			
			int memberIn = member2.getMemberIn();
			
			List<BankAccount> memberBank = expenseService.getAccountList(memberIn);
			List<Card> memberCard = expenseService.cardByMemberIn(memberIn);
			List<Map<Object, Object>> memberQuest = questService.listQuest(memberIn);
			
			model.addAttribute("quest", memberQuest);
			model.addAttribute("bank", memberBank);
			model.addAttribute("card" ,memberCard);
		}else {
			log.info("받아온 유저가 없습니다");
		}
		
		
		
		log.info("가지고 온 값" + member.toString() + "===================");
		
		
		
		model.addAttribute("member", member.get());



		return "/admin/user_detail";
	}

	
	
	
	@PostMapping("/authority")
	@ResponseBody				//페이지 찾는 리턴 필요 없을시 사용하기
	public void authority(Member member2) {
		log.info("AdminController authority()");
//		log.info("변경된 권한" + member2.getzMemberRole().toString());
		Member member = memberService.find (member2.getMemberId());
		member.setMemberRole(member2.getMemberRole());
		log.info("member 값 " + member.toString());
		
		memberService.save(member);
		
		
	}

	@GetMapping("/state")
	public String state(Model model) {
		log.info("AdminController state()");
	//
		List<CategorySub> categorySub = categoryService.cateSFindAll();
		List<Member> memberList = memberService.adminInfo();
		List<BankAccount> bankAccount = expenseService.accountAll();
//		log.info(bankAccount.toString());
		List<Card> cardList = expenseService.cardAll();
		log.info(categorySub.toString());
		List<Expense> cateExpense = expenseService.expenseAll();
		log.info(cateExpense.toString());
		
		//차트 데이터 가공
		
		try {	//월별 차트 데이터
		int currentYear = LocalDate.now().getYear(); // 현재 연도 가져오기 (예: 2025)

		// 올해 가입한 회원만 필터링하고, 월별 가입자 수를 집계합니다.
		Map<Integer, Long> monthlyCounts = memberList.stream()
				.filter(member -> member.getCreateDate() != null && member.getCreateDate().getYear() == currentYear) // null 체크 및 올해만 필터링
				.collect(Collectors.groupingBy(
					member -> member.getCreateDate().getMonthValue(), // 월(1~12) 기준으로 그룹화
					Collectors.counting() // 각 그룹의 요소(멤버) 수 세기
				));
		
		List<String> monthlyLabels = new ArrayList<>();
		List<Long> monthlyData = new ArrayList<>();     

		for (int monthNum = 1; monthNum <= 12; monthNum++) {
			monthlyLabels.add(Month.of(monthNum).getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()));
			monthlyData.add(monthlyCounts.getOrDefault(monthNum, 0L));
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		model.addAttribute("monthlyLabelsJson", objectMapper.writeValueAsString(monthlyLabels));
		model.addAttribute("monthlyDataJson", objectMapper.writeValueAsString(monthlyData));
		
		
		
		}catch(Exception e ) {
			e.printStackTrace();
		}
		try {	//년도 별 차트 
			Map<Integer, Long> yearlyCounts = memberList.stream() // 'member' 변수 사용
					.filter(m -> m.getCreateDate() != null) // 'm'은 stream 내부의 개별 Member 객체
					.collect(Collectors.groupingBy(
						m -> m.getCreateDate().getYear(),
						Collectors.counting()
					));

			List<String> yearlyLabels = new ArrayList<>();
			List<Long> yearlyData = new ArrayList<>();     

			yearlyCounts.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(entry -> {
						yearlyLabels.add(entry.getKey() + "년");
						yearlyData.add(entry.getValue());
					});
			
			ObjectMapper objectMapper = new ObjectMapper();

			model.addAttribute("yearlyLabelsJson", objectMapper.writeValueAsString(yearlyLabels));
			model.addAttribute("yearlyDataJson", objectMapper.writeValueAsString(yearlyData));
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		//chart 연도별 
		
		
		//카테고리 비율 차트 
		try {
			cateExpense.forEach(exp -> logger.debug("Expense: csIn={}, expenseSum={}, expenseType={}", exp.getCsIn(), exp.getExpenseSum(), exp.isExpenseType()));


	        Map<Integer, String> csInToNameMap = categorySub.stream()
	                .collect(Collectors.toMap(CategorySub::getCsIn, CategorySub::getCsName));

	        Map<String, Double> categoryExpenseSum = new HashMap<>();
	        double totalExpense = 0.0; // 전체 지출 합계를 저장할 변수

	        // --- 이 부분을 수정합니다: expenseType이 boolean 타입임을 감안하여 필터링 ---
	        for (Expense expense : cateExpense) {
	            // 가정: expenseType이 true이면 '지출', false이면 '수입'
	            // 따라서, expenseType이 true인 경우에만 (즉, 지출인 경우) 처리합니다.
	            if (expense.isExpenseType()) { // boolean 타입의 Getter는 'is'로 시작하는 것이 관례
	                Integer csIn = expense.getCsIn();
	                String categoryName = csInToNameMap.getOrDefault(csIn, "알 수 없음 (CS_IN: " + csIn + ")");

	                // expenseSum이 int이므로 double로 변환하여 계산합니다.
	                double currentExpenseSum = (double) expense.getExpenseSum();
	                categoryExpenseSum.put(categoryName, categoryExpenseSum.getOrDefault(categoryName, 0.0) + currentExpenseSum);
	                totalExpense += currentExpenseSum; // 전체 지출 합계에 추가
	            } else {
	                // 수입 데이터는 로그로 남기고 건너뜁니다.
	                logger.debug("Skipping income data: expense_type = false for Expense: {}", expense);
	            }
	        }
		        // --- 여기서부터 수정 ---
		        List<String> chartLabels = new ArrayList<>(categoryExpenseSum.keySet());
		        List<Double> chartData = new ArrayList<>(categoryExpenseSum.values()); // 퍼센티지 대신 원본 합계 금액을 넣습니다!


		        model.addAttribute("chartLabels", chartLabels);
		        model.addAttribute("chartData", chartData); // 이 데이터는 이제 원본 금액입니다.
		        model.addAttribute("totalExpense", totalExpense); // 총 지출 금액도 반드시 넘겨줍니다.

		}catch(Exception e) {
			e.printStackTrace();
		}
		//카테고리 비율 차트 
		
	
		
	
		
		model.addAttribute("card", cardList);
		model.addAttribute("bank", bankAccount);
		model.addAttribute("member", memberList);
		model.addAttribute("cateSub", categorySub);
		
		return "/admin/admin_state";
	}

	// 통계 / 리포트 페이지

	
	
	
	
	
	
	
	// 사이트 설정 - 카테고리 설정 페이지
	@GetMapping("/category")
	public String category(Model model) {
		log.info("AdminController category()");

		List<CategoryMain> categoryMain = categoryService.cateMFindAll();
		List<CategorySub> categorySub = categoryService.cateSFindAll();
		
		model.addAttribute("cateMain", categoryMain);
		model.addAttribute("cateSub", categorySub);
		
		
		
		return "/admin/admin_category";
	}

	
	@GetMapping("/category_gener")
	public String cateGener(Model model) {
		log.info("AdminController categoryGerner()");

		List<CategoryMain> categoryMain = categoryService.cateMFindAll();

		
		model.addAttribute("cateMain", categoryMain);
		
		return "/admin/category_gener";
	}
	
	@PostMapping("/category_generPro1")
	@ResponseBody		//대분류 카테고리 생성하는 로직
	public void cateGenerPro1(@RequestParam("categoryName") String cmName) {
		log.info("AdminController cateGenerPro1()");
		
		categoryService.save(cmName);
		
	}
	
	
	
	
	@PostMapping("/category_generPro2")
	@ResponseBody		//소분류 카테고리 생성하는 로직
	public void cateGenerPro2(@RequestParam("categoryName") String csName,
								  @RequestParam("parentCategory") int cmIn) {
		
		log.info("AdminController cateGenerPro2()");
		
		
		CategorySub categorySub = new CategorySub();
		categorySub.setCmIn(cmIn);
		categorySub.setCsName(csName);
		
		categoryService.save2(categorySub);
		
		
	}
	
	
	
	//대분류 페이지  / 변경

	@GetMapping("/category_update1")
	public String cateUpdate1(@RequestParam("cmIn")int cmIn, Model model) {
		log.info("AdminController cateUpdate1()");
		Optional<CategoryMain> categoryMain = categoryService.findById1(cmIn);
		
		
		model.addAttribute("cateMain", categoryMain.get());
		
		return "/admin/category_update1";
	}
	
	@PostMapping("/category_updatePro1")
	@ResponseBody		//소분류 카테고리 생성하는 로직
	public void cateUpdatePro1(@RequestParam("categoryNum") int cmIn,
						@RequestParam("categoryName") String cmName) {
		
		log.info("AdminController cateUpdatePro1()");
		
		CategoryMain cateFound = categoryService.findById1(cmIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		cateFound.setCmName(cmName);
		
		categoryService.update(cateFound);
	
		
		
	}
	
	@GetMapping("/category_delete1")
	public String cateDelete1(@RequestParam("cmIn") int cmIn, Model model) {
		log.info("AdminController cateDelete1()");
		
		CategoryMain cateFound = categoryService.findById1(cmIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		
		
			
			categoryService.deleteAll(cmIn);
			categoryService.delete1(cateFound);
			
			
			return "redirect:/admin/category";

	}
	
	
		//소분류 페이지  / 변경
	@GetMapping("/category_update2")
	public String cateUpdate2(@RequestParam("csIn") int csIn , Model model) {
		log.info("AdminController cateUpdate2()");
		
		Optional<CategorySub> categorySub = categoryService.findById2(csIn);
		log.info("대분류 카테고리 전부 가지고 오기");
		List<CategoryMain> categoryMain = categoryMainRepository.findAll();
		
		model.addAttribute("cateSub", categorySub.get());
		model.addAttribute("cateMain", categoryMain);

		return "/admin/category_update2";
	}
	
	
	@PostMapping("/category_updatePro2")
	@ResponseBody		//소분류 카테고리 생성하는 로직 	categoryNum
	public void cateUpdatePro2(@RequestParam("parentCategory") int cmIn,
						@RequestParam("categoryName") String csName, @RequestParam("categoryNum") int csIn) {
		
		log.info("AdminController cateUpdatePro2()");
		
		CategorySub cateFound = categoryService.findById2(csIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		cateFound.setCsName(csName);
		cateFound.setCmIn(cmIn);
		
		categoryService.update2(cateFound);
		
		
	}
	
	@GetMapping("/category_delete2")
	public String cateDelete2(@RequestParam("csIn") int csIn) {
		log.info("AdminController cateDelete2()");

		CategorySub cateFound = categoryService.findById2(csIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		categoryService.delete2(cateFound);
		return "redirect:/admin/category";
	}
	// 사이트 설정 - 카테고리 설정 페이지

	// 사이트 설정 - 퀘스트 설정 페이지

	@GetMapping("/quest")
	public String quest(Model model) {
		log.info("AdminController quest()");

		List<Map<Object, Object>> questList = adminService.listQuest();

		System.out.println(questList);
		model.addAttribute("questList", questList);
		return "/admin/admin_quest";
	}

	@GetMapping("/quest_gener")
	public String questgener(Model model) {
		log.info("AdminController questgener()");

		List<CategoryMainDTO> categoryMainList = adminService.showCategoryMain();

		model.addAttribute("categoryMainList", categoryMainList);
		return "/admin/quest_gener";
	}

	@PostMapping("/insert_Q")
	@ResponseBody
	public String insertQuest(QuestDTO questDTO) {
		log.info("AdminController insertQuest()");
		// 입력값 확인
		System.out.println(questDTO);

		if (questDTO.getQuestTypeIn() == 2 || questDTO.getQuestTypeIn() == 3) {
			questDTO.setCmIn(null);
			questDTO.setCsIn(null);
		}

		questDTO.setValid(true);
		adminService.insertQuest(questDTO);

		return "redirect:/admin/admin_quest";
	}

	@GetMapping("/categorySelect")
	@ResponseBody
	public List<CategorySubDTO> subCategorySelect(@RequestParam("cmIn") int cmIn) {
		log.info("소분류 카테고리를 불러오기 위한 ajax");

		List<CategorySubDTO> result = adminService.subCategorySelect(cmIn);
		System.out.println(result);
		return result;
	}

	@GetMapping("/quest_update")
	public String questDetail(Model model, @RequestParam("questIn") int questIn) {
		log.info("AdminController questUpdate()");
		System.out.println(questIn);
		QuestDTO questDTO= adminService.questDetail(questIn);
		List<CategoryMainDTO> categoryMainList = adminService.showCategoryMain();
		
		model.addAttribute("quest", questDTO);
		model.addAttribute("categoryMainList", categoryMainList);
		
		return "/admin/quest_update";
	}
	// 사이트 설정 - 퀘스트 설정 페이지

	@PostMapping("/update_Q")
	@ResponseBody
	public String updateQuest(QuestDTO questDTO) {
		log.info("AdminController updateQuest()");
		// 입력값 확인
		System.out.println(questDTO);

		if (questDTO.getQuestTypeIn() == 2 || questDTO.getQuestTypeIn() == 3) {
			questDTO.setCmIn(null);
		}

		adminService.updateQuest(questDTO);
		
		return "redirect:/admin/admin_quest";
	}
	
	@GetMapping("/quest_delete")
	public String deleteQuest(@RequestParam("questIn") int questIn) {
		log.info("AdminController deleteQuest()");
		
		adminService.deleteQuest(questIn);
		
		return "redirect:/admin/quest";
	}
	
	
	
	
	
	// 사이트 설정 - 공지사항 페이지

	@GetMapping("/notification")
	public String noticfication(Model model) {
		log.info("AdminController noticfication()");

		List<NoticeDTO> noticeList = noticeService.listNotice();
		log.info(noticeList.toString());
		model.addAttribute("noticeL", noticeList);
		
		
		
		return "/admin/admin_notification";
	}

	@GetMapping("/notice_gener")
	public String noticeGener(Model model) {
		log.info("AdminController noticeGener()");

		
		return "/admin/notice_gener";
	}
	@PostMapping("/notice_generPro")
	@ResponseBody
	public void noticeGenerPro(NoticeDTO noticeDTO, Model model) {
		log.info("AdminController noticeGenerPro()");
		log.info(noticeDTO.toString());

		
		
		noticeService.insert(noticeDTO);
	}
	
	@GetMapping("/notice_update")
	public String noticeUpdate(@RequestParam("noticeIn")int noticeIn ,Model model) {
		log.info("AdminController noticeUpdate()");
		
		Optional<NoticeDTO> noticeDTO = noticeService.findByIn(noticeIn);
		
		
		model.addAttribute("notice", noticeDTO.get());
		return "/admin/notice_update";

	}
	
	@PostMapping("/notice_updatePro")
	@ResponseBody
	public void noticeUpdatePro(@RequestParam("noticeIn") int noticeIn, 
			@RequestParam Map<String, Object> map, NoticeDTO noitceDTO) {
		log.info("AdminController noticeUpdatePro()");
		log.info(noitceDTO.toString());

//		NoticeDTO noticeDTO2 = noticeService.findByIn(noticeIn).orElseThrow(()
//				-> new UsernameNotFoundException("없는 카테고리")
//				);
//		noticeDTO2.setNoticeContent(map.get("noticeContent"));
//		noticeDTO2.setNoticeDate(map.get("noticeDate"));
//		noticeDTO2.setNoticeSubject(map.get("noticeSubject"));

		
		noticeService.update(noitceDTO);
	}
	
	
	
	@GetMapping("/notice_delete")
	public String noticeDelete(@RequestParam("noticeIn") int noticeIn) {
		log.info("AdminController noticeDelete()");
			
		
		noticeService.delete(noticeIn);
		
		return "redirect:/admin/notification";
	}
	
	
	// 사이트 설정 - 공지사항 페이지

	// 사이트 설정 - 상품 설정 페이지

	@GetMapping("/item")
	public String item(Model model) {
		log.info("AdminController item()");

		
		List<ItemDTO> itemList = itemService.findAll();
		
		
		model.addAttribute("itemDTO", itemList);
		
		
		
		return "/admin/admin_item";
	}

		//아이템 생성 로직
	@GetMapping("/item_gener")
	public String itemGener(Model model) {
		log.info("AdminController itemGener()");
		
		
		return "/admin/item_gener";
	}
	@PostMapping("/item_generPro")
	@ResponseBody
	public void itemGenerPro(ItemDTO itemDTO) {
		log.info("AdminController itemGenerPro()");
		log.info(itemDTO.toString());
		itemService.saveItem(itemDTO);
		
		
		
	}
		
		
		
	@GetMapping("/item_update")
	public String itemUpdate(ItemDTO itemDTO,Model model) {
		log.info("AdminController itemUpdate()");
		ItemDTO itemFind = itemService.findByItemIn(itemDTO.getItemIn());
//		log.info(itemFind.toString());
		
		
		model.addAttribute("itemDTO", itemFind);
	
		return "/admin/item_update";
	}
	
	@PostMapping("/item_updatePro")
	@ResponseBody		//아이템 변경 로직
	public void itemUpdatePro(ItemDTO itemDTO) {
		log.info("AdminController itemUpdatePro()");
//		log.info(itemDTO.toString());
		ItemDTO itemFind = itemService.findByItemIn(itemDTO.getItemIn());
		
		itemFind.setItemName(itemDTO.getItemName());
		itemFind.setItemPrice(itemDTO.getItemPrice());
		
		itemService.updateItem(itemFind);
		
	}
	@GetMapping("/item_delete")
	@ResponseBody		//아이템 변경 로직
	public void itemdelete(@RequestParam("itemIn")int itemIn) {
		log.info("AdminController itemdelete()");
		ItemDTO itemFind = itemService.findByItemIn(itemIn);
		log.info(itemFind.toString());
		
		itemService.deleteItem(itemFind);
		
	}
	
	
	// 사이트 설정 - 상품 설정 페이지

}
