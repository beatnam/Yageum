package com.yageum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yageum.domain.EmailDTO;
import com.yageum.service.EmailService;

import lombok.extern.java.Log;

@Controller
@Log
@RequestMapping("/email/*")
public class EmailController {

    private final AdminController adminController;

	private final EmailService emailService;
	
	 public EmailController(EmailService emailService, AdminController adminController) {
	        this.emailService = emailService;
	        this.adminController = adminController;
	    }

	 @GetMapping("/send")
	 public String send() {
		 log.info("EmailController send()");
		  
		 
		 
		 
		 
		 return "/admin/notice_send";
	 }
	
	
	@PostMapping("/sendPro")
	public String sendPro(EmailDTO emailDTO) {
		log.info("EmailController sendPro()");
		log.info("받은내용" + emailDTO.toString());
		
		
		try {
			emailService.sendSimpleEmail(
					
					emailDTO.getTo(),
					emailDTO.getSubject(),
					emailDTO.getContent()
					);
			log.info("이메일 전송 요청 수신 및 처리 완료: to=" + emailDTO.getTo());
			return "이메일이 성공적으로 전송되었습니다.: " + emailDTO.getTo();
			
		}catch (Exception e) {

			e.printStackTrace();
			return "이메일 전송 실패: " + e.getMessage();
		}
	
		
	}

	
	
	
}
