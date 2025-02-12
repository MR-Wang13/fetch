package com.example.receiptprocessor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @NotBlank
    private String shortDescription;

    @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Price must be in 'xx.xx' format")
    private String price;
}
