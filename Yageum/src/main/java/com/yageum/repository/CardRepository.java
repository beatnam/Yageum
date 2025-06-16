package com.yageum.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yageum.entity.Card;





public interface CardRepository extends JpaRepository<Card, Integer> {

	@Query("SELECT c FROM Card c WHERE c.memberIn = :memberIn")
	List<Card> findByMemberIn(@Param("memberIn") int memberIn);
	
	@Query("SELECT c FROM Card c WHERE c.memberIn = :memberIn AND c.methodIn = :methodIn")
	List<Card> findCardsByMemberInAndMethodIn(@Param("memberIn") int memberIn, @Param("methodIn") int methodIn);
}
