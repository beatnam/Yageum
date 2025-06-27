package com.yageum.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.domain.NoticeDTO;
import com.yageum.service.NoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/notice/*")
public class NoticeController {

	private final NoticeService noticeService;
	
	
	@GetMapping("/main")
	public String main(Model model) {
		log.info("NoticeController main()");
		List<NoticeDTO> noticeList = noticeService.listNotice();
		
		model.addAttribute("notice", noticeList);
		
		return "/notice/main";
	}
	@GetMapping("/content")
	public String content(@RequestParam("noticeIn") int noticeIn, Model model) {
		log.info("NoticeController content()");
		
		Optional<NoticeDTO> noticeDTO = noticeService.findByIn(noticeIn);
		
		
		model.addAttribute("notice", noticeDTO.get());
		
		
		
		
		return "/notice/content";
	}
	
	
	
}
