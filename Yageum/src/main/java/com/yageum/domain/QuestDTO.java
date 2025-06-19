package com.yageum.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestDTO {

	private int questIn;
	private int questTypeIn;
	private String questName;
	private Integer cmIn;
	private Integer csIn;
	private int goalValue;
	private int rewardValue;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endDate;
	private boolean isValid;

}
