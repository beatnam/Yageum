package com.yageum.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestStateDTO {

	private int memberIn;
	private int questIn;
	private int qpIn;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date qsSuccessDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date rewardDate;
	
}
