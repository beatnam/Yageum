package com.yageum.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table(name = "expense")
@ToString
@Setter
@Getter
@Entity
public class Expense {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "expense_in")
    private int expenseIn;

    @Column(name = "member_in")
    private int memberIn;

    @Column(name = "card_in")
    private int cardIn;
    
    @Column(name = "account_in")
    private int accountIn;
    
    @Column(name = "expense_sum")
    private int expenseSum;

    @Column(name = "cs_in")
    private int csIn;

    @Column(name = "expense_memo")
    private String expenseMemo;
    
    @Column(name = "expense_date")
    private LocalDate expenseDate;
    
    @Column(name = "expense_content")
    private String expenseContent;
    
    @Column(name = "expense_type")
    private boolean expenseType; 
    
    @Column(name = "method_in")
    private int methodIn;

}
