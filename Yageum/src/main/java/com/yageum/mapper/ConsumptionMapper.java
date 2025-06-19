package com.yageum.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface ConsumptionMapper {
	
    Optional<Map<String, Object>> findConsumptionByMemberAndMonth(@Param("memberIn") int memberIn, @Param("conMonth") LocalDate conMonth);

    void updateAiFeedback(@Param("memberIn") int memberIn, @Param("conMonth") LocalDate conMonth, @Param("feedbackContent") String feedbackContent);

    void insertConsumption(Map<String, Object> params);

    void updateOnlyAiFeedbackIfNull(@Param("memberIn") int memberIn, @Param("conMonth") LocalDate conMonth, @Param("feedbackContent") String feedbackContent);

    List<Map<String, Object>> getConsumptionFeedbacksByMemberIn(@Param("memberIn") Integer memberIn);

    boolean checkFeedbackOwnership(@Param("conInId") Integer conInId, @Param("memberIn") Integer memberIn);

    int deleteConsumptionFeedback(@Param("conInId") Integer conInId);
}
