package com.example.receiptprocessor.service;

import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReceiptService {

    private final Map<String, Receipt> receiptStorage = new ConcurrentHashMap<>();

    public String storeReceipt(Receipt receipt) {
        String id = UUID.randomUUID().toString();
        receiptStorage.put(id, receipt);
        return id;
    }

    public int calculatePoints(String id) {
        Receipt receipt = receiptStorage.get(id);
        if (receipt == null) {
            throw new RuntimeException("Receipt not found");
        }

        int points = 0;
        points += receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();

        BigDecimal total = new BigDecimal(receipt.getTotal());
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += 50;
        }
        if (total.remainder(BigDecimal.valueOf(0.25)).compareTo(BigDecimal.ZERO) == 0) {
            points += 25;
        }

        points += (receipt.getItems().size() / 2) * 5;

        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                BigDecimal itemPrice = new BigDecimal(item.getPrice());
                points += (int) Math.ceil(itemPrice.multiply(BigDecimal.valueOf(0.2)).doubleValue());
            }
        }

        int day = LocalDate.parse(receipt.getPurchaseDate()).getDayOfMonth();
        if (day % 2 == 1) {
            points += 6;
        }

        LocalTime time = LocalTime.parse(receipt.getPurchaseTime());
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) {
            points += 10;
        }

        return points;
    }
}
