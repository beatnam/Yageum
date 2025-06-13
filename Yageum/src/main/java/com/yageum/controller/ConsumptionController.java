package com.yageum.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yageum.service.ConsumptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yageum.service.ChatGPTClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/consumption/*")
public class ConsumptionController {

    private final ConsumptionService consumptionService;
    private final ChatGPTClient chatGPTClient;

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
        // 예시: 100 - (totalExpense / 5000) -> 10만원 지출 시 80점, 20만원 지출 시 60점, 30만원 지출 시 40점
        // 적절한 값을 찾아야 합니다. 여기서는 10000을 5000으로 줄여보겠습니다.
        // 더 완만하게 점수가 떨어지게 하려면, 10000보다 훨씬 큰 값(예: 20000, 30000)을 사용해야 합니다.
        // 저는 '어느 정도 지출을 해도 보통 이상은 줄 수 있도록' 나눗셈 기준을 15000 정도로 높여보겠습니다.
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

            String icon = "💡";
            String title = categoryName != null ? categoryName + " 지출" : "기타 지출";
            String status = "정보";
            String statusClass = "info";
            String description = categoryName + "에 총 " + totalExpenseForCategory + "원 지출했습니다.";

            if (categoryName != null) {
                switch (categoryName) {
                    case "식비":
                        icon = "🍽️";
                        // 식비 기준도 좀 더 유연하게 조정
                        if (totalExpenseForCategory > 70000) { // 5만원에서 7만원으로 상향
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
                        // 쇼핑 기준도 좀 더 유연하게 조정
                        if (totalExpenseForCategory > 150000) { // 10만원에서 15만원으로 상향
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

            detail.put("icon", icon);
            detail.put("title", title);
            detail.put("status", status);
            detail.put("statusClass", statusClass);
            detail.put("description", description);
            processedFeedbackDetails.add(detail);
        }

        List<Map<String, Object>> progressData = List.of(
            Map.of("progressText", "식비 절약 45%", "progressPercentage", 45),
            Map.of("progressText", "비상금 적립 92%", "progressPercentage", 92)
        );

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
        feedbackSummary.put("progress", progressData);

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

    @GetMapping("/eanalysis") // 이 메서드를 수정합니다.
    public String getExpenseAnalysis(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController getExpenseAnalysis() 호출");
        String memberId = userDetails.getUsername(); // Spring Security UserDetails에서 username은 memberId와 같음

        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        if (memberIn == null) {
            log.warning("Member not found for memberId: " + memberId);
            return "redirect:/error";
        }

        // 이번 달 총 지출 (기존 코드 유지)
        int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
        model.addAttribute("totalExpense", totalExpense);

        // 현재 날짜 정보 (기존 코드 유지)
        LocalDate today = LocalDate.now();
        int totalDaysInMonth = today.lengthOfMonth();
        int currentDayOfMonth = today.getDayOfMonth();
        int daysLeft = totalDaysInMonth - currentDayOfMonth;

        double averageDailyExpense = (currentDayOfMonth > 0) ? (double) totalExpense / currentDayOfMonth : 0;
        model.addAttribute("averageDailyExpense", (int) Math.round(averageDailyExpense));
        model.addAttribute("daysLeft", daysLeft);

        // 남은 예산 (기존 코드 유지)
        int budget = 2000000; // 이 값은 실제 예산 설정 로직에 따라 변경될 수 있습니다.
        int remainingBudget = budget - totalExpense;
        model.addAttribute("remainingBudget", remainingBudget);

        // ====== 변경된 부분 시작 ======
        // 월별 지출 데이터 로드 (현재 연도 1월부터 12월까지)
        // consumptionService.getMonthlyExpensesForPastMonths(memberIn, 12); 대신 아래 메서드 호출
        List<Map<String, Object>> monthlyExpensesData = consumptionService.getMonthlyExpensesForCurrentYear(memberIn);

        if (monthlyExpensesData == null) {
            monthlyExpensesData = new ArrayList<>();
            log.info("monthlyExpensesData was null, initialized to empty list.");
        }

        List<String> monthlyExpensesLabels = new ArrayList<>();
        List<Integer> monthlyExpensesValues = new ArrayList<>();

        // monthlyExpensesLabels 생성 로직 간소화
        // getMonthlyExpensesForCurrentYear는 month (1~12)와 currentYear를 보장하므로 복잡한 로직이 필요 없음.
        if (!monthlyExpensesData.isEmpty()) {
            monthlyExpensesData.forEach(entry -> {
                int monthValue = (int) entry.get("month"); // 월 값은 항상 Integer로 넘어옴
                // 서비스에서 totalExpense 키로 값을 넣으므로 "total_expense" 대신 "totalExpense" 사용
                int totalExpenseAmount = ((Number) entry.get("totalExpense")).intValue();
                
                // 라벨은 "N월" 형식으로 고정 (예: 1월, 2월, ..., 12월)
                monthlyExpensesLabels.add(monthValue + "월");
                monthlyExpensesValues.add(totalExpenseAmount);
            });
        }
        // ====== 변경된 부분 끝 ======
        
        log.info("monthlyExpensesLabels (before model add): " + monthlyExpensesLabels);
        log.info("monthlyExpensesValues (before model add): " + monthlyExpensesValues);

        // ====================================================================
        // 이 부분이 JSON 변환 로직입니다. (이제 eanalysis 메서드 안에 위치)
        ObjectMapper objectMapper = new ObjectMapper();
        String monthlyExpensesLabelsJson = "[]";
        String monthlyExpensesValuesJson = "[]";

        try {
            monthlyExpensesLabelsJson = objectMapper.writeValueAsString(monthlyExpensesLabels);
            monthlyExpensesValuesJson = objectMapper.writeValueAsString(monthlyExpensesValues);
        } catch (Exception e) {
            log.severe("JSON 변환 중 오류 발생: " + e.getMessage());
        }
        log.info("monthlyExpensesLabels (after JSON conversion): " + monthlyExpensesLabelsJson); // JSON 변환 후 로그 추가
        log.info("monthlyExpensesValues (after JSON conversion): " + monthlyExpensesValuesJson); // JSON 변환 후 로그 추가
        // Model에 JSON 문자열 추가 (기존 List<String>과 List<Integer> 대신)
        model.addAttribute("monthlyExpensesLabels", monthlyExpensesLabelsJson);
        model.addAttribute("monthlyExpensesValues", monthlyExpensesValuesJson);
        // ====================================================================

        // 절약 목표 정보 (기존 코드 유지)
        Map<String, Object> savingsPlan = consumptionService.getLatestSavingsPlanByMemberIn(memberIn);

        // *** 여기부터 추가할 코드: java.sql.Date 타입을 java.time.LocalDate로 변환 ***
        if (savingsPlan != null) {
            if (savingsPlan.get("save_created_date") instanceof java.sql.Date) {
                savingsPlan.put("save_created_date", ((java.sql.Date) savingsPlan.get("save_created_date")).toLocalDate());
            }
            if (savingsPlan.get("save_target_date") instanceof java.sql.Date) {
                savingsPlan.put("save_target_date", ((java.sql.Date) savingsPlan.get("save_target_date")).toLocalDate());
            }
        }
        // *** 여기까지 추가할 코드 ***

        model.addAttribute("savingsPlan", savingsPlan); // 수정된 savingsPlan을 모델에 추가

        // ==== 새로 추가할 progress 계산 로직 시작 (이 부분이 올바르게 있는지 확인) ====
        double progress = 0.0;
        if (savingsPlan != null &&
            savingsPlan.containsKey("currentSavings") && savingsPlan.get("currentSavings") instanceof Number &&
            savingsPlan.containsKey("save_amount") && savingsPlan.get("save_amount") instanceof Number) {

            double currentSavings = ((Number) savingsPlan.get("currentSavings")).doubleValue();
            double saveAmount = ((Number) savingsPlan.get("save_amount")).doubleValue();

            if (saveAmount != 0.0) {
                progress = (currentSavings * 100.0) / saveAmount;
            }
        }
        model.addAttribute("progress", progress); // 계산된 progress 값을 모델에 추가
        // ==== 새로 추가할 progress 계산 로직 끝 ====
        
        return "/consumption/expense_analysis";
    }


    @GetMapping("/bplanner")
    public String bplanner() {
        log.info("ConsumptionController bplanner() 호출");
        return "/consumption/budget_planner";
    }

    @GetMapping("/canalysis")
    public String canalysis() {
        log.info("ConsumptionController canalysis() 호출");
        return "/consumption/category_analysis";
    }

 // ⭐ 새로 추가되는 ChatGPT API 엔드포인트 ⭐
    // 이 메서드는 expense_feedback.html에서 AJAX로 호출됩니다.
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

            // 현재 월의 카테고리별 지출 데이터 가져오기 (efeedback 페이지의 주 데이터와 동일한 데이터)
            // efeedback 페이지는 getCategoryExpenseByMemberId를 사용하므로, 이쪽 데이터를 사용합니다.
            List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn);

            // ChatGPT에 보낼 prompt 생성
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("나는 이번달에 지출이 다음과 같아:\n");
            Map<String, Integer> aggregatedCategoryExpenses = new HashMap<>();
            int totalExpense = 0;

            for (Map<String, Object> entry : currentMonthCategoryExpenses) { // getCategoryExpenseByMemberId 결과는 이미 카테고리별 총합
                String category = (String) entry.get("category_name");
                Object totalExpenseObj = entry.get("total_expense");
                int amount = 0;
                if (totalExpenseObj instanceof Number) {
                    amount = ((Number) totalExpenseObj).intValue();
                }
                aggregatedCategoryExpenses.put(category, aggregatedCategoryExpenses.getOrDefault(category, 0) + amount); // 혹시 몰라서 한번 더 집계
                totalExpense += amount;
            }

            if (aggregatedCategoryExpenses.isEmpty()) {
                promptBuilder.append("이번 달 지출 내역이 없어.");
            } else {
                for (Map.Entry<String, Integer> entry : aggregatedCategoryExpenses.entrySet()) {
                    promptBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("원\n");
                }
                promptBuilder.append("총 지출: ").append(totalExpense).append("원\n\n");
                promptBuilder.append("이 지출 내역을 바탕으로 따뜻한 어조로 카테고리별로 분석하고, 장단점과 함께 피드백을 해줘. 그리고 전반적인 소비 패턴에 대한 의견도 줘. 그리고 300글자 내로 답해줘.");
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

            // 프론트엔드에서 전달된 선택 지표 목록
            List<String> selectedMetrics = requestBody.get("selectedMetrics");
            if (selectedMetrics == null || selectedMetrics.isEmpty()) {
                // 선택된 지표가 없는 경우 (예: 초기 로딩 시), 기본적으로 모든 지표를 포함
                selectedMetrics = new ArrayList<>();
                selectedMetrics.add("totalExpense");
                selectedMetrics.add("averageDailyExpense");
                selectedMetrics.add("daysLeft");
                selectedMetrics.add("remainingBudget");
                selectedMetrics.add("categoryExpenses"); // 카테고리별 지출도 기본 포함
            }

            // 필요한 재무 지표 값 계산
            int totalExpense = consumptionService.getTotalExpenseForCurrentMonth(memberIn);
            
            LocalDate today = LocalDate.now();
            int totalDaysInMonth = today.lengthOfMonth();
            int currentDayOfMonth = today.getDayOfMonth();
            int daysLeft = totalDaysInMonth - currentDayOfMonth;

            double averageDailyExpense = (currentDayOfMonth > 0) ? (double) totalExpense / currentDayOfMonth : 0;
            
            // 예산 (이 값은 실제 예산 데이터를 가져오도록 변경해야 합니다)
            int budget = 2000000; // 임시 고정 예산. 실제로는 DB 또는 설정에서 가져오는 것이 좋습니다.
            int remainingBudget = budget - totalExpense;


            // ChatGPT에 보낼 prompt 생성
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("너는 소비 분석 전문가야. 다음 지출 데이터를 바탕으로 따뜻하고 이해하기 쉬운 어조로 자세히 분석하고, 장단점과 개선점을 피드백해줘. 전반적인 소비 패턴에 대한 의견과 평균 지출 대비 소비 사이즈가 어떤지에 대한 의견도 줘. 답변은 최대 300글자 내로 줄바꿈을 포함하여 가독성 있게 작성해줘.\n\n");

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
            
            // 카테고리별 지출 데이터도 프롬프트에 포함 (selectedMetrics에 "categoryExpenses"가 있다면)
            if (selectedMetrics.contains("categoryExpenses")) {
                List<Map<String, Object>> currentMonthCategoryExpenses = consumptionService.getCategoryExpenseByMemberId(memberIn);
                Map<String, Integer> aggregatedCategoryExpenses = new HashMap<>();
                for (Map<String, Object> entry : currentMonthCategoryExpenses) {
                    String category = (String) entry.get("category_name");
                    Object totalExpenseObj = entry.get("total_expense");
                    int amount = 0;
                    if (totalExpenseObj instanceof Number) {
                        amount = ((Number) totalExpenseObj).intValue();
                    }
                    aggregatedCategoryExpenses.put(category, aggregatedCategoryExpenses.getOrDefault(category, 0) + amount);
                }
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
}