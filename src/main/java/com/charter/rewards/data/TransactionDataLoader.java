package com.charter.rewards.data;

import com.charter.rewards.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Loads a sample set of transactions to demonstrate the rewards API
// We have 3 customers with 6 transactions each spread across 3 months
// Months are calculated dynamically from today so the data is always current
@Component
public class TransactionDataLoader {

    public List<Transaction> getSampleTransactions() {
        LocalDate now = LocalDate.now();

        // going back 2 months from today as the start of the window
        LocalDate month1 = now.minusMonths(2).withDayOfMonth(1);
        LocalDate month2 = now.minusMonths(1).withDayOfMonth(1);
        LocalDate month3 = now.withDayOfMonth(1);

        return List.of(
                // Alice Johnson - C001
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(120.00).transactionDate(month1.plusDays(4)).build(),
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(75.50).transactionDate(month1.plusDays(14)).build(),
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(200.00).transactionDate(month2.plusDays(2)).build(),
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(45.00).transactionDate(month2.plusDays(18)).build(),
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(130.00).transactionDate(month3.plusDays(5)).build(),
                Transaction.builder().customerId("C001").customerName("Alice Johnson")
                        .amount(60.00).transactionDate(month3.plusDays(20)).build(),

                // Bob Martinez - C002
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(50.00).transactionDate(month1.plusDays(7)).build(),
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(110.00).transactionDate(month1.plusDays(21)).build(),
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(85.00).transactionDate(month2.plusDays(10)).build(),
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(155.75).transactionDate(month2.plusDays(25)).build(),
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(30.00).transactionDate(month3.plusDays(3)).build(),
                Transaction.builder().customerId("C002").customerName("Bob Martinez")
                        .amount(95.00).transactionDate(month3.plusDays(15)).build(),

                // Carol Smith - C003
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(300.00).transactionDate(month1.plusDays(1)).build(),
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(40.00).transactionDate(month1.plusDays(16)).build(),
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(100.00).transactionDate(month2.plusDays(8)).build(),
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(250.00).transactionDate(month2.plusDays(22)).build(),
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(75.00).transactionDate(month3.plusDays(9)).build(),
                Transaction.builder().customerId("C003").customerName("Carol Smith")
                        .amount(180.00).transactionDate(month3.plusDays(28)).build()
        );
    }
}
