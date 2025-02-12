package com.example.receiptprocessor.service;

import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    public String storeReceipt(Receipt receipt) {
        for (Item item : receipt.getItems()) {
            item.setReceipt(receipt);
        }
        return receiptRepository.save(receipt).getId();
    }

    public int calculatePoints(String id) {
        Optional<Receipt> receiptOptional = receiptRepository.findById(id);
        if (receiptOptional.isEmpty()) {
            throw new ReceiptNotFoundException(id);
        }

        Receipt receipt = receiptOptional.get();
        int points = 0;
        logger.info("Starting point calculation for receipt [{}]", id);

        // 1. Retailer name: 1 point for every alphanumeric character.
        int retailerPoints = receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();
        points += retailerPoints;
        logger.info("Retailer '{}' has {} alphanumeric characters, adding {} points.",
                receipt.getRetailer(), retailerPoints, retailerPoints);

        // 2. Total is a round dollar amount: 50 points.
        BigDecimal total = new BigDecimal(receipt.getTotal());
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += 50;
            logger.info("Total {} is a round dollar amount, adding 50 points.", total);
        } else {
            logger.info("Total {} is not a round dollar amount, no points added for this rule.", total);
        }

        // 3. Total is a multiple of 0.25: 25 points.
        if (total.remainder(BigDecimal.valueOf(0.25)).compareTo(BigDecimal.ZERO) == 0) {
            points += 25;
            logger.info("Total {} is a multiple of 0.25, adding 25 points.", total);
        } else {
            logger.info("Total {} is not a multiple of 0.25, no points added for this rule.", total);
        }

        // 4. 5 points for every two items on the receipt.
        int itemPairPoints = (receipt.getItems().size() / 2) * 5;
        points += itemPairPoints;
        logger.info("Receipt has {} items, adding {} points for item pairs.", receipt.getItems().size(), itemPairPoints);

        // 5. For each item: if trimmed description length is a multiple of 3,
        //    multiply the price by 0.2 and round up to get additional points.
        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                BigDecimal itemPrice = new BigDecimal(item.getPrice());
                int bonus = (int) Math.ceil(itemPrice.multiply(BigDecimal.valueOf(0.2)).doubleValue());
                points += bonus;
                logger.info("Item description '{}' (length {}) qualifies: price {} * 0.2, bonus {} points.",
                        desc, desc.length(), itemPrice, bonus);
            } else {
                logger.info("Item description '{}' (length {}) does not qualify for bonus points.",
                        desc, desc.length());
            }
        }

        // 6. If the day in the purchase date is odd: add 6 points.
        int day = LocalDate.parse(receipt.getPurchaseDate()).getDayOfMonth();
        if (day % 2 == 1) {
            points += 6;
            logger.info("Purchase day {} is odd, adding 6 points.", day);
        } else {
            logger.info("Purchase day {} is even, no points added for this rule.", day);
        }

        // 7. If the purchase time is after 2:00pm and before 4:00pm: add 10 points.
        LocalTime time = LocalTime.parse(receipt.getPurchaseTime());
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) {
            points += 10;
            logger.info("Purchase time {} is between 2:00pm and 4:00pm, adding 10 points.", time);
        } else {
            logger.info("Purchase time {} is not between 2:00pm and 4:00pm, no points added for this rule.", time);
        }

        logger.info("Final points for receipt [{}]: {}", id, points);
        return points;
    }
}
