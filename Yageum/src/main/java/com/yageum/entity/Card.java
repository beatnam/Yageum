package com.yageum.entity;

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
@Table(name = "card")
@Getter 
@Setter
@ToString
public class Card {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_in")
    private int cardIn;

    @Column(name = "member_in", nullable = false)
    private int memberId;

    @Column(name = "card_num", nullable = false, unique = true, length = 16)
    private String cardNum;

    @Column(name = "card_month", length = 2)
    private String cardMonth;

    @Column(name = "card_year", length = 2)
    private String cardYear;

    @Column(name = "card_cvc", length = 4)
    private String cardCvc;

    @Column(name = "card_name", nullable = false, length = 50)
    private String cardName;

    @Column(name = "card_username", length = 50)
    private String cardUsername;

    @Column(name = "cc_in")
    private int ccIn;

    @Column(name = "cm_name", length = 255)
    private String cmName;
}