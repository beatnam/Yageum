package com.yageum.mapper; // 실제 패키지에 맞게 수정하세요

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.SavingsPlanDTO;

import java.time.LocalDate;
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

    int updateSavingsPlan(@Param("memberIn") Integer memberIn,
            @Param("saveName") String saveName,
            @Param("saveCreatedDate") LocalDate saveCreatedDate,
            @Param("saveTargetDate") LocalDate saveTargetDate,
            @Param("saveAmount") Integer saveAmount,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth);
    
    int updateSavingsPlan2(@Param("saveIn") Integer saveIn,
            @Param("memberIn") Integer memberIn,
            @Param("saveName") String saveName,
            @Param("saveCreatedDate") LocalDate saveCreatedDate,
            @Param("saveTargetDate") LocalDate saveTargetDate,
            @Param("saveAmount") Integer saveAmount);

	int planChack(@Param("memberIn") Integer memberIn);

	void processAiFeedback(@Param("memberIn") Integer memberIn, @Param("saveFeedback") String saveFeedback);

	void processAicFeedback(@Param("memberIn") Integer memberIn, @Param("budFeedback") String budFeedback);

	Map<String, Object> getAllFeedback(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

	int countAiFeedbackByMemberInAndMonth(@Param("memberIn") Integer memberIn, @Param("year") int year, @Param("month") int month);
	
	int countBudFeedbackByMemberInAndMonth(@Param("memberIn") Integer memberIn, @Param("year") int year, @Param("month") int month);
	
    boolean hasSavingsPlanForMonth(@Param("memberIn") Integer memberIn,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth);
    
    int insertSavingsPlan(@Param("memberIn") Integer memberIn,
            @Param("saveName") String saveName,
            @Param("saveCreatedDate") LocalDate saveCreatedDate,
            @Param("saveTargetDate") LocalDate saveTargetDate,
            @Param("saveAmount") Integer saveAmount);

    Integer getSaveIn(@Param("memberIn")Integer memberIn,@Param("year") int year,@Param("month") int month);

	List<CategoryMainDTO> getAllExpenseCategories();

    List<Map<String, Object>> getIncomeCategoriesBySavingsPlanId(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

    List<Map<String, Object>> getExpenseCategoriesBySavingsPlanId(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);
    
    List<Map<String, Object>> getIncomeCategoriesForMonth(
            @Param("memberIn") Integer memberIn,
            @Param("year") int year,
            @Param("month") int month);
    
    SavingsPlanDTO findSavingsPlanByDateRange(@Param("memberIn") Integer memberIn,
            @Param("saveCreatedDate") LocalDate saveCreatedDate,
            @Param("saveTargetDate") LocalDate saveTargetDate);
    
	
}