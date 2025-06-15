package com.yageum.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yageum.entity.Card;





public interface CardRepository extends JpaRepository<Card, Integer> {

	List<Card> findByMemberId(int memberIn);

}
