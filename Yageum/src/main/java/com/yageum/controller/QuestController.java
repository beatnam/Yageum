package com.yageum.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.domain.ItemDTO;
import com.yageum.domain.QuestStateDTO;
import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/quest/*")
public class QuestController {

	private final QuestService questService;

	@GetMapping("/list")
	public String listQuest(Model model, @AuthenticationPrincipal UserDetails userDetails) {

		// 접속중인 userDetails에서 Id 찾아서 memberIn 검색 해오기
		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);

		List<Map<Object, Object>> questList = questService.listQuest(memberIn);

		System.out.println(questList);

		model.addAttribute("questList", questList);
		return "/quest/list";
	}

	@GetMapping("/item")
	public String listItem(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		log.info("QuestController item()");
		String memberId = userDetails.getUsername();

		int myReward = questService.myReward(memberId);

		List<ItemDTO> itemList = questService.listItem();
		model.addAttribute("itemList", itemList);
		model.addAttribute("myReward", myReward);
		return "/quest/item";
	}

	@PostMapping("/buy_item")
	public String buyItem(@AuthenticationPrincipal UserDetails userDetails, ItemDTO itemDTO) {
		log.info("QuestController buyItem()");
		
		System.out.println(itemDTO);
		
		ItemDTO itemDTO1 = questService.getItemInfo(itemDTO);
		
		
		
		String memberId = userDetails.getUsername();
		int myReward = questService.myReward(memberId);
		int itemPrice = itemDTO1.getItemPrice();
		int memberIn = questService.searchMemberIn(memberId);
		

		// Member 테이블을 수정할 정보를 맵에 담음
		Map<String, Object> buyItemUser = new HashMap<>();
		buyItemUser.put("memberId",memberId);
		buyItemUser.put("itemPrice", itemPrice);

		// Transaction(구매 내역) 테이블을 수정할 정보를 맵에 담음
		Map<String, Object> buyItemTransaction = new HashMap<>();
		buyItemTransaction.put("memberIn",memberIn);
		buyItemTransaction.put("itemIn",itemDTO1.getItemIn());
		buyItemTransaction.put("transactionDate", LocalDateTime.now());

		
		if (myReward >= itemPrice) {
			questService.buyItemUser(buyItemUser);
			questService.buyItemTransaction(buyItemTransaction);
			return "redirect:/quest/buy_success";

		} else {

			return "redirect:/quest/buy_failure";
		}
	}

	@GetMapping("/buy_success")
	public String buySuccess() {

		return "/quest/buy_success";
	}

	@GetMapping("/buy_failure")
	public String buyFailure() {

		return "/quest/buy_failure";
	}

	@PostMapping("/acceptQuest")
	public String acceptQuest(@RequestParam("questIn") int questIn, @AuthenticationPrincipal UserDetails userDetails) {

		String memberId = userDetails.getUsername();
		System.out.println(userDetails.getUsername());

		int memberIn = questService.searchMemberIn(memberId);
		System.out.println(memberIn);

		QuestStateDTO questStateDTO = new QuestStateDTO();
		questStateDTO.setQuestIn(questIn);
		questStateDTO.setMemberIn(memberIn);

		questService.acceptQuest(questStateDTO);

		return "redirect:/quest/list";
	}
}
