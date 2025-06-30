package com.yageum.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "bank_account")
@Getter 
@Setter
@ToString
public class BankAccount {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_in")
    private int accountIn;

    @Column(name = "member_in", nullable = false)
    private int memberIn;

    @Column(name = "bank_in")
    private int bankIn;
    
    @ManyToOne
    @JoinColumn(name = "bank_in", insertable = false, updatable = false)
    private Bank bank;

    @Column(name = "account_num", nullable = false, unique = true, length = 14)
    private String accountNum;

    @Column(name = "finnum", length = 255)
    private String finnum;

    @Column(name = "account_name", length = 50)
    private String accountName;

    @Column(name = "create_date")
    private LocalDate createDate;
    
    @Column(name = "account_alias")
    private String accountAlias;
    
//    public String getAccountNumMasked() {
//        if (accountNum != null && accountNum.length() >= 4) {
//            return "****-****-****-" + accountNum.substring(accountNum.length() - 4);
//        }
//        return accountNum;
//    }
}