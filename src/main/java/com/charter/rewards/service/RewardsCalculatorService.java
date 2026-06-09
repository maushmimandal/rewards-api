package com.charter.rewards.service;

import com.charter.rewards.exception.InvalidTransactionException;
import com.charter.rewards.model.CustomerRewardSummary;
import com.charter.rewards.model.MonthlyPoints;
import com.charter.rewards.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Core service that handles all the reward point calculations
// Rules:
//   - 2 points for every dollar spent over $100 in a transaction
//   - 1 point for every dollar spent between $50 and $100
//   - nothing below $50
// Example: $120 purchase = 2x$20 + 1x$50 = 90 points
@Service
public class RewardsCalculatorService {

    // Calculates points for a single transaction amount
    public long calculatePoints(double amount) {
        if (amount < 0) {
            throw new InvalidTransactionException(
                    "Transaction amount cannot be negative. Received: " + amount);
        }

        long points = 0;

        if (amount > 100) {
            // 2 points per dollar above $100
            points += (long) ((amount - 100) * 2);
            // plus 1 point per dollar from $50 to $100, which is always 50 points
            points += 50;
        } else if (amount > 50) {
            // 1 point per dollar above $50
            points += (long) (amount - 50);
        }
        // anything at or below $50 gets 0 points

        return points;
    }

    // Takes a list of transactions, groups them by customer and month,
    // then returns a summary with monthly breakdown and total for each customer
    public List<CustomerRewardSummary> calculateRewards(List<Transaction> transactions) {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions list must not be null.");
        }

        // validate everything before we start processing
        transactions.forEach(this::validateTransaction);

        // group transactions by customer ID
        Map<String, List<Transaction>> byCustomer = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<CustomerRewardSummary> summaries = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : byCustomer.entrySet()) {
            String customerId = entry.getKey();
            List<Transaction> customerTxns = entry.getValue();

            // use the name from the first transaction
            String customerName = customerTxns.get(0).getCustomerName();

            // group this customer's transactions by year and month
            Map<String, List<Transaction>> byMonth = customerTxns.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getTransactionDate().getYear() + "-" + t.getTransactionDate().getMonthValue()
                    ));

            List<MonthlyPoints> monthlyPointsList = new ArrayList<>();

            for (Map.Entry<String, List<Transaction>> monthEntry : byMonth.entrySet()) {
                List<Transaction> monthTxns = monthEntry.getValue();

                int year = monthTxns.get(0).getTransactionDate().getYear();
                int monthValue = monthTxns.get(0).getTransactionDate().getMonthValue();
                Month month = Month.of(monthValue);

                // sum up points for all transactions in this month
                long monthPoints = monthTxns.stream()
                        .mapToLong(t -> calculatePoints(t.getAmount()))
                        .sum();

                monthlyPointsList.add(MonthlyPoints.builder()
                        .year(year)
                        .month(monthValue)
                        .monthName(month.name())
                        .points(monthPoints)
                        .build());
            }

            // sort the monthly list from oldest to newest
            monthlyPointsList.sort(Comparator
                    .comparingInt(MonthlyPoints::getYear)
                    .thenComparingInt(MonthlyPoints::getMonth));

            long totalPoints = monthlyPointsList.stream()
                    .mapToLong(MonthlyPoints::getPoints)
                    .sum();

            summaries.add(CustomerRewardSummary.builder()
                    .customerId(customerId)
                    .customerName(customerName)
                    .monthlyPoints(monthlyPointsList)
                    .totalPoints(totalPoints)
                    .build());
        }

        // sort the final list by customer ID so the response is consistent
        summaries.sort(Comparator.comparing(CustomerRewardSummary::getCustomerId));

        return summaries;
    }

    // Checks that a transaction has valid required fields before processing
    private void validateTransaction(Transaction transaction) {
        if (transaction.getCustomerId() == null || transaction.getCustomerId().isBlank()) {
            throw new InvalidTransactionException("Transaction has a null or empty customerId.");
        }
        if (transaction.getTransactionDate() == null) {
            throw new InvalidTransactionException(
                    "Transaction for customer '" + transaction.getCustomerId() + "' has a null date.");
        }
        if (transaction.getAmount() < 0) {
            throw new InvalidTransactionException(
                    "Transaction for customer '" + transaction.getCustomerId()
                            + "' has a negative amount: " + transaction.getAmount());
        }
    }
}
