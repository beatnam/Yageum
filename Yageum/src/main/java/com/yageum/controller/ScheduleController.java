package com.yageum.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yageum.service.AdminService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleController {

	private final AdminService adminService;

	
	@Scheduled(cron = "0 0 0 * * ?")
	public void questSchedule() {
		// 매일 0시 날짜가 변하면 오늘날짜와 마감일을 비교하고
		// 마감이 지난 퀘스트는 뜨지않게 설정
		adminService.changeQuestVaild();

	}

}
