package com.charter.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Top level response returned by the API
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsResponse {

    // list of reward summaries, one per customer
    private List<CustomerRewardSummary> customerRewards;

    // total number of customers in the response
    private int totalCustomers;
}
