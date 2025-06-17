package com.yageum.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.yageum.domain.SavingsPlanDTO;
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

        model.addAttribute("monthlyExpensesLabels", monthlyExpensesLabels);
        model.addAttribute("monthlyExpensesValues", monthlyExpensesValues);

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
    
    @PostMapping("/bplannerPro")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bplannerPro(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> payload,
            SavingsPlanDTO savingsPlanDTO
    ) {
        Map<String, Object> response = new HashMap<>();
        log.info("ConsumptionController bplannerPro() 호출 - 총 수입 저장 요청");

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인된 사용자 정보를 찾을 수 없습니다.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String memberIdStr = userDetails.getUsername();
            Integer memberIn = consumptionService.getMemberInByMemberId(memberIdStr);

            if (memberIn == null) {
                response.put("success", false);
                response.put("message", "회원 고유 번호를 찾을 수 없습니다.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            Integer totalIncome = (Integer) payload.get("totalIncome");
            String saveName = (String) payload.get("save_name");

            if (totalIncome == null) {
                response.put("success", false);
                response.put("message", "총 수입 값이 누락되었습니다.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
            int chackPlan = consumptionService.planChack(memberIn);

            if (chackPlan == 0) {
                savingsPlanDTO.setMemberId(memberIn);
                savingsPlanDTO.setSaveName(saveName);
                savingsPlanDTO.setSaveAmount(totalIncome);
                savingsPlanDTO.setSaveCreatedDate(firstDayOfMonth);
                savingsPlanDTO.setSaveTargetDate(lastDayOfMonth);
                consumptionService.updateMonthlyIncome(savingsPlanDTO); 

                log.info("회원 ID {}의 총 수입 {} 저장 성공" + memberIn + totalIncome);
                response.put("success", true);
                response.put("message", "예산이 성공적으로 저장되었습니다.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                log.info("회원 ID {}의 이번 달 예산은 이미 저장되어 있습니다." + memberIn);
                response.put("success", false);
                response.put("message", "이번 달 예산이 이미 저장되어 있습니다.");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

        } catch (Exception e) {
            log.info("bplannerPro 예산 저장 중 서버 오류 발생:" + e);
            response.put("success", false);
            response.put("message", "예산 저장 중 서버 내부 오류가 발생했습니다: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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
        log.info("ConsumptionController efeedset()");
    	String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        String aiFeedback = (String) payload.get("feedbackContent");
        int checkPlan = consumptionService.planChack(memberIn);
        if(checkPlan == 1) {
	        log.info("수신된 AI 피드백 내용: {}" + aiFeedback);
	        consumptionService.processAiFeedback(memberIn, aiFeedback);

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "AI 피드백 처리 및 목표 생성이 성공적으로 완료되었습니다.");
	        return response;
	    } else {
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", false);
	        response.put("message", "회원 " + memberId + "님의 이번달 계획이 없습니다. 예산 설정을 하세요.");
	        return response;
	    }
    }
    
    @PostMapping("/cfeedset")
    @ResponseBody
    public Map<String, Object> cfeedset(@AuthenticationPrincipal UserDetails userDetails,
            							@RequestBody Map<String, Object> payload) {
        log.info("ConsumptionController efeedset()");
    	String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        String aiFeedback = (String) payload.get("feedbackContent");
        int checkPlan = consumptionService.planChack(memberIn);
        if(checkPlan == 1) {
	        log.info("수신된 AI 피드백 내용: {}" + aiFeedback);
	        consumptionService.processAicFeedback(memberIn, aiFeedback);

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("message", "AI 피드백 처리 및 목표 생성이 성공적으로 완료되었습니다.");
	        return response;
	    } else {
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", false);
	        response.put("message", "회원 " + memberId + "님의 이번달 계획이 없습니다. 예산 설정을 하세요.");
	        return response;
	    }
    }
    
    @GetMapping("/canalysis")
    public String canalysis(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("ConsumptionController canalysis()");
        String memberId = userDetails.getUsername();
        Integer memberIn = consumptionService.getMemberInByMemberId(memberId);
        
        return "/consumption/consumption_analysis";
    }
}