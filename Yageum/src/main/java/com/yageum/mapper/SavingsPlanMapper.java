package com.yageum.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SavingsPlanMapper {
    // getLatestSavingsByMemberIn -> getLatestSavingsPlanByMemberIn
    Map<String, Object> findLatestSavingsPlanByMemberIn(@Param("memberIn") Integer memberIn);

    // getAllSavingsGoalsByMemberIn -> getAllSavingsPlansByMemberIn
    List<Map<String, Object>> getAllSavingsPlansByMemberIn(@Param("memberIn") Integer memberIn);
    // 추가적인 저축 목표 관련 쿼리 (예: 목표 달성률 계산 등)
    Integer calculateTotalCurrentSavings(@Param("memberIn") Integer memberIn);
}