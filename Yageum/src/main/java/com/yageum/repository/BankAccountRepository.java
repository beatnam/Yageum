package com.yageum.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yageum.entity.BankAccount;
import com.yageum.entity.Card;




public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
	
	@Query("SELECT c FROM BankAccount c WHERE c.memberIn = :memberIn")
	List<BankAccount> findByMemberIn(@Param("memberIn") int memberIn);
	
}
