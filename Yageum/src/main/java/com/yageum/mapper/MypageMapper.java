package com.yageum.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yageum.entity.Card;


@Mapper
@Repository
public interface MypageMapper {

	void deleteAccountById(int accountIn);

	void deleteCardById(int cardIn);

	void minsertPro(Card card);




    
    
    
}