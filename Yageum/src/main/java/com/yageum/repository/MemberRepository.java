package com.yageum.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.yageum.entity.Member;


public interface MemberRepository extends JpaRepository<Member, Integer>{
	
	Member findByMemberId(String memberId);
	
	void deleteByMemberId(String memberId);


	
	
}
