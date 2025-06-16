package com.yageum.domain;


import com.yageum.entity.Card;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class CardDTO {
	
    private int cardIn;
    private int memberIn;
    private String cardNum;
    private String cardMonth;
    private String cardYear;
    private String cardCvc;
    private String cardName;
    private String cardUsername;
    private int cc_in;
    private int methodIn;
    
    public CardDTO(Card card) {
        this.cardIn = card.getCardIn();
        this.memberIn = card.getMemberIn();
        this.cardNum = card.getCardNum();
        this.cardMonth = card.getCardMonth();
        this.cardYear = card.getCardYear();
        this.cardCvc = card.getCardCvc();
        this.cardName = card.getCardName();
        this.cardUsername = card.getCardUsername();
        this.cc_in = card.getCcIn();
        this.methodIn = card.getMethodIn();
    }

}
