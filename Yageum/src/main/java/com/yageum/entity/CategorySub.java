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
@Table(name = "category_sub")
@Getter 
@Setter
@ToString
public class CategorySub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_in")
    private int csIn;

    @Column(name = "cm_in")
    private int cmIn;

    @Column(name = "cs_name")
    private String csName;
}