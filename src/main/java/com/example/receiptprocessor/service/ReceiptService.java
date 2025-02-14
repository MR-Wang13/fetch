package com.example.receiptprocessor.service;

import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class ReceiptService {

    private static final BigDecimal QUARTER = BigDecimal.valueOf(0.25);
    private static final int ROUND_DOLLAR_POINTS = 50;
    private static final int MULTIPLE_OF_QUARTER_POINTS = 25;


    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }
    @Transactional
    public String storeReceipt(Receipt receipt) {
        // check if existing
        Optional<Receipt> existingReceipt = receiptRepository.findByRetailerAndPurchaseDateAndPurchaseTime(
                receipt.getRetailer(), receipt.getPurchaseDate(), receipt.getPurchaseTime()
        );
        if (existingReceipt.isPresent()) {// if already existed, return existing id
            logger.warn("Duplicate receipt submission detected for retailer {} on {} {}. Returning existing id: {}",
                    receipt.getRetailer(), receipt.getPurchaseDate(), receipt.getPurchaseTime(), existingReceipt.get().getId());
            return existingReceipt.get().getId();
        }

        for (Item item : receipt.getItems()) {
            item.setReceipt(receipt);
        }
        return receiptRepository.save(receipt).getId();
    }

    public int calculatePoints(String id) {

        Receipt receipt = receiptRepository.findById(id).orElseThrow(() -> new ReceiptNotFoundException(id));
        int points = 0;
        logger.debug("Starting point calculation for receipt [{}]", id);

        // 1. Retailer name: 1 point for every alphanumeric character.
        int retailerPoints = receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();
        points += retailerPoints;
        logger.debug("Retailer '{}' has {} alphanumeric characters, adding {} points.",
                receipt.getRetailer(), retailerPoints, retailerPoints);

        // 2. Total is a round dollar amount: 50 points.
        BigDecimal total = new BigDecimal(receipt.getTotal());
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += ROUND_DOLLAR_POINTS;
            logger.debug("Total {} is a round dollar amount, adding 50 points.", total);
        } else {
            logger.debug("Total {} is not a round dollar amount, no points added for this rule.", total);
        }

        // 3. Total is a multiple of 0.25: 25 points.
        if (total.remainder(QUARTER).compareTo(BigDecimal.ZERO) == 0) {
            points += MULTIPLE_OF_QUARTER_POINTS;
            logger.debug("Total {} is a multiple of 0.25, adding 25 points.", total);
        } else {
            logger.debug("Total {} is not a multiple of 0.25, no points added for this rule.", total);
        }

        // 4. 5 points for every two items on the receipt.
        int itemPairPoints = (receipt.getItems().size() / 2) * 5;
        points += itemPairPoints;
        logger.debug("Receipt has {} items, adding {} points for item pairs.", receipt.getItems().size(), itemPairPoints);

        // 5. For each item: if trimmed description length is a multiple of 3,
        //    multiply the price by 0.2 and round up to get additional points.
        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                BigDecimal itemPrice = new BigDecimal(item.getPrice());
                int bonus = (int) Math.ceil(itemPrice.multiply(BigDecimal.valueOf(0.2)).doubleValue());
                points += bonus;
                logger.debug("Item description '{}' (length {}) qualifies: price {} * 0.2, bonus {} points.",
                        desc, desc.length(), itemPrice, bonus);
            } else {
                logger.debug("Item description '{}' (length {}) does not qualify for bonus points.",
                        desc, desc.length());
            }
        }

        // 6. If the day in the purchase date is odd: add 6 points.
        int day = LocalDate.parse(receipt.getPurchaseDate()).getDayOfMonth();
        if (day % 2 == 1) {
            points += 6;
            logger.debug("Purchase day {} is odd, adding 6 points.", day);
        } else {
            logger.debug("Purchase day {} is even, no points added for this rule.", day);
        }

        // 7. If the purchase time is after 2:00pm and before 4:00pm: add 10 points.
        LocalTime time = LocalTime.parse(receipt.getPurchaseTime());
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) {
            points += 10;
            logger.debug("Purchase time {} is between 2:00pm and 4:00pm, adding 10 points.", time);
        } else {
            logger.debug("Purchase time {} is not between 2:00pm and 4:00pm, no points added for this rule.", time);
        }

        logger.debug("Final points for receipt [{}]: {}", id, points);
        return points;
    }
}
