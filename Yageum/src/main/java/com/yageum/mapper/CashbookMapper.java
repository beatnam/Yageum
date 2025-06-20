package com.yageum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ExpenseDTO;

@Mapper
@Repository
public interface CashbookMapper {

   ExpenseDTO getExpenseDetailById(@Param("id") int id);

   void updateExpense(ExpenseDTO expenseDTO);

    
    
    
    
}