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

		adminService.changeQuestVaild();

	}

}
