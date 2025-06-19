package com.yageum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ExpenseDTO;

@Mapper
@Repository
public interface CashbookMapper {

	ExpenseDTO selectExpenseDetail(int id); 

   
    
    
    
    
}