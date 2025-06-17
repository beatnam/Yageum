package com.yageum.repository;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yageum.entity.Expense;



public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
	
	@Query("SELECT e FROM Expense e WHERE e.memberIn = :memberIn AND e.expenseDate = :date ORDER BY e.expenseDate DESC")
	List<Expense> findByMemberInAndDate(@Param("memberIn") int memberIn, @Param("date") LocalDate date);

	@Query("SELECT COALESCE(SUM(e.expenseSum), 0) FROM Expense e WHERE e.memberIn = :memberIn AND e.expenseDate = :date AND e.expenseType = :expenseType")
	int sumExpenseByDateAndType(@Param("memberIn") int memberIn, @Param("date") LocalDate date, @Param("expenseType") boolean expenseType);
}
