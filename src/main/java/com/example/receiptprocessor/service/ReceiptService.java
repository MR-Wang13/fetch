package com.example.receiptprocessor.service;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import com.example.receiptprocessor.util.PointsCalculator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);
    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    /**
     * Stores the receipt and returns a response object containing the receipt ID.
     * If a duplicate receipt exists (based on retailer, purchaseDate, and purchaseTime),
     * the existing receipt's ID is returned.
     */
    @Transactional
    public ReceiptIdResponse storeReceipt(Receipt receipt) {
        // Check if a receipt with the same retailer, purchaseDate, and purchaseTime already exists.
        Optional<Receipt> existingReceipt = receiptRepository.findByRetailerAndPurchaseDateAndPurchaseTime(
                receipt.getRetailer(), receipt.getPurchaseDate(), receipt.getPurchaseTime()
        );
        if (existingReceipt.isPresent()) {
            logger.warn("Duplicate receipt submission detected for retailer {} on {} {}. Returning existing id: {}",
                    receipt.getRetailer(), receipt.getPurchaseDate(), receipt.getPurchaseTime(), existingReceipt.get().getId());
            return new ReceiptIdResponse(existingReceipt.get().getId());
        }

        // Set up bi-directional relationship: assign the receipt to each item.
        for (Item item : receipt.getItems()) {
            item.setReceipt(receipt);
        }
        String id = receiptRepository.save(receipt).getId();
        return new ReceiptIdResponse(id);
    }

    /**
     * Calculates points for a given receipt ID and returns a response object containing the points.
     *
     */
    public PointsResponse calculatePoints(String id) {
        // Retrieve the receipt by ID or throw an exception if not found.
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));
        logger.debug("Starting point calculation for receipt [{}]", id);

        // Calculate points using the business logic encapsulated in PointsCalculator.
        int points = PointsCalculator.calculatePoints(receipt);

        logger.debug("Final points for receipt [{}]: {}", id, points);
        return new PointsResponse(points);
    }
}
