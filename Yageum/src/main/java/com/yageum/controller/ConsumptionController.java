package com.yageum.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.service.ConsumptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yageum.domain.CategoryMainDTO;
import com.yageum.domain.CategorySubDTO;
import com.yageum.domain.SavingsDetail;
import com.yageum.domain.SavingsPlanDTO;
import com.yageum.mapper.SavingsPlanMapper;
import com.yageum.service.ChatGPTClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/consumption/*")
public class ConsumptionController {

    private final ConsumptionService consumptionService;
    private final ChatGPTClient chatGPTClient;
    private final ObjectMapper objectMapper;
    private final SavingsPlanMapper savingsPlanMapper;

    @GetMapping("/efeedback")
    public String efeedback(Model model) {
        log.info("ConsumptionController efeedback() 호출");
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName(); // username (String)

        if (memberId == null || memberId.isEmpty()) {
            log.warning("로그인되지 않은 사용자 또는 memberId가 비어있습니다. 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login"; // 로그인되지 않은 사용자 처리
        }

        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        if (memberIn == null) {
            log.warning("member_id '" + memberId + "'에 해당하는 member_in을 찾을 수 없습니다. 빈 데이터를 반환합니다.");
            model.addAttribute("efeedbackData", new HashMap<>());
            return "consumption/expense_feedback";
        }

        List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn);
        // --- 1. 현재 월 지출 내역 로그 (이 로그의 출력이 필요합니다!) ---
        log.info("현재 월 카테고리 지출 내역 (currentMonthCategoryExpenses): {}"+","+ currentMonthCategoryExpenses);


        // --- 1. 카테고리별 예산 데이터 가져오기 시작 ---
        Map<String, Integer> categoryBudgets = consumptionService.getCategoryBudgetsByMemberIn(memberIn); // 새로운 서비스 메서드
        // --- 1. 카테고리별 예산 데이터 로그 (이 로그의 출력이 필요합니다!) ---
        log.info("카테고리별 예산 데이터 (categoryBudgets): {}"+","+ categoryBudgets);
        // --- 1. 카테고리별 예산 데이터 가져오기 끝 ---

        int totalExpense = currentMonthCategoryExpenses.stream()
            .mapToInt(expense -> {
                Object totalExpenseObj = expense.get("total_expense");
                if (totalExpenseObj instanceof Number) {
                    return ((Number) totalExpenseObj).intValue();
                }
                return 0;
            })
            .sum();

        // 소비 효율 점수 계산 로직 수정: totalExpense를 덜 민감하게 반응하도록 나눗셈 기준 조정
        int efficiencyScore = Math.max(50, Math.min(95, 100 - (totalExpense / 15000)));

        String efficiencyMessage = efficiencyScore > 80 ? "매우 효율적인 소비를 하고 계십니다!"
                                    : efficiencyScore > 60 ? "적정 수준의 소비 패턴입니다."
                                    : "소비 패턴을 조정하면 절약할 수 있습니다.";

        String feedbackText = efficiencyScore > 80 ? "우수한 소비 습관"
                            : efficiencyScore > 60 ? "평균적인 소비 패턴"
                            : "개선이 필요한 소비 패턴";

        Map<String, Object> lastMonthAnalysis = consumptionService.getLastMonthExpenseAnalysis(memberIn);

        List<Map<String, String>> processedFeedbackDetails = new ArrayList<>();

        for (Map<String, Object> expenseEntry : currentMonthCategoryExpenses) {
            Map<String, String> detail = new HashMap<>();
            String categoryName = (String) expenseEntry.get("category_name");
            int totalExpenseForCategory = 0;
            Object totalExpenseObj = expenseEntry.get("total_expense");
            if (totalExpenseObj instanceof Number) {
                totalExpenseForCategory = ((Number) totalExpenseObj).intValue();
            }

            // --- 2. 예산과 지출 비교 로직 추가 시작 ---
            int budgetForCategory = categoryBudgets.getOrDefault(categoryName, 0); // 해당 카테고리 예산 가져오기
            
            // Log.info 구문 수정 완료!
            log.info("처리 중 - 카테고리: {}, 총 지출: {}, 해당 카테고리 예산: {}"+","+ categoryName+","+ totalExpenseForCategory+","+ budgetForCategory);
            
            String icon = "💡";
            String title = categoryName != null ? categoryName + " 지출" : "기타 지출";
            String status = "정보";
            String statusClass = "info";
            String description = ""; // 초기화

            if (categoryName != null) {
                if (budgetForCategory > 0) { // 예산이 설정된 경우
                    if (totalExpenseForCategory > budgetForCategory) {
                        status = "예산 초과";
                        statusClass = "danger";
                        description = String.format("이번 달 %s 지출이 예산 %d원을 %d원 초과했습니다. (%d원 지출)",
                                            categoryName, budgetForCategory, (totalExpenseForCategory - budgetForCategory), totalExpenseForCategory);
                        icon = "⚠️";
                    } else if (totalExpenseForCategory <= budgetForCategory * 0.8) { // 예산의 80% 이하 사용
                        status = "예산 절약";
                        statusClass = "good";
                        description = String.format("이번 달 %s 지출이 예산 %d원보다 적습니다. (%d원 지출)",
                                            categoryName, budgetForCategory, totalExpenseForCategory);
                        icon = "💰";
                    } else { // 예산 범위 내
                        status = "예산 내";
                        statusClass = "info";
                        description = String.format("이번 달 %s 지출은 예산 %d원 범위 내에 있습니다. (%d원 지출)",
                                            categoryName, budgetForCategory, totalExpenseForCategory);
                        icon = "✔️";
                    }
                } else { // 예산이 설정되지 않은 경우 기존 로직 유지
                    description = categoryName + "에 총 " + totalExpenseForCategory + "원 지출했습니다.";
                    switch (categoryName) {
                        case "식비":
                            icon = "🍽️";
                            if (totalExpenseForCategory > 70000) {
                                status = "주의";
                                statusClass = "warning";
                                description = "이번 달 식비가 " + totalExpenseForCategory + "원으로 높은 편입니다. 외식을 줄여보세요.";
                            } else {
                                status = "양호";
                                statusClass = "good";
                                description = "이번 달 식비는 " + totalExpenseForCategory + "원으로 잘 관리되고 있습니다.";
                            }
                            break;
                        case "교통비":
                            icon = "🚗";
                            status = "절약";
                            statusClass = "good";
                            description = "대중교통 이용으로 교통비를 효율적으로 관리하고 있습니다. " + totalExpenseForCategory + "원 지출.";
                            break;
                        case "쇼핑":
                            icon = "🛍️";
                            if (totalExpenseForCategory > 150000) {
                                status = "개선 필요";
                                statusClass = "danger";
                                description = "이번 달 쇼핑 지출이 " + totalExpenseForCategory + "원으로 많습니다. 충동구매를 주의하세요.";
                            } else {
                                status = "양호";
                                statusClass = "good";
                                description = "이번 달 쇼핑 지출은 " + totalExpenseForCategory + "원으로 적절합니다.";
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            // --- 2. 예산과 지출 비교 로직 추가 끝 ---

            detail.put("icon", icon);
            detail.put("title", title);
            detail.put("status", status);
            detail.put("statusClass", statusClass);
            detail.put("description", description);
            processedFeedbackDetails.add(detail);
        }

        List<Map<String, Object>> progressData = new ArrayList<>(); // 리스트로 초기화
        // 여기에 카테고리별 예산 대비 지출율을 progressData에 추가할 수 있습니다.
        for (Map<String, Object> expenseEntry : currentMonthCategoryExpenses) {
            String categoryName = (String) expenseEntry.get("category_name");
            int totalExpenseForCategory = 0;
            Object totalExpenseObj = expenseEntry.get("total_expense");
            if (totalExpenseObj instanceof Number) {
                totalExpenseForCategory = ((Number) totalExpenseObj).intValue();
            }
            int budgetForCategory = categoryBudgets.getOrDefault(categoryName, 0);
            
            if (budgetForCategory > 0) {
                int progressPercentage = (int) ((double) totalExpenseForCategory / budgetForCategory * 100);
                progressData.add(Map.of("progressText", String.format("%s 지출율 %d%%", categoryName, progressPercentage),
                                        "progressPercentage", Math.min(100, progressPercentage))); // 100% 초과 방지
            }
        }


        Map<String, Object> feedbackSummary = new HashMap<>();
        feedbackSummary.put("overallScore", efficiencyScore);
        feedbackSummary.put("feedbackText", feedbackText);
        feedbackSummary.put("efficiencyMessage", efficiencyMessage);
        feedbackSummary.put("feedbackDetails", processedFeedbackDetails);
        feedbackSummary.put("recommendations", List.of(
            Map.of("title", "식비 예산 최적화", "description", efficiencyScore < 70
                   ? "외식 횟수를 줄이고, 식재료를 활용한 요리를 추천합니다."
                   : "식비 관리가 잘 되고 있습니다. 현재 수준을 유지하세요."),
            Map.of("title", "비상금 적립", "description", totalExpense > 200000
                   ? "예상보다 많은 지출이 발생하고 있으니, 비상금 적립을 추천합니다."
                   : "비상금이 안정적으로 관리되고 있습니다.")
        ));
        feedbackSummary.put("lastMonthAnalysis", lastMonthAnalysis);
        feedbackSummary.put("progress", progressData); // 업데이트된 progressData 사용
        
        // Log.info 구문 수정 완료!
        log.info("efeedbackData.progress: {}" +","+ feedbackSummary.get("progress"));
        
        
        model.addAttribute("efeedbackData", feedbackSummary);

        return "consumption/expense_feedback";
    }

    @GetMapping("/lastMonthAnalysis")
    public String lastMonthAnalysis(Model model) {
        log.info("ConsumptionController lastMonthAnalysis() 호출");
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (memberId == null || memberId.isEmpty()) {
            log.warning("로그인되지 않은 사용자 또는 memberId가 비어있습니다.");
            return "redirect:/login";
        }

        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        if (memberIn == null) {
            log.warning("member_id '" + memberId + "'에 해당하는 member_in을 찾을 수 없습니다.");
            model.addAttribute("lastMonthData", new HashMap<>());
            return "consumption/last_month_analysis";
        }

        Map<String, Object> lastMonthAnalysis = consumptionService.getLastMonthExpenseAnalysis(memberIn);
        model.addAttribute("lastMonthData", lastMonthAnalysis);

        return "consumption/last_month_analysis";
    }

    @GetMapping("/eanalysis")
    public String getExpenseAnalysis(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController getExpenseAnalysis() 호출");
        String memberId = userDetails.getUsername();

        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        if (memberIn == null) {
            log.info("Member not found for memberId: " + memberId);
            return "redirect:/error";
        }

        // 1. 이번 달 기준 데이터 (Current Month Data)

        // 이번 달 총 지출
        int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
        model.addAttribute("totalExpense", totalExpense);

        LocalDate today = LocalDate.now();
        int totalDaysInMonth = today.lengthOfMonth();
        int currentDayOfMonth = today.getDayOfMonth();
        int daysLeft = totalDaysInMonth - currentDayOfMonth;

        // 하루 평균 지출 (현재까지의 지출 / 지난 일수)
        double averageDailyExpense = (currentDayOfMonth > 0) ? (double) totalExpense / currentDayOfMonth : 0;
        model.addAttribute("averageDailyExpense", (int) Math.round(averageDailyExpense));
        model.addAttribute("daysLeft", daysLeft);

        // 이번 달 예산 설정 금액
        int currentMonthBudget = consumptionService.getBudgetForCurrentMonth(memberIn);
        int remainingBudget = currentMonthBudget - totalExpense;
        model.addAttribute("remainingBudget", remainingBudget);

        // 예산 사용률 (이번 달 기준) - 변수명 변경: currentMonthBudgetUsageProgress
        double currentMonthBudgetUsageProgress = 0.0;
        if (currentMonthBudget != 0) {
            currentMonthBudgetUsageProgress = (totalExpense * 100.0) / currentMonthBudget;
        }
        model.addAttribute("currentMonthBudgetUsageProgress", Math.max(0.0, currentMonthBudgetUsageProgress));


        // 월별 지출 추이 그래프 데이터 (현재 연도 데이터)
        List<Map<String, Object>> monthlyExpensesData = consumptionService.getMonthlyExpensesForCurrentYear(memberIn);
        if (monthlyExpensesData == null) {
            monthlyExpensesData = new ArrayList<>();
            log.info("monthlyExpensesData was null, initialized to empty list.");
        }

        List<String> monthlyExpensesLabels = new ArrayList<>();
        List<Integer> monthlyExpensesValues = new ArrayList<>();

        if (!monthlyExpensesData.isEmpty()) {
            monthlyExpensesData.forEach(entry -> {
                int monthValue = ((Number) entry.get("month")).intValue();
                int totalExpenseAmount = ((Number) entry.get("totalExpense")).intValue();

                monthlyExpensesLabels.add(monthValue + "월");
                monthlyExpensesValues.add(totalExpenseAmount);
            });
        }
        
        try {
            // ObjectMapper를 사용하여 리스트를 JSON 문자열로 변환
            String labelsJson = objectMapper.writeValueAsString(monthlyExpensesLabels);
            String valuesJson = objectMapper.writeValueAsString(monthlyExpensesValues);

            // 변환된 JSON 문자열을 HTML에서 찾는 이름으로 모델에 추가
            model.addAttribute("monthlyExpensesLabelsJson", labelsJson);
            model.addAttribute("monthlyExpensesValuesJson", valuesJson);

        } catch (Exception e) {
            log.info("월별 지출 추이 JSON 변환 중 오류 발생: " + e.getMessage() + e);
            // 오류 발생 시 빈 JSON 배열 문자열이라도 넘겨주어 클라이언트에서 파싱 오류 방지
            model.addAttribute("monthlyExpensesLabelsJson", "[]");
            model.addAttribute("monthlyExpensesValuesJson", "[]");
        }

        // 2. 지난 달 기준 데이터 (Previous Month Data)

        // 지난달 절약 목표 정보 가져오기
        Map<String, Object> previousMonthSavingsPlan = consumptionService.getPreviousMonthSavingsPlan(memberIn);

        if (previousMonthSavingsPlan != null && previousMonthSavingsPlan.isEmpty()) {
            previousMonthSavingsPlan = null;
            log.info("previousMonthSavingsPlan was an empty map, setting to null for Thymeleaf processing.");
        }

        if (previousMonthSavingsPlan != null) {
            if (previousMonthSavingsPlan.get("save_created_date") instanceof java.sql.Date) {
                previousMonthSavingsPlan.put("save_created_date", ((java.sql.Date) previousMonthSavingsPlan.get("save_created_date")).toLocalDate());
            }
            if (previousMonthSavingsPlan.get("save_target_date") instanceof java.sql.Date) {
                previousMonthSavingsPlan.put("save_target_date", ((java.sql.Date) previousMonthSavingsPlan.get("save_target_date")).toLocalDate());
            }
        }
        model.addAttribute("savingsPlan", previousMonthSavingsPlan);

        //지난달 저축가능 금액
        Integer previousMonthSavableAmount = consumptionService.getPreviousMonthRemainingBudget(memberIn);
        if (previousMonthSavableAmount == null) {
            previousMonthSavableAmount = 0;
            log.info("previousMonthSavableAmount was null, setting to 0.");
        }
        model.addAttribute("previousMonthSavableAmount", previousMonthSavableAmount);

        //지난달 예산 사용률
        double previousMonthBudgetUsageProgress = consumptionService.getPreviousMonthBudgetUsageProgress(memberIn);
        model.addAttribute("previousMonthBudgetUsageProgress", previousMonthBudgetUsageProgress);


        return "/consumption/expense_analysis";
    }

    // ChatGPT API 엔드포인트
    @GetMapping("/getChatGptFeedbackForEfeedback")
    @ResponseBody // JSON/XML 데이터를 반환
    public Map<String, String> getChatGptFeedbackForEfeedback() {
        log.info("ConsumptionController getChatGptFeedbackForEfeedback() 호출");
        Map<String, String> response = new HashMap<>();
        String chatGptResponse = "분석 결과가 없습니다."; // 기본 메시지

        try {
            String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (memberId == null || memberId.isEmpty()) {
                response.put("error", "로그인 정보가 없습니다.");
                return response;
            }

            Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
            if (memberIn == null) {
                response.put("error", "회원 정보를 찾을 수 없습니다.");
                return response;
            }

            List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn);

            // ⭐ 수정된 부분 시작 ⭐
            Map<String, Integer> aggregatedCategoryExpenses = currentMonthCategoryExpenses.stream()
                .filter(e -> e.get("category_name") != null && e.get("total_expense") instanceof Number)
                .collect(Collectors.toMap(
                    e -> (String) e.get("category_name"),
                    e -> ((Number) e.get("total_expense")).intValue(),
                    Integer::sum // 동일 카테고리 합산
                ));

            int totalExpense = aggregatedCategoryExpenses.values().stream().mapToInt(Integer::intValue).sum();
            // ⭐ 수정된 부분 끝 ⭐


            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("나는 이번달에 지출이 다음과 같아:\n");

            if (aggregatedCategoryExpenses.isEmpty()) {
                promptBuilder.append("이번 달 지출 내역이 없어.");
            } else {
                for (Map.Entry<String, Integer> entry : aggregatedCategoryExpenses.entrySet()) {
                    promptBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("원\n");
                }
                promptBuilder.append("총 지출: ").append(totalExpense).append("원\n\n");
                promptBuilder.append("이 지출 내역을 바탕으로 따뜻한 어조로 카테고리별로 분석하고, 장단점과 함께 피드백을 해줘. 그리고 전반적인 소비 패턴에 대한 의견도 줘. 답변은 최대 300글자 내로 줄바꿈을 포함하여 가독성 있게 작성해줘.\\n\\n");
            }

            String prompt = promptBuilder.toString();
            log.info("ChatGPT Prompt: " + prompt);

            chatGptResponse = chatGPTClient.askChatGpt(prompt);
            log.info("ChatGPT Response: " + chatGptResponse);

            response.put("feedback", chatGptResponse);

        } catch (Exception e) {
            log.severe("ChatGPT 피드백 생성 중 오류 발생: " + e.getMessage());
            response.put("error", "피드백을 생성하는 중 오류가 발생했습니다.");
        }
        return response;
    }
    
    @PostMapping("/getChatGptFeedbackForEanalysis")
    @ResponseBody // JSON/XML 데이터를 반환
    public Map<String, String> getChatGptFeedbackForEanalysis(@RequestBody Map<String, List<String>> requestBody) {
        log.info("ConsumptionController getChatGptFeedbackForEanalysis() 호출");
        Map<String, String> response = new HashMap<>();
        String chatGptResponse = "분석 결과가 없습니다.";

        try {
            String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (memberId == null || memberId.isEmpty()) {
                response.put("error", "로그인 정보가 없습니다.");
                return response;
            }

            Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
            if (memberIn == null) {
                response.put("error", "회원 정보를 찾을 수 없습니다.");
                return response;
            }

            // 프론트엔드에서 전달된 선택 지표 목록
            List<String> selectedMetrics = requestBody.get("selectedMetrics");
            if (selectedMetrics == null || selectedMetrics.isEmpty()) {
                selectedMetrics = new ArrayList<>();
                selectedMetrics.add("totalExpense");
                selectedMetrics.add("averageDailyExpense");
                selectedMetrics.add("daysLeft");
                selectedMetrics.add("remainingBudget");
                selectedMetrics.add("categoryExpenses");
            }

            // 필요한 재무 지표 값 계산
            int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
            
            LocalDate today = LocalDate.now();
            int totalDaysInMonth = today.lengthOfMonth();
            int currentDayOfMonth = today.getDayOfMonth();
            int daysLeft = totalDaysInMonth - currentDayOfMonth;

            double averageDailyExpense = (currentDayOfMonth > 0) ? (double) totalExpense / currentDayOfMonth : 0;
            
            int budget = consumptionService.getBudgetForCurrentMonth(memberIn);
            int remainingBudget = budget - totalExpense;


            // ChatGPT에 보낼 prompt 생성
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("너는 소비 분석 전문가야. 다음 지출 데이터를 바탕으로 따뜻하고 이해하기 쉬운 어조로 자세히 분석하고, 장단점과 개선점을 피드백해줘."
            		+ "전반적인 소비 패턴에 대한 의견과 평균 지출 대비 소비 사이즈가 어떤지에 대한 의견도 줘. 답변은 최대 300글자 내로 줄바꿈을 포함하여 가독성 있게 작성해줘.\n\n");

            // 선택된 지표에 따라 프롬프트 내용 추가
            if (selectedMetrics.contains("totalExpense")) {
                promptBuilder.append("이번 달 총 지출: ").append(totalExpense).append("원\n");
            }
            if (selectedMetrics.contains("averageDailyExpense")) {
                promptBuilder.append("하루 평균 지출: ").append((int) Math.round(averageDailyExpense)).append("원\n");
            }
            if (selectedMetrics.contains("daysLeft")) {
                promptBuilder.append("남은 일수: ").append(daysLeft).append("일\n");
            }
            if (selectedMetrics.contains("remainingBudget")) {
                promptBuilder.append("남은 예산: ").append(remainingBudget).append("원\n");
            }
            
            // 카테고리별 지출 데이터도 프롬프트에 포함
            if (selectedMetrics.contains("categoryExpenses")) {
                List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn);
                Map<String, Integer> aggregatedCategoryExpenses = currentMonthCategoryExpenses.stream()
                    .filter(e -> e.get("category_name") != null && e.get("total_expense") instanceof Number)
                    .collect(Collectors.toMap(
                        e -> (String) e.get("category_name"),
                        e -> ((Number) e.get("total_expense")).intValue(),
                        Integer::sum
                    ));

                if (!aggregatedCategoryExpenses.isEmpty()) {
                    promptBuilder.append("\n카테고리별 지출 내역:\n");
                    for (Map.Entry<String, Integer> entry : aggregatedCategoryExpenses.entrySet()) {
                        promptBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("원\n");
                    }
                } else {
                     promptBuilder.append("\n이번 달 지출 내역 (카테고리별)이 아직 없어.\n");
                }
            }


            String prompt = promptBuilder.toString();
            log.info("ChatGPT Prompt: " + prompt);

            chatGptResponse = chatGPTClient.askChatGpt(prompt);
            log.info("ChatGPT Response: " + chatGptResponse);

            response.put("feedback", chatGptResponse);

        } catch (Exception e) {
            log.severe("ChatGPT 피드백 생성 중 오류 발생: " + e.getMessage());
            response.put("error", "피드백을 생성하는 중 오류가 발생했습니다.");
        }
        return response;
    }
    
    @GetMapping("/bplanner")
    public String bplanner(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController bplanner() 호출");
        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        
        
        return "/consumption/budget_planner";
    }
    
    @GetMapping("/hasSavingsPlanForMonth")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> hasSavingsPlanForMonth(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Integer memberIn = consumptionService.getMemberInByMemberId(userDetails.getUsername());
        if (memberIn == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            LocalDate date = LocalDate.of(year, month, 1);
            LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

            boolean exists = consumptionService.hasSavingsPlanForMonth(memberIn, startOfMonth, endOfMonth);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.info("hasSavingsPlanForMonth API 오류: {}" + e.getMessage() + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hasSavingsPlanForCurrentMonth")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> hasSavingsPlanForCurrentMonth(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Integer memberIn = consumptionService.getMemberInByMemberId(userDetails.getUsername());
        if (memberIn == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean exists = consumptionService.hasSavingsPlanForCurrentMonth(memberIn);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.info("hasSavingsPlanForCurrentMonth API 오류: {}" + e.getMessage() + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/getSavingsPlanForMonth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSavingsPlanForMonth(
            @RequestParam("startOfMonth") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startOfMonth,
            @RequestParam("endOfMonth") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endOfMonth,
            @AuthenticationPrincipal UserDetails userDetails) {
        YearMonth currentYearMonth = YearMonth.now();
        LocalDate conMonthDate = currentYearMonth.atDay(1);
        LocalDate today = LocalDate.now();
	    int year = today.getYear();
	    int month = today.getMonthValue();
    	
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer memberIn = consumptionService.getMemberInByMemberId(userDetails.getUsername());
        if (memberIn == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
        	Integer saveIn = consumptionService.getSaveIn(memberIn, year, month);
            Map<String, Object> savingsPlanData = consumptionService.getSavingsPlanAndCategoriesByMonth(memberIn, saveIn, startOfMonth, endOfMonth);
            
            if (savingsPlanData != null && !savingsPlanData.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "data", savingsPlanData));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "선택된 월에 저장된 예산이 없습니다."));
            }
        } catch (Exception e) {
            log.severe("getSavingsPlanForMonth API 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "예산 로드 중 오류가 발생했습니다."));
        }
    }
    
    
    @PostMapping("/bplannerPro")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBudgetPlanner(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        if (memberIn == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("success", false, "message", "사용자 정보를 찾을 수 없습니다."));
        }

        try {
            String saveName = (String) payload.get("saveName");
            LocalDate saveCreatedDate = LocalDate.parse((String) payload.get("saveCreatedDate"));
            LocalDate saveTargetDate = LocalDate.parse((String) payload.get("saveTargetDate"));
            Integer saveAmount = (Integer) payload.get("saveAmount");

            List<Map<String, Object>> expenseDetailsMaps = (List<Map<String, Object>>) payload.get("expenseDetails");
            List<SavingsDetail> expenseDetails = new ArrayList<>();

            // ⭐ 추가된 부분: 카테고리 이름과 ID 매핑을 위한 Map 생성
            List<CategoryMainDTO> allExpenseCategories = consumptionService.getAllExpenseCategories();
            Map<String, Integer> categoryNameToIdMap = new HashMap<>();
            for (CategoryMainDTO category : allExpenseCategories) {
                categoryNameToIdMap.put(category.getCmName(), category.getCmIn());
            }

            if (expenseDetailsMaps != null) {
                for (Map<String, Object> item : expenseDetailsMaps) {
                    SavingsDetail detail = new SavingsDetail();
                    Object nameOrCmIn = item.get("name"); // 이 필드에 카테고리 이름("식비") 또는 ID(1)가 올 수 있음

                    Integer cmIn = null;
                    if (nameOrCmIn instanceof Integer) {
                        // 이미 Integer 타입으로 넘어온 경우 (새로 추가된 항목일 가능성)
                        cmIn = (Integer) nameOrCmIn;
                    } else if (nameOrCmIn instanceof String) {
                        // String 타입으로 넘어온 경우 (불러온 기존 항목일 가능성)
                        String categoryName = (String) nameOrCmIn;
                        cmIn = categoryNameToIdMap.get(categoryName); // 이름으로 ID 조회

                        if (cmIn == null) {
                            // 매핑되는 카테고리 ID를 찾을 수 없는 경우 경고 로그를 남기고 다음 항목으로 넘어갑니다.
                            log.warning("알 수 없는 카테고리 이름이 감지되었습니다. 스킵합니다: " + categoryName);
                            continue; // 이 항목은 저장하지 않고 건너뜁니다.
                        }
                    } else {
                        // 예상치 못한 타입인 경우 (로그 남기고 스킵)
                        log.warning("expenseDetails의 'name' 필드에서 예상치 못한 타입이 감지되었습니다. 스킵합니다: " + nameOrCmIn);
                        continue; // 이 항목은 저장하지 않고 건너뜁니다.
                    }

                    detail.setCmIn(cmIn); // 최종적으로 결정된 cmIn 설정
                    detail.setSdCost((Integer) item.get("amount"));
                    detail.setSdDate(saveCreatedDate); // 예산 설정일 사용
                    expenseDetails.add(detail);
                }
            }

            Map<String, Object> result = consumptionService.saveBudgetPlanAndDetails(
                    memberIn, saveName, saveCreatedDate, saveTargetDate, saveAmount, expenseDetails);

            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            log.severe("예산 및 지출 상세 저장 중 오류 발생: " + e.getMessage()); // log.error 대신 log.severe 사용 (기존 코드와 일관성)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("success", false, "message", "예산 및 지출 상세 저장 중 오류 발생: " + e.getMessage()));
        }
    }
    
    @GetMapping("/checkPlan")
    @ResponseBody
    public int checkPlan(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    	log.info("ConsumptionController canalysis()");
    	String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        
        int chackPlan = consumptionService.planChack(memberIn);
        
    	return chackPlan;
    }
    
    @PostMapping("/efeedset")
    @ResponseBody
    public Map<String, Object> efeedset(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> payload) {
        log.info("ConsumptionController efeedset() 호출됨");
    	String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        String aiFeedback = (String) payload.get("feedbackContent");

        // 1. 이번 달 계획이 있는지 확인 (기존 로직 유지)
        int checkPlan = consumptionService.planChack(memberIn);
        if(checkPlan != 1) { // 계획이 없는 경우
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "회원 " + memberId + "님의 이번달 계획이 없습니다. 예산 설정을 하세요.");
            return response;
        }

        // 2. 이미 AI 피드백이 저장되어 있는지 확인
        boolean hasExistingFeedback = consumptionService.hasExistingFeedback(memberIn, LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        if (hasExistingFeedback) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미 저장된 피드백이 있습니다.");
            return response;
        }

        // 3. 모든 검사를 통과하면 AI 피드백 처리 및 저장
        log.info("수신된 AI 피드백 내용: {}" + aiFeedback);
        consumptionService.processAiFeedback(memberIn, aiFeedback);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "AI 피드백 처리 및 목표 생성이 성공적으로 완료되었습니다.");
        return response;
    }
    
    @PostMapping("/cfeedset")
    @ResponseBody
    public Map<String, Object> cfeedset(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> payload) {
        log.info("ConsumptionController cfeedset() 호출됨");
        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        String aiFeedback = (String) payload.get("feedbackContent");

        // 1. 이번 달 계획이 있는지 확인 (기존 로직 유지)
        int checkPlan = consumptionService.planChack(memberIn);
        if (checkPlan != 1) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "회원 " + memberId + "님의 이번달 계획이 없습니다. 예산 설정을 하세요.");
            return response;
        }
        log.info("수신된 종합 분석 피드백 내용 (bud_feedback): {}" + aiFeedback);
        consumptionService.processAicFeedback(memberIn, aiFeedback);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "종합 분석 피드백 처리 및 목표 생성이 성공적으로 완료되었습니다.");
        return response;
    }
    
    @GetMapping("/hasBudFeedback")
    @ResponseBody
    public Map<String, Object> hasBudFeedback(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("ConsumptionController hasBudFeedback() 호출됨");
        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        Map<String, Object> response = new HashMap<>();
        try {
            // 이번 달의 bud_feedback 존재 여부를 확인하는 서비스 메소드 호출
            boolean exists = consumptionService.hasExistingBudFeedback(memberIn);
            response.put("exists", exists);
            response.put("success", true);
            log.info("bud_feedback 존재 여부 확인: {}" + exists);
        } catch (Exception e) {
            log.info("bud_feedback 존재 여부 확인 중 오류 발생: {}" + e.getMessage());
            response.put("success", false);
            response.put("error", "피드백 존재 여부 확인 중 서버 오류가 발생했습니다.");
        }
        return response;
    }
    
    @GetMapping("/canalysis")
    public String canalysis(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController canalysis()");
        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        
        // 이달의 총지출액(이번 달 총 지출)
        int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
        model.addAttribute("totalExpense", totalExpense);

        LocalDate today = LocalDate.now();
        int totalDaysInMonth = today.lengthOfMonth();
        int currentDayOfMonth = today.getDayOfMonth();
        int year = today.getYear();
        int month = today.getMonthValue();
        int daysLeft = totalDaysInMonth - currentDayOfMonth;
        
        // 일별 지출액(일평균 지출)
        double averageDailyExpense = (currentDayOfMonth > 0) ? (double) totalExpense / currentDayOfMonth : 0;
        model.addAttribute("averageDailyExpense", (int) Math.round(averageDailyExpense));
        model.addAttribute("daysLeft", daysLeft);
        
        // 이번달 지출 횟수(총 거래 건수)
        int countThisMonth = consumptionService.thisMonthCount(memberIn);
        model.addAttribute("countThisMonth", countThisMonth);
        
        // 이번달 남은 금액(이번 달 남은 금액)
        int currentMonthBudget = consumptionService.getBudgetForCurrentMonth(memberIn);
        int remainingBudget = currentMonthBudget - totalExpense;
        model.addAttribute("remainingBudget", remainingBudget);
        
        // 이번 달 카테고리별 지출 데이터 (차트용)
        List<Map<String, Object>> categoryExpenses = consumptionService.getCategoryExpensesForChart(memberIn, month, year);
        log.info("categoryExpenses : " + categoryExpenses);
        model.addAttribute("categoryExpenses", categoryExpenses);

        // 1. 카테고리별 지출 분석 인사이트 (텍스트)
        String categoryAnalysisInsights = consumptionService.analyzeCategoryExpenses(categoryExpenses);
        model.addAttribute("categoryAnalysisInsights", categoryAnalysisInsights);

        // 2. 지난달 소비 수준 분석
        Map<String, Object> lastMonthAnalysis = consumptionService.getLastMonthExpenseAnalysis(memberIn);
        model.addAttribute("lastMonthAnalysis", lastMonthAnalysis);

        // 3. 지난달 절약 목표 정보
        Map<String, Object> previousMonthSavingsPlan = consumptionService.getPreviousMonthSavingsPlan(memberIn);
        model.addAttribute("previousMonthSavingsPlan", previousMonthSavingsPlan);
        
        return "/consumption/consumption_analysis";
    }
    
    @GetMapping("/getChatGptFeedbackForCanalysis")
    @ResponseBody
    public Map<String, Object> getChatGptFeedbackForCanalysis() { // Map<String, Object>로 반환 타입 변경
        log.info("ConsumptionController getChatGptFeedbackForCanalysis() 호출");
        Map<String, Object> response = new HashMap<>(); // Object 타입을 값으로 받을 수 있도록 변경
        String chatGptResponse = "분석 결과가 없습니다.";
        boolean hasExistingFeedback = false; // 새로운 변수 초기화

        try {
            String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (memberId == null || memberId.isEmpty()) {
                response.put("error", "로그인 정보가 없습니다.");
                return response;
            }

            Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
            if (memberIn == null) {
                response.put("error", "회원 정보를 찾을 수 없습니다.");
                return response;
            }
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            LocalDate conMonthDate = YearMonth.now().atDay(1); // 해당 월의 첫째 날

            // ★★★★ 기존 피드백 존재 여부 확인 로직 추가 ★★★★
            Optional<Map<String, Object>> existingConsumptionOpt = consumptionService.getConsumptionByMemberAndMonth(memberIn, conMonthDate);
            if (existingConsumptionOpt.isPresent()) {
                Object feedbackObj = existingConsumptionOpt.get().get("con_feedback");
                if (feedbackObj instanceof String) {
                    String existingFeedbackContent = (String) feedbackObj;
                    hasExistingFeedback = (existingFeedbackContent != null && !existingFeedbackContent.trim().isEmpty());
                }
            }
            response.put("hasExistingFeedback", hasExistingFeedback); // 클라이언트에 전송

            Map<String, Object> storedFeedbacks = consumptionService.getAllFeedback(memberIn, month, year);
            String storedCategoryFeedback = (String) storedFeedbacks.getOrDefault("categoryGptFeedback", "저장된 카테고리 피드백이 없습니다.");
            String storedMonthlyFeedback = (String) storedFeedbacks.getOrDefault("monthlyGptFeedback", "저장된 월간 피드백이 없습니다.");

            List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn); 
            
            Map<String, Integer> aggregatedCategoryExpenses = currentMonthCategoryExpenses.stream()
                .filter(e -> e.get("category_name") != null && e.get("total_expense") instanceof Number)
                .collect(Collectors.toMap(
                    e -> (String) e.get("category_name"),
                    e -> ((Number) e.get("total_expense")).intValue(),
                    Integer::sum
                ));

            int totalExpense = aggregatedCategoryExpenses.values().stream().mapToInt(Integer::intValue).sum();
            
            StringBuilder promptBuilder = new StringBuilder();
            
            promptBuilder.append("나는 이번 달에 지출이 다음과 같아:\n");
            if (aggregatedCategoryExpenses.isEmpty()) {
                promptBuilder.append("이번 달 지출 내역이 아직 없어.\n");
            } else {
                for (Map.Entry<String, Integer> entry : aggregatedCategoryExpenses.entrySet()) {
                    promptBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("원\n");
                }
                promptBuilder.append("총 지출: ").append(totalExpense).append("원\n\n");
            }
            
            promptBuilder.append("이전 분석받은 피드백은 다음과 같아:\n");
            promptBuilder.append("- 카테고리별 과거 피드백: ").append(storedCategoryFeedback).append("\n");
            promptBuilder.append("- 월간 과거 피드백: ").append(storedMonthlyFeedback).append("\n\n");

            promptBuilder.append("위의 이번 달 지출 내역과 이전 피드백을 종합하여, ");
            promptBuilder.append("나의 전반적인 소비 패턴에 대한 새로운 분석과 앞으로의 소비 습관 개선을 위한 ");
            promptBuilder.append("따뜻하고 격려적인 어조의 종합적인 AI 피드백을 제공해줘. ");
            promptBuilder.append("답변은 200글자로 내외로 핵심 내용을 요약하고, 줄바꿈을 포함하여 가독성 있게 작성해줘.\n");

            String prompt = promptBuilder.toString();
            log.info("ChatGPT Prompt: " + prompt);

            chatGptResponse = chatGPTClient.askChatGpt(prompt);
            log.info("ChatGPT Response: " + chatGptResponse);

            response.put("feedback", chatGptResponse);

        } catch (Exception e) {
            log.severe("ChatGPT 피드백 생성 중 오류 발생: " + e.getMessage());
            response.put("error", "피드백을 생성하는 중 오류가 발생했습니다.");
            response.put("feedback", "AI 피드백을 생성하는 데 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
        return response;
    }

    @PostMapping("/saveAiFeedback")
    @ResponseBody
    public ResponseEntity<?> saveAiFeedback(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        String feedbackContent = (String) payload.get("feedback");
        Boolean isUpdate = (Boolean) payload.get("isUpdate");

        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        YearMonth currentYearMonth = YearMonth.now();
        LocalDate conMonthDate = currentYearMonth.atDay(1);
        LocalDate today = LocalDate.now();
	    int year = today.getYear();
	    int month = today.getMonthValue();
	    Integer saveIn = consumptionService.getSaveIn(memberIn, year, month);
	    int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
        if (feedbackContent == null || feedbackContent.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "피드백 내용이 없습니다."));
        }

        try {
            consumptionService.saveNewOrUpdateAiFeedback(memberIn, saveIn, conMonthDate, feedbackContent, totalExpense);
            
            String message = (isUpdate != null && isUpdate) ? "AI 피드백이 성공적으로 업데이트되었습니다." : "AI 피드백이 성공적으로 저장되었습니다.";
            return ResponseEntity.ok().body(Map.of("message", message));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 ID 형식이 올바르지 않습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "AI 피드백 저장/업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 소비 분석 피드백 리스트 페이지
    @GetMapping("/consumptionList")
    public String consumptionList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController consumptionList() 호출됨.");

        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        List<Map<String, Object>> feedbackList = Collections.emptyList();

        if (memberIn != null) {
            feedbackList = consumptionService.getConsumptionFeedbacksByMemberIn(memberIn);
        } else {
            log.info("memberId: {} 에 해당하는 member_in을 찾을 수 없습니다. 빈 피드백 리스트를 반환합니다."+ memberId);
        }

        model.addAttribute("feedbackList", feedbackList);
        return "/consumption/consumption_list";
    }
    
    
    @GetMapping("/categoryExpenses")
    @ResponseBody
    public Map<String, Integer> getCategoryExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "month") int month,
            @RequestParam(name = "year") int year) {

        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        if (memberIn == null) {
            log.info("MemberIn not found for memberId: {}" + memberId);
            return Collections.emptyMap();
        }

        List<Map<String, Object>> rawCategoryExpenses =
                consumptionService.getCategoryExpensesForChart(memberIn, month, year);
        log.info("카테고리 이름 및 값이 가져와지나? " + rawCategoryExpenses);
        Map<String, Integer> categoryExpensesMap = new HashMap<>();
        if (rawCategoryExpenses != null) {
            for (Map<String, Object> entry : rawCategoryExpenses) {
                String categoryName = (String) entry.get("categoryName");
                Object totalAmountObj = entry.get("amount");
                log.info("카테고리 이름 및 값이 가져와지나? " +categoryName + totalAmountObj);

                if (categoryName != null && totalAmountObj != null) {
                    try {
                        int totalAmount = 0;
                        if (totalAmountObj instanceof Number) {
                            totalAmount = ((Number) totalAmountObj).intValue();
                        } else if (totalAmountObj instanceof String) {
                            totalAmount = Integer.parseInt((String) totalAmountObj);
                        } else {
                            log.info("Unknown type for totalAmount: {}. Value: {}"+ totalAmountObj.getClass().getName()+ totalAmountObj);
                            continue;
                        }
                        categoryExpensesMap.put(categoryName, totalAmount);
                    } catch (NumberFormatException e) {
                        log.info("Failed to parse totalAmount to int for entry: {} - {}"+ entry+ e.getMessage());
                    } catch (Exception e) {
                        log.info("Error processing category expense entry: {} - {}"+ entry+ e.getMessage());
                    }
                } else {
                    log.info("Skipping entry due to null categoryName or totalAmount: {}"+ entry);
                }
            }
        }

        log.info("API 호출: memberIn={}, month={}, year={} - Category Expenses: {}"+ memberIn+ month+ year+ categoryExpensesMap);
        return categoryExpensesMap;
    }
    
    @DeleteMapping("/feedback/{conInId}")
    @ResponseBody
    public ResponseEntity<String> deleteConsumptionFeedback(@PathVariable("conInId") Integer conInId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("ConsumptionController deleteConsumptionFeedback() - conInId: {}"+ conInId);

        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);

        if (memberIn == null) {
            log.info("삭제 시도 - memberId: {} 에 해당하는 member_in을 찾을 수 없습니다."+ memberId);
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"사용자 정보를 찾을 수 없습니다.\"}", HttpStatus.UNAUTHORIZED);
        }

        boolean hasPermission = consumptionService.checkFeedbackOwnership(conInId, memberIn);
        if (!hasPermission) {
            log.info("삭제 권한 없음 - conInId: {}, memberIn: {}"+ conInId+ memberIn);
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"삭제 권한이 없습니다.\"}", HttpStatus.FORBIDDEN);
        }

        try {
            boolean isDeleted = consumptionService.deleteConsumptionFeedback(conInId);

            if (isDeleted) {
                log.info("피드백 성공적으로 삭제됨 - conInId: {}"+ conInId);
                return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"피드백이 성공적으로 삭제되었습니다.\"}", HttpStatus.OK);
            } else {
                log.info("피드백 삭제 실패 - conInId: {}"+ conInId);
                return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"피드백 삭제에 실패했습니다.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.info("피드백 삭제 중 예외 발생 - conInId: {}"+ conInId+ e);
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"서버 오류로 인해 삭제에 실패했습니다.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/expense-categories")
    public ResponseEntity<List<CategoryMainDTO>> getAllExpenseCategories() {
        List<CategoryMainDTO> categories = consumptionService.getAllExpenseCategories();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/loadBudgetPlan")
    public ResponseEntity<Map<String, Object>> loadBudgetPlan(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String memberId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID를 가져옴
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId); // memberId로 memberIn 조회

        if (memberIn == null) {
            return new ResponseEntity<>(Collections.singletonMap("message", "사용자 정보를 찾을 수 없습니다."), HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> budgetPlanData = consumptionService.loadBudgetPlan(memberIn, year, month);
            return new ResponseEntity<>(budgetPlanData, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Collections.singletonMap("message", "예산 로드 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/api/income-categories")
    public ResponseEntity<List<CategorySubDTO>> getAllIncomeCategories() {
        List<CategorySubDTO> incomeCategories = consumptionService.getAllIncomeCategories();
        return ResponseEntity.ok(incomeCategories);
    }
    
}