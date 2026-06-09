package com.charter.rewards.controller;

import com.charter.rewards.data.TransactionDataLoader;
import com.charter.rewards.model.CustomerRewardSummary;
import com.charter.rewards.model.RewardsResponse;
import com.charter.rewards.model.Transaction;
import com.charter.rewards.service.RewardsCalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller that exposes the rewards endpoints
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    private final RewardsCalculatorService rewardsCalculatorService;
    private final TransactionDataLoader transactionDataLoader;

    // Constructor injection
    public RewardsController(RewardsCalculatorService rewardsCalculatorService,
                             TransactionDataLoader transactionDataLoader) {
        this.rewardsCalculatorService = rewardsCalculatorService;
        this.transactionDataLoader = transactionDataLoader;
    }

    // GET /api/rewards
    // Returns reward points for all 3 sample customers using the built-in dataset
    @GetMapping
    public ResponseEntity<RewardsResponse> getRewards() {
        List<Transaction> transactions = transactionDataLoader.getSampleTransactions();
        List<CustomerRewardSummary> summaries = rewardsCalculatorService.calculateRewards(transactions);

        RewardsResponse response = RewardsResponse.builder()
                .customerRewards(summaries)
                .totalCustomers(summaries.size())
                .build();

        return ResponseEntity.ok(response);
    }

    // POST /api/rewards/calculate
    // Accepts a custom list of transactions from the caller and returns the reward summary
    @PostMapping("/calculate")
    public ResponseEntity<RewardsResponse> calculateRewards(
            @RequestBody List<Transaction> transactions) {

        List<CustomerRewardSummary> summaries = rewardsCalculatorService.calculateRewards(transactions);

        RewardsResponse response = RewardsResponse.builder()
                .customerRewards(summaries)
                .totalCustomers(summaries.size())
                .build();

        return ResponseEntity.ok(response);
    }
}
