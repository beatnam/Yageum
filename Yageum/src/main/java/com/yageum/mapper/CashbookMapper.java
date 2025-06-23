package com.yageum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ExpenseDTO;
import com.yageum.entity.BankAccount;

@Mapper
@Repository
public interface CashbookMapper {

   ExpenseDTO getExpenseDetailById(@Param("id") int id);

   void updateExpense(ExpenseDTO expenseDTO);

   List<BankAccount> getAccountList(int memberIn);

    
    
    
    
}