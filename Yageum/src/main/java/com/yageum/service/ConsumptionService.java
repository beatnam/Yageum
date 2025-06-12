package com.yageum.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.yageum.mapper.ExpenseMapper; 
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class ConsumptionService {
    
    private final ExpenseMapper expenseMapper;

    // 추가된 메서드: member_id(String)를 사용하여 member_in(int) 조회
    public Integer getMemberInByMemberId(String memberId) {
        log.info("ConsumptionService getMemberInByMemberId() 호출: memberId=" + memberId);
        return expenseMapper.getMemberInByMemberId(memberId);
    }

    // 특정 회원의 이번 달 카테고리별 소비 내역 조회 (파라미터 변경: memberIn)
    public List<Map<String, Object>> getCategoryExpenseByMemberId(int memberIn) { // 파라미터 int memberIn으로 변경
        log.info("ConsumptionService getCategoryExpenseByMemberId() 호출: memberIn=" + memberIn);
        return expenseMapper.getCategoryExpenseByMemberId(memberIn); // memberIn 전달
    }

    // 저번 달 소비 수준 분석 (파라미터 변경: memberIn)
    public Map<String, Object> getLastMonthExpenseAnalysis(int memberIn) { // 파라미터 int memberIn으로 변경
        log.info("ConsumptionService getLastMonthExpenseAnalysis() 호출: memberIn=" + memberIn);

        // 저번 달 소비 내역 조회 (메서드 호출 변경: memberIn 전달)
        List<Map<String, Object>> lastMonthExpenses = expenseMapper.getExpenseByLastMonth(memberIn);

        // 총 소비 금액 계산 (total_expense 키 사용)
        int lastTotal = lastMonthExpenses.stream()
            .mapToInt(expense -> {
                Object totalExpenseObj = expense.get("total_expense"); 
                if (totalExpenseObj instanceof Number) {
                    return ((Number) totalExpenseObj).intValue();
                }
                return 0; 
            })
            .sum();

        // 지출 카테고리별 분석 (category_name, total_expense 키 사용)
        Map<String, Integer> categoryExpenses = new HashMap<>();
        for (Map<String, Object> expense : lastMonthExpenses) {
            String category = (String) expense.get("category_name"); 
            Object amountObj = expense.get("total_expense"); 
            int amount = 0;
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            }

            if (category != null) { 
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0) + amount);
            }
        }

        // 소비 패턴 분석 (기존 로직 유지)
        String consumptionTrend = lastTotal > 200000
            ? "지난달 소비가 높은 수준입니다. 절약할 수 있는 방법을 고려해보세요!"
            : "지난달 소비가 적정 수준이었습니다.";

        // 맞춤형 추천 생성 (기존 로직 유지)
        List<Map<String, String>> recommendations = List.of(
            Map.of(
                "title", "소비 조절 필요",
                "description", lastTotal > 200000
                    ? "지난달 지출이 많았습니다. 외식 비용을 줄이거나 절약 계획을 세워보세요."
                    : "지난달 소비가 안정적이었습니다! 지속적으로 절약 습관을 유지하세요."
            )
        );

        // 최종 분석 결과 반환
        Map<String, Object> feedbackSummary = new HashMap<>();
        feedbackSummary.put("lastTotal", lastTotal);
        feedbackSummary.put("categoryExpenses", categoryExpenses);
        feedbackSummary.put("consumptionTrend", consumptionTrend);
        feedbackSummary.put("recommendations", recommendations);

        return feedbackSummary;
    }
    
    // 특정 월 카테고리별 소비 내역 조회 (파라미터 변경: memberIn)
    public List<Map<String, Object>> getCategoryExpenseByMonth(int memberIn, int month, int year) { // 파라미터 int memberIn으로 변경
        log.info("ConsumptionService getCategoryExpenseByMonth() 호출: memberIn=" + memberIn + ", month=" + month + ", year=" + year);
        return expenseMapper.getCategoryExpenseByMonth(memberIn, month, year); // memberIn 전달
    }
}