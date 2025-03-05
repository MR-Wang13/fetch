package com.example.receiptprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BonusCalculationRequest {
    private long receiptCount;
}
