package com.yageum.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yageum.entity.Bank;
import com.yageum.entity.BankAccount;

@Mapper
@Repository
public interface OpenbankingMapper {

	Bank findByName(String bankName);

	void insertBank(Bank bank);

	void insertBankAccount(BankAccount account);

  
    
    
    
}