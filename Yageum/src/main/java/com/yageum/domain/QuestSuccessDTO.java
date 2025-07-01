package com.yageum.domain;

import java.time.LocalDate;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestSuccessDTO {

	private int memberIn;
	private int questIn;
	private int rewardValue;
	private LocalDate qsSuccessDate;
}
