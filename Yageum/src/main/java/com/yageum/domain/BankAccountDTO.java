package com.yageum.domain;


import java.time.LocalDate;

import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class BankAccountDTO {
	
    private int accountIn;
    private int memberIn;
    private int bankIn;
    private String accountNum;
    private String finNum;
    private String accountName;
    private LocalDate createDate;
    
    private String bankName;
    
    public BankAccountDTO(BankAccount bankAccount) {
        this.accountIn = bankAccount.getAccountIn();
        this.bankName = bankAccount.getBank().getBankName(); // 연관관계 필요
        this.accountNum = bankAccount.getAccountNum();
    }

}
