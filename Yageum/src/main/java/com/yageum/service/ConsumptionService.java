package com.yageum.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.yageum.domain.SavingsPlanDTO;
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

    // member_id(String)를 사용하여 member_in(int) 조회
    public Integer getMemberInByMemberId(String memberId) {
        log.info("ConsumptionService getMemberInByMemberId() 호출: memberId=" + memberId);
        return expenseMapper.getMemberInByMemberId(memberId);
    }

    // 이번 달 카테고리별 소비 내역 조회
    public List<Map<String, Object>> getCategoryExpenseByMemberId(int memberIn) {
        log.info("ConsumptionService getCategoryExpenseByMemberId() 호출: memberIn=" + memberIn);
        return expenseMapper.getCategoryExpenseByMemberId(memberIn);
    }

    // 저번 달 소비 수준 분석
    public Map<String, Object> getLastMonthExpenseAnalysis(int memberIn) {
        log.info("ConsumptionService getLastMonthExpenseAnalysis() 호출: memberIn=" + memberIn);

        // 지난달 날짜 정보
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        int lastMonthYear = lastMonth.getYear();
        int lastMonthValue = lastMonth.getMonthValue();
        List<Map<String, Object>> lastMonthExpenses = expenseMapper.getCategoryExpenseByMonth(memberIn, lastMonthValue, lastMonthYear);

        // 총 소비 금액 계산
        int lastTotal = lastMonthExpenses.stream()
            .mapToInt(expense -> {
                Object totalExpenseObj = expense.get("total_expense"); 
                if (totalExpenseObj instanceof Number) {
                    return ((Number) totalExpenseObj).intValue();
                }
                return 0; 
            })
            .sum();

        // 지출 카테고리별 분석
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

        // 소비 패턴 분석
        String consumptionTrend = lastTotal > 200000
            ? "지난달 소비가 높은 수준입니다. 절약할 수 있는 방법을 고려해보세요!"
            : "지난달 소비가 적정 수준이었습니다.";

        // 맞춤형 추천 생성
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
    
    // 특정 월 카테고리별 소비 내역 조회
    public List<Map<String, Object>> getCategoryExpenseByMonth(int memberIn, int month, int year) {
        log.info("ConsumptionService getCategoryExpenseByMonth() 호출: memberIn=" + memberIn + ", month=" + month + ", year=" + year);
        return expenseMapper.getCategoryExpenseByMonth(memberIn, month, year);
    }

    //SavingsPlan 테이블 관련 메서드
    public Map<String, Object> getLatestSavingsPlanByMemberIn(Integer memberIn) {
        Map<String, Object> savingsPlan = savingsPlanMapper.findLatestSavingsPlanByMemberIn(memberIn); 

        if (savingsPlan != null) {
            Integer currentSavings = savingsPlanMapper.calculateBalanceUpToMonth(memberIn, LocalDate.now().getMonthValue(), LocalDate.now().getYear());
            if (currentSavings == null) {
                currentSavings = 0;
            }
            savingsPlan.put("currentSavings", currentSavings);
        } else {
            savingsPlan = new HashMap<>();
            savingsPlan.put("currentSavings", 0);
            savingsPlan.put("save_name", "미설정");
            savingsPlan.put("save_amount", 0);
            savingsPlan.put("save_created_date", LocalDate.of(1900, 1, 1));
            savingsPlan.put("save_target_date", LocalDate.of(1900, 1, 1));
        }
        return savingsPlan;
    }

    public List<Map<String, Object>> getAllSavingsPlans(Integer memberIn) {
        return savingsPlanMapper.getAllSavingsPlansByMemberIn(memberIn);
    }

    // 이번 달 총 지출 (expense_analysis에서 사용)
    public int getTotalExpenseForCurrentMonth(Integer memberIn) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        log.info("ConsumptionService getTotalExpenseForCurrentMonth() 호출: memberIn=" + memberIn + ", month=" + month + ", year=" + year);
        return expenseMapper.getTotalExpenseForMonth(memberIn, month, year);
    }
    
    // 이번 달 예산 설정 금액 가져오기
    public int getBudgetForCurrentMonth(Integer memberIn) {
        Integer budget = savingsPlanMapper.getBudgetForMonth(memberIn, LocalDate.now().getMonthValue(), LocalDate.now().getYear());

        if (budget == null) {
            return 0;
        }
        return budget;
    }
    
    // ⭐ 수정: 지난 달 절약 목표 정보
    public Map<String, Object> getPreviousMonthSavingsPlan(Integer memberIn) {
        log.info("ConsumptionService getPreviousMonthSavingsPlan() 호출: memberIn=" + memberIn);
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        int prevMonthValue = previousMonth.getMonthValue();
        int prevMonthYear = previousMonth.getYear();
        Map<String, Object> savingsPlan = savingsPlanMapper.findSavingsPlanByMonthAndYear(memberIn, prevMonthValue, prevMonthYear);
        if (savingsPlan != null) {
            Integer currentSavings = savingsPlanMapper.calculateBalanceUpToMonth(memberIn, prevMonthValue, prevMonthYear);
            if (currentSavings == null) {
                currentSavings = 0;
            }
            savingsPlan.put("currentSavings", currentSavings);
        }
        return savingsPlan;
    }


    public List<Map<String, Object>> getMonthlyExpensesForCurrentYear(Integer memberIn) {
        List<Map<String, Object>> monthlyExpenses = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= 12; month++) {
            int totalMonthlyExpense = expenseMapper.getTotalExpenseForMonth(memberIn, month, currentYear);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("year", currentYear);
            monthData.put("totalExpense", totalMonthlyExpense);
            monthlyExpenses.add(monthData);
        }
        return monthlyExpenses;
    }
    
    public int getPreviousMonthRemainingBudget(Integer memberIn) {
        log.info("ConsumptionService getPreviousMonthRemainingBudget() 호출: memberIn=" + memberIn);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        int lastMonthYear = lastMonth.getYear();
        int lastMonthValue = lastMonth.getMonthValue();
        int totalExpenseLastMonth = expenseMapper.getTotalExpenseForMonth(memberIn, lastMonthValue, lastMonthYear);
        Integer budgetLastMonthFromMapper = savingsPlanMapper.budgetLastMons(memberIn);
        int budgetLastMonth = (budgetLastMonthFromMapper != null) ? budgetLastMonthFromMapper : 0;
        log.info("지난달 예산 (처리 후): " + budgetLastMonth);
        log.info("지난달 총 지출: " + totalExpenseLastMonth);

        return budgetLastMonth - totalExpenseLastMonth;
    }
    
	public int budgetLastMons(Integer memberIn) {
		log.info("ConsumptionService budgetLastMons() 호출: memberIn=" + memberIn);
		return savingsPlanMapper.budgetLastMons(memberIn);
	}
	
	public void updateMonthlyIncome(SavingsPlanDTO savingsPlanDTO) {
		log.info("ConsumptionService updateMonthlyIncome() 호출: savingsPlanDTO=" + savingsPlanDTO);
		
		savingsPlanMapper.updateMonthlyIncome(savingsPlanDTO);
	}

	public int planChack(Integer memberIn) {
		log.info("ConsumptionService updateMonthlyIncome()");
		return savingsPlanMapper.planChack(memberIn);
	}

    public double getPreviousMonthBudgetUsageProgress(Integer memberIn) {
        log.info("ConsumptionService getPreviousMonthBudgetUsageProgress() 호출: memberIn=" + memberIn);

        // 1. 지난달 정보 가져오기
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        int lastMonthYear = lastMonth.getYear();
        int lastMonthValue = lastMonth.getMonthValue();
        int totalExpenseLastMonth = expenseMapper.getTotalExpenseForMonth(memberIn, lastMonthValue, lastMonthYear);
        Integer budgetLastMonthFromMapper = savingsPlanMapper.budgetLastMons(memberIn);
        int budgetLastMonth = (budgetLastMonthFromMapper != null) ? budgetLastMonthFromMapper : 0;

        // 4. 예산 사용률 계산
        double previousMonthBudgetUsageProgress = 0.0;
        if (budgetLastMonth > 0) {
            previousMonthBudgetUsageProgress = (totalExpenseLastMonth * 100.0) / budgetLastMonth;
        }
        return Math.max(0.0, previousMonthBudgetUsageProgress);
    }

	public void processAiFeedback(Integer memberIn, String aiFeedback) {
		log.info("ConsumptionService processAiFeedback()");
		
		savingsPlanMapper.processAiFeedback(memberIn, aiFeedback);
	}

	public void processAicFeedback(Integer memberIn, String aiFeedback) {
		log.info("ConsumptionService processAicFeedback()");
		
		savingsPlanMapper.processAicFeedback(memberIn, aiFeedback);
	}

	public int thisMonthCount(Integer memberIn) {
		log.info("ConsumptionService thisMonthCount()");
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        
		return expenseMapper.thisMonthCount(memberIn, month, year);
	}

	public List<Map<String, Object>> getCategoryExpensesForChart(Integer memberIn, int month, int year) {
		log.info("ConsumptionService getCategoryExpensesForChart()");
		
		return expenseMapper.getCategoryExpensesData(memberIn, month, year);
	}
    
    
}