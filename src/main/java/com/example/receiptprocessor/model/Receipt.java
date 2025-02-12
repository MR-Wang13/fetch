package com.example.receiptprocessor.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Receipt {
    @NotBlank
    private String retailer;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid date format (YYYY-MM-DD)")
    private String purchaseDate;

    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Invalid time format (HH:MM)")
    private String purchaseTime;

    @NotNull
    @Size(min = 1, message = "At least one item is required")
    private List<Item> items;

    @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Total must be in 'xx.xx' format")
    private String total;
}
