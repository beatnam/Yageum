package com.yageum.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ExpenseDTO;
import com.yageum.entity.BankAccount;

@Mapper
@Repository
public interface CashbookMapper {

   ExpenseDTO getExpenseDetailById(@Param("id") int id);

   void updateExpense(ExpenseDTO expenseDTO);

   List<BankAccount> getAccountList(int memberIn);

   List<Map<String, Object>> getDailyIncomeExpense(@Param("memberIn") int memberIn, @Param("start") LocalDate start, @Param("end") LocalDate end);

//   List<ExpenseDTO> getMonthList(@Param("memberIn") int memberIn, @Param("start") LocalDate start, @Param("end") LocalDate end);

   List<ExpenseDTO> searchExpense(Map<String, Object> paramMap);


    
    
    
}