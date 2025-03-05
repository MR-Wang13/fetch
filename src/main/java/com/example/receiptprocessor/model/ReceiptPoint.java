package com.example.receiptprocessor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "receipt_point")
public class ReceiptPoint {
    @Id
    private String receipt_id;

    @NotBlank
    private Integer points;

}
