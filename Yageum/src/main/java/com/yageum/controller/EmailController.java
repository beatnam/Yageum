package com.yageum.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yageum.domain.EmailDTO;
import com.yageum.entity.Member;
import com.yageum.service.EmailService;
import com.yageum.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@Log
@RequiredArgsConstructor
@RequestMapping("/email/*")
public class EmailController {

    private final AdminController adminController;
	private final MemberService memberService;
	private final EmailService emailService;
	
	
	

	
	
	
	 @GetMapping("/send")
	 public String send() {
		 log.info("EmailController send()");
		  
		 
		 
		 
		 
		 return "/admin/notice_send";
	 }
	
	
	@PostMapping("/sendPro")
	public String sendPro(EmailDTO emailDTO) {
		log.info("EmailController sendPro()");
		log.info("받은내용" + emailDTO.toString());
		List<Member> member = memberService.adminInfo();
		
		List<Map<String, String>> emails = member.stream()
				.filter(m -> m.getEmailConsent() == true)
				.map(m -> Map.of("to", m.getMemberEmail()))
				.collect(Collectors.toList());
						
						
		
						
				

				
				
		try {
			emailService.sendSimpleEmail(
					
//					emails.get(),
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
