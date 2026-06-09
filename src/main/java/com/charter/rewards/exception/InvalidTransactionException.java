package com.charter.rewards.exception;

// Custom exception for invalid transaction data like negative amounts or missing fields
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
