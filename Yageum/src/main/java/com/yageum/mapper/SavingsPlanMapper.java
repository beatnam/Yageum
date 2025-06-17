package com.yageum.mapper; // 실제 패키지에 맞게 수정하세요

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yageum.domain.SavingsPlanDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface SavingsPlanMapper {
    // 가장 최신 절약 목표 정보 가져오기
    Map<String, Object> findLatestSavingsPlanByMemberIn(Integer memberIn);
    
    Map<String, Object> findSavingsPlanByMonthAndYear(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

    // 이 메서드는 특정 월까지의 총 지출/수입 차액을 계산합니다.
    Integer calculateBalanceUpToMonth(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

    // ⭐ 추가: 특정 월의 예산 설정 금액 가져오기 (이번 달 예산에 사용)
    Integer getBudgetForMonth(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

    // ⭐ 기존 budgetLastMons 메서드 (지난 달 예산에 사용)
    Integer budgetLastMons(Integer memberIn);

    // 멤버의 모든 절약 목표 가져오기
    List<Map<String, Object>> getAllSavingsPlansByMemberIn(Integer memberIn);

	void updateMonthlyIncome(SavingsPlanDTO savingsPlanDTO);

	int planChack(@Param("memberIn") Integer memberIn);

	void processAiFeedback(@Param("memberIn") Integer memberIn, @Param("saveFeedback") String saveFeedback);

	void processAicFeedback(@Param("memberIn") Integer memberIn, @Param("budFeedback") String budFeedback);
	
}