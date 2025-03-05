package com.example.receiptprocessor.dto;


import com.example.receiptprocessor.model.Receipt;
import lombok.Data;

@Data
public class PointsCalculationRequest {
    private Receipt receipt;
    private BonusCalculationRequest bonusCalculationRequest;
}
