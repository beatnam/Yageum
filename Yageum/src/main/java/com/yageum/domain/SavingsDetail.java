package com.yageum.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsDetail {
    private Integer sdIn;
    private Integer saveIn;
    private Integer cmIn;
    private LocalDate sdDate;
    private Integer sdCost;
    
    public SavingsDetail() {}

    public SavingsDetail(Integer saveIn, Integer cmIn, LocalDate sdDate, Integer sdCost) {
        this.saveIn = saveIn;
        this.cmIn = cmIn;
        this.sdDate = sdDate;
        this.sdCost = sdCost;
    }
    
    @Override
    public String toString() {
        return "SavingsDetail{" +
               "sdIn=" + sdIn +
               ", saveIn=" + saveIn +
               ", cmIn=" + cmIn +
               ", sdDate=" + sdDate +
               ", sdCost=" + sdCost +
               '}';
    }
    
}
