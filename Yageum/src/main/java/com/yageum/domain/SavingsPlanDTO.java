package com.yageum.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class SavingsPlanDTO {
	
    private int saveIn;
    private int memberId;
    private String saveName;
    private LocalDate saveCreatedDate;
    private LocalDate saveTargetDate;
    private int saveAmount;
    private String saveFeedback;
    private String budFeedback;

}
