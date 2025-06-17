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
@Table(name = "bank")
@Getter 
@Setter
@ToString
public class Bank {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_in")
    private int bankIn;

    @Column(name = "bank_name")
    private String bankName;


}