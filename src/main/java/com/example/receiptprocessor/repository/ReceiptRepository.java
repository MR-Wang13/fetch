package com.example.receiptprocessor.repository;

import com.example.receiptprocessor.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    Optional<Receipt> findByRetailerAndPurchaseDateAndPurchaseTime(String retailer, String purchaseDate, String purchaseTime);

}
