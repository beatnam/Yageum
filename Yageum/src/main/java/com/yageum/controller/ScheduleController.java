package com.yageum.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yageum.domain.ExpenseDTO;
import com.yageum.domain.MemberDTO;
import com.yageum.domain.QuestStateDTO;
import com.yageum.service.AdminService;
import com.yageum.service.MemberService;
import com.yageum.service.QuestService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleController {

	private final AdminService adminService;

	private final MemberService memberService;

	private final QuestService questService;

	@Scheduled(cron = "0 0 0 * * ?")
	public void questSchedule() {
		// 매일 0시 날짜가 변하면 오늘날짜와 마감일을 비교하고
		// 마감이 지난 퀘스트는 뜨지않게 설정
		adminService.changeQuestVaild();
		adminService.changeQuestStateVaild();

	}

	@Scheduled(cron = "59 59 23 * * *")
	public void loginStraight() {
		// 매일밤 자정 직전에 최근 로그인 날짜가 오늘과 같은지 비교후 같으면 연속출석 + 1
		List<MemberDTO> memberList = memberService.listMemberLastLogin();
		for (int i = 0; i < memberList.size(); i++) {
			if ((memberList.get(i).getLastLoginDate()).equals(LocalDate.now())) {
				// 오늘 출석했으면 + 1
				memberService.updateMemberStraight(memberList.get(i).getMemberIn());
			} else {
				// 안했으면 0
				memberService.updateMemberStraightZero(memberList.get(i).getMemberIn());
			}
		}
	}

	// cron = "0/1 * * * * *"
	@Scheduled(cron = "59 59 23 * * *")
	public void expenseStraight() {
		List<ExpenseDTO> memberExpense = memberService.listMemberLastExpense();

		System.out.println(memberExpense);
		for (int i = 0; i < memberExpense.size(); i++) {
			if ((memberExpense.get(i).getExpenseDate()).equals(LocalDate.now())) {
				memberService.updateMemberExpense(memberExpense.get(i).getMemberIn());
			} else {
				memberService.updateMemberExpenseZero(memberExpense.get(i).getMemberIn());
			}
		}
	}

	@Scheduled(cron = "0 0 0 1 * *")
	public void successType4() {
		// 4유형(절약)을 수행하는 멤버 번호 및 퀘스트 번호를 갖고옴
		List<QuestStateDTO> memberQuest4 = questService.listQuestType4();
		
		
	}

}
