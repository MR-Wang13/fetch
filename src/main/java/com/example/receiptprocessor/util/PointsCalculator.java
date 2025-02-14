package com.example.receiptprocessor.util;

import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class PointsCalculator {

    private static final BigDecimal QUARTER = BigDecimal.valueOf(0.25);
    private static final int ROUND_DOLLAR_POINTS = 50;
    private static final int MULTIPLE_OF_QUARTER_POINTS = 25;

    /**
     * Pure function that calculates points from a given Receipt.
     *
     * @param receipt the receipt object
     * @return the calculated points
     */
    public static int calculatePoints(Receipt receipt) {
        int points = 0;

        // 1. Retailer name: 1 point for every alphanumeric character.
        int retailerPoints = receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();
        points += retailerPoints;

        // 2. Total is a round dollar amount: 50 points.
        BigDecimal total = new BigDecimal(receipt.getTotal());
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += ROUND_DOLLAR_POINTS;
        }

        // 3. Total is a multiple of 0.25: 25 points.
        if (total.remainder(QUARTER).compareTo(BigDecimal.ZERO) == 0) {
            points += MULTIPLE_OF_QUARTER_POINTS;
        }

        // 4. 5 points for every two items on the receipt.
        int itemPairPoints = (receipt.getItems().size() / 2) * 5;
        points += itemPairPoints;

        // 5. For each item: if trimmed description length is a multiple of 3,
        //    multiply the price by 0.2 and round up to get additional points.
        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                BigDecimal itemPrice = new BigDecimal(item.getPrice());
                int bonus = (int) Math.ceil(itemPrice.multiply(BigDecimal.valueOf(0.2)).doubleValue());
                points += bonus;
            }
        }

        // 6. If the day in the purchase date is odd: add 6 points.
        int day = LocalDate.parse(receipt.getPurchaseDate()).getDayOfMonth();
        if (day % 2 == 1) {
            points += 6;
        }

        // 7. If the purchase time is after 2:00pm and before 4:00pm: add 10 points.
        LocalTime time = LocalTime.parse(receipt.getPurchaseTime());
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) {
            points += 10;
        }

        return points;
    }
}
