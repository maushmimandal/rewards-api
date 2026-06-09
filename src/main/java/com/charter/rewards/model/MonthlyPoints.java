package com.charter.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Holds reward points earned by a customer for a particular month
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPoints {

    // year like 2024
    private int year;

    // month number, 1 for January and 12 for December
    private int month;

    // month name like JANUARY, FEBRUARY etc
    private String monthName;

    // total points earned in this month
    private long points;
}
