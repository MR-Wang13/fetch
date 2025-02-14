package com.example.receiptprocessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for returning calculated points.
 */
@Data
@AllArgsConstructor
public class PointsResponse {
    private int points;
}
