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
    private int memberId;

    @Column(name = "bank_in")
    private int bankIn;

    @Column(name = "account_num", nullable = false, unique = true, length = 14)
    private String accountNum;

    @Column(name = "finnum", length = 255)
    private String finnum;

    @Column(name = "account_alias", length = 50)
    private String accountAlias;

    @Column(name = "create_date")
    private LocalDate createDate;
}