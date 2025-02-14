package com.example.receiptprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for returning a receipt ID.
 */
@Data
@AllArgsConstructor
public class ReceiptIdResponse {
    private String id;

}
