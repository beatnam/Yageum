package com.yageum.domain;


import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class NoticeDTO {

	private int noticeIn;
	private String noticeSubject;
	private String noticeContent;
	private Date noticeDate;
	
	
	
	
}
