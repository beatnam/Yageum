package com.yageum.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class EmailDTO {

	 private String to;      // 수신자 이메일 주소
	 private String subject; // 이메일 제목
	 private String content; // 이메일 본문
	
	
}
