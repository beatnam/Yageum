package com.yageum.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ExpenseMapper {

    // 새로운 메서드: member_id(String)를 사용하여 member_in(int) 조회
    Integer getMemberInByMemberId(@Param("memberId") String memberId); // Integer 반환 타입으로 null 처리 가능

    // 이번 달 카테고리별 소비 내역 조회
    List<Map<String, Object>> getCategoryExpenseByMemberId(@Param("memberIn") int memberIn);

    // 지난달 카테고리별 소비 내역 조회 (ConsumptionService에서 분석에 사용)
    List<Map<String, Object>> getExpenseByLastMonth(@Param("memberIn") int memberIn);

    // 특정 월 카테고리별 소비 내역 조회
    List<Map<String, Object>> getCategoryExpenseByMonth(@Param("memberIn") int memberIn, @Param("month") int month, @Param("year") int year);

    // 특정 월의 총 지출을 조회하는 메소드
    int getTotalExpenseForMonth(@Param("memberIn") Integer memberIn, @Param("month") int month, @Param("year") int year);

    // ExpenseMapper.java
    List<Map<String, Object>> getLastMonthExpenseAnalysis(@Param("memberIn") int memberIn);
}