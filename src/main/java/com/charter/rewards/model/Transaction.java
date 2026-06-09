package com.charter.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Represents a single purchase made by a customer
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // customer ID
    private String customerId;

    // customer name
    private String customerName;

    // amount spent in this transaction
    private double amount;

    // date when the purchase was made
    private LocalDate transactionDate;
}
