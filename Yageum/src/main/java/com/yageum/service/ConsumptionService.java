package com.yageum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.yageum.mapper.ExpenseMapper;
import com.yageum.mapper.SavingsPlanMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class ConsumptionService {
    
    private final ExpenseMapper expenseMapper;
    private final SavingsPlanMapper savingsPlanMapper;

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
    
    // 기존 getCategoryExpenseByMemberId (efeedback에서 사용)
    public List<Map<String, Object>> getCategoryExpenseByMemberId(Integer memberIn) {
        return expenseMapper.getCategoryExpenseByMemberId(memberIn);
    }

    // 특정 월의 카테고리별 지출 (canalysis에서 사용)
    public List<Map<String, Object>> getCategoryExpenseByMonth(Integer memberIn, int month, int year) {
        return expenseMapper.getCategoryExpenseByMonth(memberIn, month, year);
    }

    // ⭐ SavingsPlan 테이블 관련 메서드 (수정됨) ⭐
    public Map<String, Object> getLatestSavingsPlanByMemberIn(Integer memberIn) {
        // 기존의 절약 목표 정보 (save_name, save_amount 등)를 가져오는 로직
        Map<String, Object> savingsPlan = savingsPlanMapper.findLatestSavingsPlanByMemberIn(memberIn); 
        // (여기서 'consumptionMapper.findLatestSavingsPlanByMemberIn'는 예시입니다.
        //  실제 서비스 로직에 맞게 데이터베이스에서 데이터를 가져오는 코드를 사용하세요.)

        if (savingsPlan != null) {
            // ====================================================================
            // *** 여기에 '현재 저축액(currentSavings)'을 가져오는 로직을 추가합니다. ***

            // '현재 저축액'을 가져오는 방법은 데이터베이스 스키마 및 비즈니스 로직에 따라 다릅니다.
            // 아래는 몇 가지 예시입니다. 실제 상황에 맞는 코드를 사용하세요.

            // 예시 1: 'savings_plan' 테이블에 'current_saved_amount'와 같은 컬럼이 직접 있는 경우
            // (만약 매퍼에서 이미 이 컬럼을 가져오고 있다면, 키 이름을 "currentSavings"로 통일시키세요.)
            // Long currentSavedAmountFromDB = (Long) savingsPlan.get("current_saved_amount"); // DB 컬럼 이름
            // if (currentSavedAmountFromDB == null) {
            //     currentSavedAmountFromDB = 0L;
            // }
            // savingsPlan.put("currentSavings", currentSavedAmountFromDB);


            // 예시 2: '현재 저축액'이 다른 테이블(예: 거래 내역)에서 '저축' 유형의 합계로 계산되어야 하는 경우
            // (이 경우, 'consumptionMapper'에 '현재 저축액'을 조회하는 새로운 메서드가 필요합니다.)
            // 예: public Integer calculateTotalCurrentSavings(Integer memberIn);
            Integer currentSavings = savingsPlanMapper.calculateTotalCurrentSavings(memberIn); // 가상의 매퍼 메서드 호출
            if (currentSavings == null) {
                currentSavings = 0; // 값이 없으면 0으로 초기화
            }
            savingsPlan.put("currentSavings", currentSavings); // "currentSavings" 키로 맵에 추가

            // 예시 3: 당장 실제 데이터가 없다면 임시로 0을 넣어줄 수 있습니다. (하지만 이는 임시 방편입니다.)
            // savingsPlan.put("currentSavings", 0);
            
            // ====================================================================
        } else {
            // savingsPlan이 null인 경우에도 빈 맵을 반환하여 NullPointerException 방지
            savingsPlan = new HashMap<>();
            savingsPlan.put("currentSavings", 0); // 기본값 0 설정
        }
        return savingsPlan;
    }

    public List<Map<String, Object>> getAllSavingsPlans(Integer memberIn) {
        return savingsPlanMapper.getAllSavingsPlansByMemberIn(memberIn);
    }

    // 이번 달 총 지출 (expense_analysis에서 사용될 수 있음)
    public int getTotalExpenseForCurrentMonth(Integer memberIn) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        return expenseMapper.getTotalExpenseForMonth(memberIn, month, year);
    }

    public List<Map<String, Object>> getMonthlyExpensesForCurrentYear(Integer memberIn) {
        List<Map<String, Object>> monthlyExpenses = new ArrayList<>();
        int currentYear = LocalDate.now().getYear(); // 현재 연도 (예: 2025)

        for (int month = 1; month <= 12; month++) { // 1월부터 12월까지 반복
            // 매퍼를 호출하여 해당 월의 지출 합계 가져오기
            // member_in과 현재 연도, 그리고 1월부터 12월까지의 월을 전달
            int totalMonthlyExpense = expenseMapper.getTotalExpenseForMonth(memberIn, month, currentYear);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("year", currentYear); // 연도는 현재 연도로 고정
            monthData.put("totalExpense", totalMonthlyExpense);
            monthlyExpenses.add(monthData);
        }
        return monthlyExpenses;
    }
}