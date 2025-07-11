package com.yageum.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yageum.domain.EmailDTO;
import com.yageum.domain.NoticeDTO;
import com.yageum.entity.Member;
import com.yageum.service.EmailService;
import com.yageum.service.MemberService;
import com.yageum.service.NoticeService;

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
	private final NoticeService noticeService;
	
	

	
	
	
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
		
		List<String> emails = member.stream()
				.filter(m -> m.getEmailConsent() == true)
				.map(m -> m.getMemberEmail())
				.collect(Collectors.toList());
		
		

				
		if(emails.isEmpty()) {
			return "이메일 수신 동의를 한 대상자가 없습니다.";
		}
		
		
		try {	
			Date currentDateTime = new Date(System.currentTimeMillis());
			String subject = currentDateTime + "업데이트 사항을 이메일로 전송하였습니다";
			
			log.info(subject.toString());

			
			//받아온 공지 정보 넘겨주기
			emailService.changeType(
					emails,
					emailDTO.getSubject(),
					emailDTO.getContent()
					);
			log.info("이메일 전송 요청 수신 및 처리 완료: to=" + emailDTO.getTo());
			
			
			
			
			
			// 팝업 공지사항 이용하기
			NoticeDTO noticeDTO = new NoticeDTO();
			noticeDTO.setNoticeSubject(subject); 
			noticeDTO.setNoticeContent(emailDTO.getContent());
			noticeDTO.setNoticeDate(currentDateTime);
			
			noticeService.insert(noticeDTO); 
			
			
			return "이메일이 성공적으로 전송되었습니다.: " + emailDTO.getTo();
			
		}catch (Exception e) {

			e.printStackTrace();
			return "이메일 전송 실패: " + e.getMessage();
		}
	
		
		
		
		
		
		
		
		
		
	}

	
	
	
}
