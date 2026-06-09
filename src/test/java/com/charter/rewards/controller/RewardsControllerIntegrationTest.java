package com.charter.rewards.controller;

import com.charter.rewards.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integration tests for the RewardsController
// Spins up the full Spring context and tests the actual HTTP layer using MockMvc
@SpringBootTest
@AutoConfigureMockMvc
class RewardsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // --- GET /api/rewards tests ---

    @Test
    @DisplayName("GET /api/rewards returns HTTP 200 with sample data for all customers")
    void testGetRewardsReturnsOk() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCustomers").value(3))
                .andExpect(jsonPath("$.customerRewards", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/rewards response contains expected customer IDs")
    void testGetRewardsContainsCustomerIds() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRewards[*].customerId",
                        containsInAnyOrder("C001", "C002", "C003")));
    }

    @Test
    @DisplayName("GET /api/rewards each customer has monthly breakdown and positive total")
    void testGetRewardsHasMonthlyBreakdown() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRewards[0].monthlyPoints", not(empty())))
                .andExpect(jsonPath("$.customerRewards[0].totalPoints", greaterThanOrEqualTo(0)));
    }

    // --- POST /api/rewards/calculate tests ---

    @Test
    @DisplayName("POST /api/rewards/calculate returns correct points for $120 purchase")
    void testCalculateRewards120() throws Exception {
        List<Transaction> txns = List.of(
                Transaction.builder()
                        .customerId("T001").customerName("Test User")
                        .amount(120.0)
                        .transactionDate(LocalDate.of(2024, 1, 15))
                        .build()
        );

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txns)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.customerRewards[0].totalPoints").value(90));
    }

    @Test
    @DisplayName("POST /api/rewards/calculate handles multiple customers correctly")
    void testCalculateMultipleCustomers() throws Exception {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("A001").customerName("Alice")
                        .amount(200.0).transactionDate(LocalDate.of(2024, 1, 10)).build(),
                Transaction.builder().customerId("B001").customerName("Bob")
                        .amount(75.0).transactionDate(LocalDate.of(2024, 1, 12)).build(),
                Transaction.builder().customerId("A001").customerName("Alice")
                        .amount(110.0).transactionDate(LocalDate.of(2024, 2, 5)).build()
        );
        // Alice: 250 (Jan) + 70 (Feb) = 320, Bob: 25

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txns)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2))
                .andExpect(jsonPath("$.customerRewards[?(@.customerId=='A001')].totalPoints",
                        contains(320)))
                .andExpect(jsonPath("$.customerRewards[?(@.customerId=='B001')].totalPoints",
                        contains(25)));
    }

    @Test
    @DisplayName("POST /api/rewards/calculate returns 0 points for amount at or below $50")
    void testCalculateZeroPointsBelow50() throws Exception {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Carol")
                        .amount(50.0).transactionDate(LocalDate.of(2024, 3, 1)).build()
        );

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txns)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRewards[0].totalPoints").value(0));
    }

    @Test
    @DisplayName("POST /api/rewards/calculate returns 400 for transaction with negative amount")
    void testCalculateNegativeAmountReturnsBadRequest() throws Exception {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("C001").customerName("Carol")
                        .amount(-20.0).transactionDate(LocalDate.of(2024, 3, 1)).build()
        );

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txns)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("negative")));
    }

    @Test
    @DisplayName("POST /api/rewards/calculate returns 400 for transaction with null customerId")
    void testCalculateNullCustomerIdReturnsBadRequest() throws Exception {
        // passing null customerId to make sure validation catches it
        String body = "[{\"customerId\":null,\"customerName\":\"Ghost\",\"amount\":100.0,\"transactionDate\":\"2024-01-01\"}]";

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rewards/calculate handles customer with transactions across three months")
    void testCalculateThreeMonthBreakdown() throws Exception {
        List<Transaction> txns = List.of(
                Transaction.builder().customerId("D001").customerName("Diana")
                        .amount(120.0).transactionDate(LocalDate.of(2024, 1, 5)).build(),  // 90
                Transaction.builder().customerId("D001").customerName("Diana")
                        .amount(200.0).transactionDate(LocalDate.of(2024, 2, 10)).build(), // 250
                Transaction.builder().customerId("D001").customerName("Diana")
                        .amount(60.0).transactionDate(LocalDate.of(2024, 3, 20)).build()   // 10
        );

        mockMvc.perform(post("/api/rewards/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txns)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRewards[0].totalPoints").value(350))
                .andExpect(jsonPath("$.customerRewards[0].monthlyPoints", hasSize(3)));
    }
}
