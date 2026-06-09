package com.charter.rewards.service;

import com.charter.rewards.exception.InvalidTransactionException;
import com.charter.rewards.model.CustomerRewardSummary;
import com.charter.rewards.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the RewardsCalculatorService
// Covers individual point calculation rules, boundary values and multi-customer aggregation
class RewardsCalculatorServiceTest {

    private RewardsCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new RewardsCalculatorService();
    }

    // --- calculatePoints tests ---

    @Test
    @DisplayName("Amount of $0 earns 0 points")
    void testZeroAmount() {
        assertEquals(0, service.calculatePoints(0));
    }

    @Test
    @DisplayName("Amount of $50 exactly earns 0 points")
    void testExactly50() {
        assertEquals(0, service.calculatePoints(50));
    }

    @Test
    @DisplayName("Amount of $50.01 earns 0 points (truncated integer math)")
    void testJustAbove50() {
        // (50.01 - 50) = 0.01, truncated to 0
        assertEquals(0, service.calculatePoints(50.01));
    }

    @Test
    @DisplayName("Amount of $51 earns 1 point")
    void testAmountOf51() {
        assertEquals(1, service.calculatePoints(51));
    }

    @Test
    @DisplayName("Amount of $75 earns 25 points")
    void testAmountOf75() {
        // (75 - 50) * 1 = 25
        assertEquals(25, service.calculatePoints(75));
    }

    @Test
    @DisplayName("Amount of $100 exactly earns 50 points")
    void testExactly100() {
        // (100 - 50) * 1 = 50
        assertEquals(50, service.calculatePoints(100));
    }

    @Test
    @DisplayName("Amount of $120 earns 90 points (assignment example)")
    void testAmountOf120() {
        // 2*(120-100) + 1*50 = 40 + 50 = 90
        assertEquals(90, service.calculatePoints(120));
    }

    @Test
    @DisplayName("Amount of $200 earns 250 points")
    void testAmountOf200() {
        // 2*(200-100) + 50 = 200 + 50 = 250
        assertEquals(250, service.calculatePoints(200));
    }

    @Test
    @DisplayName("Amount of $100.50 earns 51 points")
    void testJustAbove100() {
        // 2*(100.50-100) + 50 = 1 + 50 = 51
        assertEquals(51, service.calculatePoints(100.50));
    }

    @Test
    @DisplayName("Amount of $30 (below $50) earns 0 points")
    void testAmountBelow50() {
        assertEquals(0, service.calculatePoints(30));
    }

    @Test
    @DisplayName("Negative amount throws InvalidTransactionException")
    void testNegativeAmount() {
        InvalidTransactionException ex = assertThrows(
                InvalidTransactionException.class,
                () -> service.calculatePoints(-10));
        assertTrue(ex.getMessage().contains("negative"));
    }

    // --- calculateRewards aggregation tests ---

    @Test
    @DisplayName("Single customer, single transaction calculates correctly")
    void testSingleCustomerSingleTransaction() {
        List<Transaction> txns = List.of(
                Transaction.builder()
                        .customerId("C001").customerName("Alice")
                        .amount(120.0)
                        .transactionDate(LocalDate.of(2024, 1, 10))
                        .build()
        );

        List<CustomerRewardSummary> result = service.calculateRewards(txns);

        assertEquals(1, result.size());
        CustomerRewardSummary summary = result.get(0);
        assertEquals("C001", summary.getCustomerId());
        assertEquals(90, summary.getTotalPoints());
        assertEquals(1, summary.getMonthlyPoints().size());
        assertEquals(90, summary.getMonthlyPoints().get(0).getPoints());
        assertEquals("JANUARY", summary.getMonthlyPoints().get(0).getMonthName());
    }

    @Test
    @DisplayName("Single customer, multiple transactions in same month are summed")
    void testSingleCustomerMultipleTransactionsSameMonth() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 1, 5)).build(),
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(75.0).transactionDate(LocalDate.of(2024, 1, 20)).build()
        );
        // 120 gives 90 pts and 75 gives 25 pts, so total is 115
        List<CustomerRewardSummary> result = service.calculateRewards(txns);

        assertEquals(1, result.size());
        assertEquals(115, result.get(0).getTotalPoints());
        assertEquals(1, result.get(0).getMonthlyPoints().size());
        assertEquals(115, result.get(0).getMonthlyPoints().get(0).getPoints());
    }

    @Test
    @DisplayName("Single customer, transactions spread across three months")
    void testSingleCustomerThreeMonths() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 1, 10)).build(), // 90 pts
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(200.0).transactionDate(LocalDate.of(2024, 2, 15)).build(), // 250 pts
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(50.0).transactionDate(LocalDate.of(2024, 3, 20)).build()   // 0 pts
        );

        List<CustomerRewardSummary> result = service.calculateRewards(txns);

        assertEquals(1, result.size());
        CustomerRewardSummary summary = result.get(0);
        assertEquals(3, summary.getMonthlyPoints().size());
        assertEquals(340, summary.getTotalPoints()); // 90 + 250 + 0

        assertEquals(90,  summary.getMonthlyPoints().get(0).getPoints()); // Jan
        assertEquals(250, summary.getMonthlyPoints().get(1).getPoints()); // Feb
        assertEquals(0,   summary.getMonthlyPoints().get(2).getPoints()); // Mar
    }

    @Test
    @DisplayName("Multiple customers are each calculated independently")
    void testMultipleCustomers() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 1, 5)).build(),  // 90
                Transaction.builder().customerId("C002").customerName("Bob")
                        .amount(200.0).transactionDate(LocalDate.of(2024, 1, 8)).build(),  // 250
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(75.0).transactionDate(LocalDate.of(2024, 2, 10)).build(),  // 25
                Transaction.builder().customerId("C002").customerName("Bob")
                        .amount(30.0).transactionDate(LocalDate.of(2024, 2, 12)).build()   // 0
        );

        List<CustomerRewardSummary> result = service.calculateRewards(txns);
        assertEquals(2, result.size());

        CustomerRewardSummary alice = result.stream()
                .filter(s -> s.getCustomerId().equals("C001")).findFirst().orElseThrow();
        CustomerRewardSummary bob = result.stream()
                .filter(s -> s.getCustomerId().equals("C002")).findFirst().orElseThrow();

        assertEquals(115, alice.getTotalPoints());  // 90 + 25
        assertEquals(250, bob.getTotalPoints());    // 250 + 0
    }

    @Test
    @DisplayName("Transaction with amount exactly $0 earns 0 points and does not throw")
    void testZeroAmountTransaction() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(0.0).transactionDate(LocalDate.of(2024, 1, 1)).build()
        );
        List<CustomerRewardSummary> result = service.calculateRewards(txns);
        assertEquals(0, result.get(0).getTotalPoints());
    }

    @Test
    @DisplayName("Null transactions list throws IllegalArgumentException")
    void testNullTransactionsList() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateRewards(null));
    }

    @Test
    @DisplayName("Transaction with null customerId throws InvalidTransactionException")
    void testNullCustomerId() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId(null).customerName("Alice")
                        .amount(100.0).transactionDate(LocalDate.of(2024, 1, 1)).build()
        );
        assertThrows(InvalidTransactionException.class, () -> service.calculateRewards(txns));
    }

    @Test
    @DisplayName("Transaction with null date throws InvalidTransactionException")
    void testNullTransactionDate() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(100.0).transactionDate(null).build()
        );
        assertThrows(InvalidTransactionException.class, () -> service.calculateRewards(txns));
    }

    @Test
    @DisplayName("Transaction with negative amount throws InvalidTransactionException")
    void testNegativeAmountInList() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(-50.0).transactionDate(LocalDate.of(2024, 1, 1)).build()
        );
        assertThrows(InvalidTransactionException.class, () -> service.calculateRewards(txns));
    }

    @Test
    @DisplayName("Monthly breakdown is sorted chronologically")
    void testMonthlyBreakdownOrder() {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 3, 1)).build(),
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 1, 1)).build(),
                Transaction.builder().customerId("C001").customerName("Alice")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 2, 1)).build()
        );

        List<CustomerRewardSummary> result = service.calculateRewards(txns);
        List<String> months = result.get(0).getMonthlyPoints()
                .stream().map(m -> m.getMonthName()).toList();

        assertEquals(List.of("JANUARY", "FEBRUARY", "MARCH"), months);
    }
}
