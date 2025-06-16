package com.yageum.domain;

import java.time.LocalDate;

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
    private LocalDate expenseDate;
    private String expenseContent;
    private boolean expenseType;
    private int methodIn;

}
