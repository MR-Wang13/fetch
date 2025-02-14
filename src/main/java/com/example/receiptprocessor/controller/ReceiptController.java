package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/process")
    public ResponseEntity<ReceiptIdResponse> processReceipt(@Valid @RequestBody Receipt receipt) {
        return ResponseEntity.ok(receiptService.storeReceipt(receipt));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<PointsResponse> getPoints(@PathVariable String id) {
        return ResponseEntity.ok(receiptService.calculatePoints(id));
    }
}
