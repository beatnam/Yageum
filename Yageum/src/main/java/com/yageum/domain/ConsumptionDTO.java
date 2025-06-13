package com.yageum.domain;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ConsumptionDTO {
	
    private int conIn;
    private int expenseIn;
    private int memberIn;
    private int saveIn;
    private String conResult;
    private LocalDate conMonth;
    private int conTotal;
    private int conFeedback;

}
