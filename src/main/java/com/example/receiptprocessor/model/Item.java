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

    @ManyToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @NotBlank
    @Pattern(regexp = "^[\\w\\s\\-]+$", message = "Short description must only contain alphanumeric characters, spaces, and hyphens.")
    private String shortDescription;

    @NotBlank
    @Pattern(regexp = "^\\d+\\.\\d{2}$", message = "Price must be in 'xx.xx' format")
    private String price;


}
