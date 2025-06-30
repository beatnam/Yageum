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
@Table(name = "card_corporation")
@Getter 
@Setter
@ToString
public class CardCompany {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cc_in")
    private int ccIn;

    @Column(name = "cc_name", nullable = false)
    private String ccName;
	

}
