package com.yageum.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yageum.entity.CategorySub;




public interface CategorySubRepository extends JpaRepository<CategorySub, Integer> {
	
	@Query("SELECT cs FROM CategorySub cs WHERE cs.cmIn = :cmIn")
	List<CategorySub> findByCmIn(@Param("cmIn") int cmIn);
}
