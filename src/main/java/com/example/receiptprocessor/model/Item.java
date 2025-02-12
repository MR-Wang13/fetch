package com.example.receiptprocessor.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    @NotBlank
    private String shortDescription;

    @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Price must be in 'xx.xx' format")
    private String price;
}
