package com.yageum.service;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.java.Log;

@Service
@Log
public class EmailService {

	 private final JavaMailSender mailSender;
	 
	 
	 
	 public EmailService(JavaMailSender mailSender) {
	        this.mailSender = mailSender;
	 }
	 
	 
	 public void sendSimpleEmail(String toEmail, String subject, String content) {
		 SimpleMailMessage message = new SimpleMailMessage();
		 
//		 message.setFrom("rudgh2436@gmail.com");	하드코딩 지정된 수신자
		 
		 message.setTo(toEmail);		//수신자 이메일 주소 설정
		 message.setSubject(subject);	//이메일 제목 설정
		 message.setText(content);			//이메일 본문 내용 설정
		 
		 mailSender.send(message);
		 log.info("이메일 전송 완료: " + toEmail + " (제목: " + subject + ")");
		 
	 }
	 
	 /*
	 public void sendSimpleEmails(String[] toEmails, String subject, String content) {
		 SimpleMailMessage message = new SimpleMailMessage();
		 message.setTo(toEmails); // 여러 수신자 설정 (String 배열)
	     message.setSubject(subject);
	     message.setText(content);
	     
	     try {
	            mailSender.send(message);
	            // 배열을 문자열로 변환하여 로깅 (예: [email1@a.com, email2@b.com])
	            log.info("이메일 전송 성공 (여러 수신자): To=" + String.join(", ", toEmails) + ", Subject=" + subject);
	        } catch (Exception e) {
	            log.severe("이메일 전송 실패 (여러 수신자): To=" + String.join(", ", toEmails) + ", Subject=" + subject + ", Error=" + e.getMessage());
	            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.", e);
	     }
	     
	 }
	 
	 public void sendSimpleEmails(List<String> toEmails, String subject, String content) {
	        // List<String>을 String[]으로 변환하여 기존 메서드 호출
		 sendSimpleEmails(toEmails.toArray(new String[0]), subject, content);
	    }
	 
	 
	 
	  첨부파일 보내는 메서드 
	 public void sendEmail(String toEmail, String subject, String text) {
		 MimeMessage message = mailSender.createMimeMessage();
		 try {
			 MimeMessageHelper helper = new MimeMessageHelper(message, true);		//true첨부파일 허용
			 helper.setTo(toEmail);
			 helper.setSubject(subject);
			 helper.setText(text, true);
			 
			 mailSender.send(message);
		 }catch (Exception e) {
			 
			e.printStackTrace();
		}
	 }
	 */
	 
	 
	 

}
