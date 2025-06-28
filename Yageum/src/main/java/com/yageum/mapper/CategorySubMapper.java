package com.yageum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CategorySubMapper {

	void delete(int cmIn);

	
	
	
}
