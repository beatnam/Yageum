package com.yageum.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yageum.domain.MemberDTO;
import com.yageum.service.AdminService;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleController {

	private final AdminService adminService;

	private final MemberService memberService;

	@Scheduled(cron = "0 0 0 * * ?")
	public void questSchedule() {
		// 매일 0시 날짜가 변하면 오늘날짜와 마감일을 비교하고
		// 마감이 지난 퀘스트는 뜨지않게 설정
		adminService.changeQuestVaild();

	}

	@Scheduled(cron = "58 59 23 * * *")
	public void loginStraight() {
		// 매일밤 자정 직전에 최근 로그인 날짜가 오늘과 같은지 비교후 같으면 연속출석 + 1
		List<MemberDTO> memberList = memberService.listMemberLastLogin();
		System.out.println(memberList);
		for (int i = 0; i < memberList.size(); i++) {
			if ((memberList.get(i).getLastLoginDate()).equals(LocalDate.now())) {
				memberService.updateMemberStraight(memberList.get(i).getMemberIn());
			}
		}
	}

	
}
