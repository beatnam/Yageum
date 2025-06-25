package com.yageum.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.ItemDTO;
import com.yageum.domain.NoticeDTO;
import com.yageum.domain.QuestDTO;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.CategorySub;
import com.yageum.entity.Member;
import com.yageum.repository.CategoryMainRepository;
import com.yageum.service.AdminService;
import com.yageum.service.CategoryService;
import com.yageum.service.ItemService;
import com.yageum.service.MemberService;
import com.yageum.service.NoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/admin/*")
public class AdminController {

	
	//service
	private final MemberService memberService;
	private final CategoryService categoryService;
	private final AdminService adminService;
	private final ItemService itemService;
	private final NoticeService noticeService;
	
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
	public String state() {
		log.info("AdminController state()");

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
	@ResponseBody	
	public void cateDelete1(@RequestParam("cmIn") int cmIn) {
		log.info("AdminController cateDelete1()");
		
		CategoryMain cateFound = categoryService.findById1(cmIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		categoryService.delete1(cateFound);
		
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
	@ResponseBody	
	public void cateDelete2(@RequestParam("csIn") int csIn) {
		log.info("AdminController cateDelete2()");

		CategorySub cateFound = categoryService.findById2(csIn).orElseThrow(()
				-> new UsernameNotFoundException("없는 카테고리")
				);
		
		categoryService.delete2(cateFound);
		
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
			questDTO.setCsIn(null);
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

	@GetMapping("/noticfication")
	public String noticfication(Model model) {
		log.info("AdminController noticfication()");

		List<NoticeDTO> noticeList = noticeService.listNotice();
		log.info(noticeList.toString());
		model.addAttribute("noticeL", noticeList);
		
		
		
		return "/admin/admin_noticfication";
	}

	@GetMapping("/notice_gener")
	public String noticeGener(Model model) {
		log.info("AdminController noticeGener()");

		
		return "/admin/notice_gener";
	}
	@PostMapping("/notice_generPro")
	public String noticeGenerPro(NoticeDTO noticeDTO, Model model) {
		log.info("AdminController noticeGenerPro()");
		
		noticeService.insert(noticeDTO);
		
		
		
		
		return "/admin/notice_gener";
	}
	
	@GetMapping("/notice_update")
	public String noticeUpdate(Model model) {
		log.info("AdminController noticeUpdate()");

		return "/admin/notice_update";

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
