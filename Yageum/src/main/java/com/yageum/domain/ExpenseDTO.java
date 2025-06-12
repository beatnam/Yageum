package com.yageum.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ExpenseDTO {
	
    private int expenseIn;
    private int memberIn;
    private int cardIn;
    private int accountIn;
    private int expenseSum;
    private int csIn;
    private String expenseMemo;
    private LocalDateTime expenseDate;
    private String expenseContent;
    private boolean expenseType;
    private int methodIn;

}
