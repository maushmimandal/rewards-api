package com.charter.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Summary of reward points for a single customer across all months
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewardSummary {

    // customer ID
    private String customerId;

    // customer name
    private String customerName;

    // month-wise breakdown of points
    private List<MonthlyPoints> monthlyPoints;

    // total points earned across all months
    private long totalPoints;
}
