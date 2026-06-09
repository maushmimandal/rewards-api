# Rewards API

This is a Spring Boot REST API built for the Charter coding assignment. It calculates reward points earned by customers based on their purchase transactions over a three month period.

---

## What it does

A retailer runs a rewards program where customers earn points on every purchase:

- 2 points for every dollar spent above $100 in a single transaction
- 1 point for every dollar spent between $50 and $100
- No points for purchases at or below $50

For example, a $120 purchase gives you 2 x $20 + 1 x $50 = 90 points.

The API takes a list of transactions, groups them by customer and month, and returns how many points each customer earned per month along with the overall total.

---

## Project structure

```
rewards-api/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/charter/rewards/
    │   │   ├── RewardsApiApplication.java          entry point
    │   │   ├── controller/
    │   │   │   └── RewardsController.java           REST endpoints
    │   │   ├── data/
    │   │   │   └── TransactionDataLoader.java       sample dataset
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java      handles all errors
    │   │   │   └── InvalidTransactionException.java custom exception
    │   │   ├── model/
    │   │   │   ├── CustomerRewardSummary.java        per customer result
    │   │   │   ├── MonthlyPoints.java                per month breakdown
    │   │   │   ├── RewardsResponse.java              top level response
    │   │   │   └── Transaction.java                  input model
    │   │   └── service/
    │   │       └── RewardsCalculatorService.java     core logic
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/charter/rewards/
            ├── controller/
            │   └── RewardsControllerIntegrationTest.java
            └── service/
                └── RewardsCalculatorServiceTest.java
```

---

## Tech stack

- Java 17
- Spring Boot 3.2.4
- Lombok
- JUnit 5
- Maven

---

## How to run

Make sure you have Java 17 and Maven installed.

```
mvn clean install
mvn spring-boot:run
```

The server starts on http://localhost:8081

To just run the tests:

```
mvn test
```

---

## API endpoints

### GET /api/rewards

Returns reward points for all customers using the built-in sample dataset. The dataset has 3 customers with transactions across the last 3 months.

Example response:

```json
{
  "totalCustomers": 3,
  "customerRewards": [
    {
      "customerId": "C001",
      "customerName": "Alice Johnson",
      "monthlyPoints": [
        {
          "year": 2026,
          "month": 4,
          "monthName": "APRIL",
          "points": 115
        },
        {
          "year": 2026,
          "month": 5,
          "monthName": "MAY",
          "points": 310
        },
        {
          "year": 2026,
          "month": 6,
          "monthName": "JUNE",
          "points": 170
        }
      ],
      "totalPoints": 595
    }
  ]
}
```

---

### POST /api/rewards/calculate

Use this if you want to pass in your own list of transactions instead of using the sample data.

Request body should be a JSON array of transactions:

```json
[
  {
    "customerId": "C001",
    "customerName": "Alice Johnson",
    "amount": 120.0,
    "transactionDate": "2024-01-15"
  },
  {
    "customerId": "C002",
    "customerName": "Bob Martinez",
    "amount": 85.0,
    "transactionDate": "2024-01-20"
  }
]
```

Response format is the same as the GET endpoint above.

---

## Error handling

If something goes wrong, the API returns a JSON error body instead of crashing:

```json
{
  "timestamp": "2026-06-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Transaction amount cannot be negative. Received: -20.0"
}
```

Common errors:

| Situation | HTTP status |
|-----------|-------------|
| Negative transaction amount | 400 |
| Missing or null customer ID | 400 |
| Null date on a transaction | 400 |
| Null transactions list | 400 |
| Something unexpected breaks | 500 |

---

## How points are calculated

The logic is straightforward:

```
if amount > 100:
    points = (amount - 100) * 2 + 50
else if amount > 50:
    points = (amount - 50) * 1
else:
    points = 0
```

Decimal amounts are truncated to whole dollars before calculation.

---

## Sample data

The built-in dataset has 3 customers, each with 6 transactions spread across 3 months. The months are calculated dynamically from the current date so you always see recent month names.

| Customer | ID |
|----------|----|
| Alice Johnson | C001 |
| Bob Martinez | C002 |
| Carol Smith | C003 |

---

## Notes

- No database used, everything runs in memory
- Months are never hardcoded, always derived from LocalDate.now()
- The POST endpoint lets you test with any custom data you want
