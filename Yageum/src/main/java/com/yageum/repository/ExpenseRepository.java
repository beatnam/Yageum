package com.yageum.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.yageum.entity.Expense;



public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
}
