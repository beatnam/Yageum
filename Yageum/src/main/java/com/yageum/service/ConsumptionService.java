package com.yageum.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yageum.mapper.ConsumptionMapper;
import com.yageum.mapper.ExpenseMapper;
import com.yageum.mapper.SavingsPlanMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Log
@RequiredArgsConstructor
@Slf4j
public class ConsumptionService {
    
    private final ExpenseMapper expenseMapper;
    private final SavingsPlanMapper savingsPlanMapper;
    private final ConsumptionMapper consumptionMapper;

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
    
    private static final Map<Integer, String> MAIN_CATEGORY_NAMES = new HashMap<>();
    static {
        MAIN_CATEGORY_NAMES.put(1, "식비");
        MAIN_CATEGORY_NAMES.put(2, "주거비");
        MAIN_CATEGORY_NAMES.put(3, "교통비");
        MAIN_CATEGORY_NAMES.put(4, "의료비");
        MAIN_CATEGORY_NAMES.put(5, "교육비");
        MAIN_CATEGORY_NAMES.put(6, "여가/문화");
        MAIN_CATEGORY_NAMES.put(7, "의류/미용");
        MAIN_CATEGORY_NAMES.put(8, "통신비");
        MAIN_CATEGORY_NAMES.put(9, "보험/금융");
        MAIN_CATEGORY_NAMES.put(10, "가전/가구");
        MAIN_CATEGORY_NAMES.put(11, "생활용품");
        MAIN_CATEGORY_NAMES.put(12, "사회활동/경조사");
        MAIN_CATEGORY_NAMES.put(13, "기타");
        MAIN_CATEGORY_NAMES.put(14, "수입");
        MAIN_CATEGORY_NAMES.put(18, "헬스");
    }
    private static final Map<String, Integer> SUB_TO_MAIN_CATEGORY_MAP = new HashMap<>();
    static {
        SUB_TO_MAIN_CATEGORY_MAP.put("외식", 1);
        SUB_TO_MAIN_CATEGORY_MAP.put("식료품", 1);
        SUB_TO_MAIN_CATEGORY_MAP.put("카페/디저트", 1);
        SUB_TO_MAIN_CATEGORY_MAP.put("배달음식", 1);
        SUB_TO_MAIN_CATEGORY_MAP.put("편의점", 1);

        SUB_TO_MAIN_CATEGORY_MAP.put("월세/전세", 2);
        SUB_TO_MAIN_CATEGORY_MAP.put("관리비", 2);
        SUB_TO_MAIN_CATEGORY_MAP.put("전기/수도/가스", 2);
        SUB_TO_MAIN_CATEGORY_MAP.put("인터넷/TV", 2);

        SUB_TO_MAIN_CATEGORY_MAP.put("대중교통", 4); 
        SUB_TO_MAIN_CATEGORY_MAP.put("주유비", 3);
        SUB_TO_MAIN_CATEGORY_MAP.put("택시비", 3);

        SUB_TO_MAIN_CATEGORY_MAP.put("병원비", 4);
        SUB_TO_MAIN_CATEGORY_MAP.put("약국", 4);
        SUB_TO_MAIN_CATEGORY_MAP.put("건강보조식품", 4);
        SUB_TO_MAIN_CATEGORY_MAP.put("운동/피트니스", 4);

        SUB_TO_MAIN_CATEGORY_MAP.put("학원비", 5);
        SUB_TO_MAIN_CATEGORY_MAP.put("도서/교육자료", 5);
        SUB_TO_MAIN_CATEGORY_MAP.put("온라인 강의", 5);

        SUB_TO_MAIN_CATEGORY_MAP.put("문화생활", 6);
        SUB_TO_MAIN_CATEGORY_MAP.put("여행", 6);
        SUB_TO_MAIN_CATEGORY_MAP.put("영화/공연", 6);
        SUB_TO_MAIN_CATEGORY_MAP.put("게임/취미", 6);

        SUB_TO_MAIN_CATEGORY_MAP.put("미용실/네일", 7);
        SUB_TO_MAIN_CATEGORY_MAP.put("화장품", 7);

        SUB_TO_MAIN_CATEGORY_MAP.put("통신비", 8);

        SUB_TO_MAIN_CATEGORY_MAP.put("보험료", 9);
        SUB_TO_MAIN_CATEGORY_MAP.put("저축/투자", 9);
        SUB_TO_MAIN_CATEGORY_MAP.put("대출 상환", 9);

        SUB_TO_MAIN_CATEGORY_MAP.put("가전제품", 10);
        SUB_TO_MAIN_CATEGORY_MAP.put("가구/인테리어", 10);

        SUB_TO_MAIN_CATEGORY_MAP.put("생활용품", 11);

        SUB_TO_MAIN_CATEGORY_MAP.put("경조사비", 12);
        SUB_TO_MAIN_CATEGORY_MAP.put("기부/후원", 12);

        SUB_TO_MAIN_CATEGORY_MAP.put("기타 지출", 13);

        SUB_TO_MAIN_CATEGORY_MAP.put("급여", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("용돈", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("사업 수입", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("금융소득", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("기타 수입", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("상여금", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("환급금/보조금", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("부수입", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("아르바이트", 14);
        SUB_TO_MAIN_CATEGORY_MAP.put("중고거래 판매금", 14);

        SUB_TO_MAIN_CATEGORY_MAP.put("닭가슴살", 18);
        SUB_TO_MAIN_CATEGORY_MAP.put("단백질쉐이크", 18);
        SUB_TO_MAIN_CATEGORY_MAP.put("헬스장이용비", 18);
        SUB_TO_MAIN_CATEGORY_MAP.put("보충제", 18);
        SUB_TO_MAIN_CATEGORY_MAP.put("헬스용복압벨트", 18);
    }
    
    public String analyzeCategoryExpenses(List<Map<String, Object>> categoryExpenses) {
        StringBuilder insightsText = new StringBuilder();

        if (categoryExpenses == null || categoryExpenses.isEmpty()) {
            return "데이터가 없어 소비 분석을 수행할 수 없습니다.";
        }

        // 총 지출 계산
        int totalExpense = categoryExpenses.stream()
                                         .mapToInt(itemObj -> {
                                            Map<String, Object> item = (Map<String, Object>)itemObj;
                                            Object amountObj = item.get("amount");
                                            if (amountObj instanceof Number) {
                                                return ((Number) amountObj).intValue();
                                            }
                                            log.info("Amount value is not a Number type for category (in totalExpense calculation): {}" + item.get("categoryName"));
                                            return 0;
                                         })
                                         .sum();

        List<Map<String, Object>> subCategoryDetailsSorted = new java.util.ArrayList<>(categoryExpenses);
        subCategoryDetailsSorted.sort(Comparator.comparingInt(itemObj -> {
            Map<String, Object> item = (Map<String, Object>)itemObj;
            Object amountObj = item.get("amount");
            if (amountObj instanceof Number) {
                return ((Number) amountObj).intValue();
            }
            log.info("Amount value is not a Number type for category (in sorting): {}" + item.get("categoryName"));
            return 0;
        }).reversed());


        // 2. 주 카테고리별 지출 집계
        Map<String, Integer> mainCategoryAggregatedExpenses = new LinkedHashMap<>();
        for (Map<String, Object> item : categoryExpenses) {
            String subCategoryName = (String) item.get("categoryName");
            Object amountObj = item.get("amount");
            int amount = 0;
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            } else {
                log.info("Amount value is not a Number type for category (in main category aggregation): {}" + subCategoryName);
            }
            
            Integer mainCmIn = SUB_TO_MAIN_CATEGORY_MAP.get(subCategoryName);
            if (mainCmIn != null) {
                String mainCategoryName = MAIN_CATEGORY_NAMES.get(mainCmIn);
                if (mainCategoryName != null) {
                    mainCategoryAggregatedExpenses.merge(mainCategoryName, amount, Integer::sum);
                }
            } else {
                log.info("Unknown sub-category '{}' found. It will not be aggregated into main categories." + subCategoryName);
            }
        }
        
        // 주 카테고리별 지출을 금액 기준으로 내림차순 정렬
        List<Map.Entry<String, Integer>> sortedMainCategoryExpenses = mainCategoryAggregatedExpenses.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());


        // --- 인사이트 생성 시작 ---
        insightsText.append("### 💰 소비 분석 주요 인사이트 (총 지출: ₩").append(String.format("%,d", totalExpense)).append("원)\n");
        insightsText.append("---\n");

        // 1. 서브 카테고리별 지출 현황 (상위 5개 또는 전부)
        insightsText.append("#### 📊 상세 카테고리별 지출 현황:\n");
        int count = 0;
        for (Map<String, Object> item : subCategoryDetailsSorted) {
            String subCategoryName = (String) item.get("categoryName");
            Object amountObj = item.get("amount");
            int amount = 0;
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            } else {
                 log.info("Amount value is not a Number type for category (in sub-category display): {}" + subCategoryName);
            }
            double percentage = (totalExpense > 0) ? (double) amount / totalExpense * 100 : 0;
            insightsText.append(String.format("- **%s**: ₩%,d원 (%.2f%%)\n", subCategoryName, amount, percentage));
            count++;
            if (count >= 5 && subCategoryDetailsSorted.size() > 5) {
            }
        }
        insightsText.append("\n---\n");

        // 2. 주 카테고리별 지출 현황
        insightsText.append("#### 📈 주요 카테고리별 지출 현황:\n");
        for (Map.Entry<String, Integer> entry : sortedMainCategoryExpenses) {
            String mainCategoryName = entry.getKey();
            int mainAmount = entry.getValue();
            double mainPercentage = (totalExpense > 0) ? (double) mainAmount / totalExpense * 100 : 0;
            insightsText.append(String.format("- **%s**: ₩%,d원 (%.2f%%)\n", mainCategoryName, mainAmount, mainPercentage));
        }
        insightsText.append("\n---\n");


        insightsText.append("#### 💡 핵심 인사이트:\n\n");

        // 1. 최대 지출 주 카테고리
        if (!sortedMainCategoryExpenses.isEmpty()) {
            Map.Entry<String, Integer> topMainCategory = sortedMainCategoryExpenses.get(0);
            double topMainPercentage = (totalExpense > 0) ? (double) topMainCategory.getValue() / totalExpense * 100 : 0;
            insightsText.append(String.format("1. **가장 큰 지출 영역**: '%s' 카테고리에서 ₩%,d원 (총 지출의 %.2f%%)으로 가장 많은 지출이 발생했습니다. 이 주 카테고리 내의 세부 항목들을 집중적으로 검토하여 불필요한 소비를 줄일 수 있는지 파악하는 것이 중요합니다.\n\n", topMainCategory.getKey(), topMainCategory.getValue(), topMainPercentage));
        }
        
        // 2. 상위 서브 카테고리 지출
        if (!subCategoryDetailsSorted.isEmpty()) {
            Map<String, Object> topSubCategory = subCategoryDetailsSorted.get(0);
            String topSubCategoryName = (String) topSubCategory.get("categoryName");
            // 추가: amount도 안전하게 처리
            Object topSubCategoryAmountObj = topSubCategory.get("amount");
            int topSubCategoryAmount = 0;
            if (topSubCategoryAmountObj instanceof Number) {
                topSubCategoryAmount = ((Number) topSubCategoryAmountObj).intValue();
            } else {
                 log.info("Amount value is not a Number type for top sub-category: {}" + topSubCategoryName);
            }

            double topSubCategoryPercentage = (totalExpense > 0) ? (double) topSubCategoryAmount / totalExpense * 100 : 0;
            insightsText.append(String.format("2. **주목할 만한 세부 지출**: 개별 항목 중 특히 **'%s'** 지출이 ₩%,d원 (%.2f%%)으로 가장 높습니다. 이는 특정 항목에 대한 소비가 많음을 의미하므로, 이 지출의 성격(필수/선택, 일회성/반복성)을 파악하여 관리 계획을 세우는 것이 좋습니다.\n\n", topSubCategoryName, topSubCategoryAmount, topSubCategoryPercentage));
        }

        // 3. 절약 가능성이 높은 주 카테고리 (식비 중 '외식', '카페/디저트', '배달음식' 등)
        List<String> discretionaryMainCategories = List.of("식비", "문화/여가", "의류/미용", "생활용품"); // 재량 지출이 많은 주 카테고리
        
        List<Map.Entry<String, Integer>> highPotentialSavingsMainCategories = sortedMainCategoryExpenses.stream()
                .filter(entry -> discretionaryMainCategories.contains(entry.getKey()) && entry.getValue() > 0)
                .collect(Collectors.toList());

        if (!highPotentialSavingsMainCategories.isEmpty()) {
            insightsText.append("3. **절약 목표 설정 권장**: 다음 주 카테고리들은 지출 조정을 통해 절약 효과를 기대할 수 있습니다:\n");
            for (Map.Entry<String, Integer> entry : highPotentialSavingsMainCategories) {
                insightsText.append(String.format("   - **%s**: ₩%,d원 지출. 이 영역의 세부 항목(예: 외식, 카페/디저트, 의류 구매, 문화생활 빈도 등)을 재조정하여 예산을 효율적으로 관리할 수 있습니다.\n", entry.getKey(), entry.getValue()));
            }
            insightsText.append("\n");
        }
        
        // 4. 고정/필수 지출 관리 조언 (주거/통신, 교통, 금융 등)
        List<String> fixedEssentialMainCategories = List.of("주거/통신", "교통", "건강/의료", "금융");
        List<Map.Entry<String, Integer>> fixedCategories = sortedMainCategoryExpenses.stream()
                .filter(entry -> fixedEssentialMainCategories.contains(entry.getKey()) && entry.getValue() > 0)
                .collect(Collectors.toList());

        if (!fixedCategories.isEmpty()) {
            insightsText.append("4. **고정/필수 지출 관리**: '주거/통신', '교통', '건강/의료', '금융'과 같은 필수 지출 카테고리에는 월간 고정 지출이 발생합니다. 이 부분은 급격한 절약보다는 장기적인 관점에서 더 나은 조건(예: 통신 요금제 변경, 대출 금리 비교)을 찾아 비용 효율을 높이는 방안을 모색하는 것이 효과적입니다.\n\n");
        }


        insightsText.append("---\n");
        insightsText.append("이 분석을 통해 소비 패턴을 더 잘 이해하고, 현명한 재정 계획을 세우는 데 도움이 되기를 바랍니다.");

        return insightsText.toString();
    }

	public Map<String, Object> getAllFeedback(Integer memberIn, int month, int year) {
		log.info("ConsumptionService getAllFeedback()");
		
		return savingsPlanMapper.getAllFeedback(memberIn, month, year);
	}

    // 이미 AI 피드백이 존재하는지 확인하는 메소드 구현
	public boolean hasExistingFeedback(Integer memberIn, int year, int monthValue) {
		log.info("ConsumptionService hasExistingFeedback()");
		int count = savingsPlanMapper.countAiFeedbackByMemberInAndMonth(memberIn, year, monthValue);
		return count > 0;
	}
	
	public boolean hasExistingBudFeedback(Integer memberIn) {
	    log.info("ConsumptionService hasExistingBudFeedback() 호출: memberIn={}" + memberIn);
	    LocalDate today = LocalDate.now();
	    int year = today.getYear();
	    int month = today.getMonthValue();
	    int count = savingsPlanMapper.countBudFeedbackByMemberInAndMonth(memberIn, year, month);
	    return count > 0;
	}
	
    // 이 메소드는 특정 월의 예산 계획이 존재하는지 확인합니다.
    public boolean hasSavingsPlanForMonth(Integer memberIn, LocalDate startOfMonth, LocalDate endOfMonth) {
        log.info("ConsumptionService hasSavingsPlanForMonth() 호출: memberIn={}, startOfMonth={}, endOfMonth={}" + memberIn + startOfMonth + endOfMonth);
        return savingsPlanMapper.hasSavingsPlanForMonth(memberIn, startOfMonth, endOfMonth);
    }

    // 이 메소드는 현재 월의 예산 계획이 존재하는지 확인합니다.
    public boolean hasSavingsPlanForCurrentMonth(Integer memberIn) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        return savingsPlanMapper.hasSavingsPlanForMonth(memberIn, startOfMonth, endOfMonth);
    }
    
    // 저축 계획 저장 또는 업데이트 (프론트에서 넘어온 날짜 사용)
    public boolean saveOrUpdateSavingsPlan(Integer memberIn, Map<String, Object> budgetData) {
        String saveName = (String) budgetData.get("saveName");
        Integer saveAmount = (Integer) budgetData.get("saveAmount");

        LocalDate saveCreatedDate = LocalDate.parse((String) budgetData.get("saveCreatedDate"));
        LocalDate saveTargetDate = LocalDate.parse((String) budgetData.get("saveTargetDate"));

        boolean exists = savingsPlanMapper.hasSavingsPlanForMonth(memberIn, saveCreatedDate, saveTargetDate);

        int result;
        if (exists) {
            result = savingsPlanMapper.updateSavingsPlan(
                    memberIn, saveName, saveCreatedDate, saveTargetDate, saveAmount,
                    saveCreatedDate, saveTargetDate
            );
        } else {
            result = savingsPlanMapper.insertSavingsPlan(
                    memberIn, saveName, saveCreatedDate, saveTargetDate, saveAmount
            );
        }
        return result > 0;
    }
    
    public Optional<Map<String, Object>> getConsumptionByMemberAndMonth(int memberIn, LocalDate conMonth) {
        return consumptionMapper.findConsumptionByMemberAndMonth(memberIn, conMonth);
    }

    @Transactional
    public void saveNewOrUpdateAiFeedback(int memberIn, Integer saveIn, LocalDate conMonth, String feedbackContent, int totalExpense) {
        Optional<Map<String, Object>> existingConsumptionOpt = consumptionMapper.findConsumptionByMemberAndMonth(memberIn, conMonth);

        if (existingConsumptionOpt.isPresent()) {
            consumptionMapper.updateAiFeedback(memberIn, conMonth, feedbackContent);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("memberIn", memberIn);
            params.put("conMonth", conMonth);
            params.put("saveIn", saveIn);
            params.put("feedbackContent", feedbackContent);
            params.put("conTotal", totalExpense);

            consumptionMapper.insertConsumption(params);
        }
    }

	public Integer getSaveIn(Integer memberIn, int year, int month) {
		
		return savingsPlanMapper.getSaveIn(memberIn, year, month);
	}
	
    public List<Map<String, Object>> getConsumptionFeedbacksByMemberIn(Integer memberIn) {
        log.info("ConsumptionService getConsumptionFeedbacksByMemberIn() 호출: memberIn={}"+ memberIn);

        if (memberIn == null) {
            log.info("getConsumptionFeedbacksByMemberIn: memberIn이 null입니다. 빈 리스트 반환.");
            return Collections.emptyList();
        }

        List<Map<String, Object>> feedbacks = consumptionMapper.getConsumptionFeedbacksByMemberIn(memberIn);

        if (feedbacks == null || feedbacks.isEmpty()) {
            log.info("memberIn={}에 대한 피드백 데이터가 없습니다."+ memberIn);
            return Collections.emptyList();
        }
        for (Map<String, Object> feedback : feedbacks) {
            Object conMonthObj = feedback.get("conMonth");
            LocalDate conMonth;

            if (conMonthObj instanceof LocalDate) {
                conMonth = (LocalDate) conMonthObj;
            } else if (conMonthObj instanceof String) {
                conMonth = LocalDate.parse((String) conMonthObj, DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                conMonth = LocalDate.now();
            }

            int year = conMonth.getYear();
            int month = conMonth.getMonthValue();

            List<Map<String, Object>> monthlyExpenses = expenseMapper.getCategoryExpenseByMonth(memberIn, month, year);

            Map<String, Integer> categoriesMap = new LinkedHashMap<>();
            for (Map<String, Object> expense : monthlyExpenses) {
                String category = (String) expense.get("category_name");
                Object amountObj = expense.get("total_expense");
                int amount = 0;
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).intValue();
                }

                if (category != null) {
                    categoriesMap.put(category, categoriesMap.getOrDefault(category, 0) + amount);
                }
            }
            feedback.put("categories", categoriesMap);
        }

        feedbacks.sort(Comparator.comparing(feedback -> {
            Object conMonthObj = ((Map<String, Object>) feedback).get("conMonth");
            if (conMonthObj instanceof LocalDate) {
                return (LocalDate) conMonthObj;
            } else if (conMonthObj instanceof String) {
                 return LocalDate.parse((String) conMonthObj, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return LocalDate.MIN;
        }, Comparator.reverseOrder()));

        return feedbacks;
    }

    public boolean checkFeedbackOwnership(Integer conInId, Integer memberIn) {
        log.info("ConsumptionService checkFeedbackOwnership() 호출: conInId={}, memberIn={}"+ conInId+ memberIn);
        if (conInId == null || memberIn == null) {
            return false;
        }
        return consumptionMapper.checkFeedbackOwnership(conInId, memberIn);
    }

    @Transactional
    public boolean deleteConsumptionFeedback(Integer conInId) {
        log.info("ConsumptionService deleteConsumptionFeedback() 호출: conInId={}"+ conInId);
        if (conInId == null) {
            log.info("deleteConsumptionFeedback: conInId가 null입니다. 삭제 실패.");
            return false;
        }
        int deletedRows = consumptionMapper.deleteConsumptionFeedback(conInId);
        return deletedRows > 0;
    }
    
}