package com.example.receiptprocessor.repository;

import com.example.receiptprocessor.model.ReceiptPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptPointRepository extends JpaRepository<ReceiptPoint, String> {
}
