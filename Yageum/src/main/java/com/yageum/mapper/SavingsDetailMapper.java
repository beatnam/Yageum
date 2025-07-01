package com.yageum.mapper;

import com.yageum.domain.SavingsDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SavingsDetailMapper {
    void insertSavingsDetail(SavingsDetail savingsDetail);
    void insertSavingsDetailsBatch(List<SavingsDetail> savingsDetails);
    void deleteSavingsDetailsBySaveIn(@Param("saveIn") Integer saveIn);
}