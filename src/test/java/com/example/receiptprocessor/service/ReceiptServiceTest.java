package com.example.receiptprocessor.service;

import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private Receipt testReceipt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testReceipt = new Receipt();
        testReceipt.setRetailer("Target");
        testReceipt.setPurchaseDate("2022-01-01");
        testReceipt.setPurchaseTime("13:01");
        testReceipt.setTotal("35.35");

        Item item1 = new Item();
        item1.setShortDescription("Mountain Dew 12PK");
        item1.setPrice("6.49");

        Item item2 = new Item();
        item2.setShortDescription("Emils Cheese Pizza");
        item2.setPrice("12.25");

        testReceipt.setItems(List.of(item1, item2));
    }

    @Test
    void testStoreReceipt() {
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);

        String receiptId = receiptService.storeReceipt(testReceipt);

        assertNotNull(receiptId);
        verify(receiptRepository, times(1)).save(testReceipt);
    }

    @Test
    void testCalculatePoints() {
        when(receiptRepository.findById(anyString())).thenReturn(Optional.of(testReceipt));

        int points = receiptService.calculatePoints("test-id");

        assertTrue(points > 0, "Points should be greater than zero");
        verify(receiptRepository, times(1)).findById("test-id");
    }

    @Test
    void testCalculatePoints_ReceiptNotFound() {
        when(receiptRepository.findById("invalid-id")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            receiptService.calculatePoints("invalid-id");
        });

        assertEquals("Receipt not found", exception.getMessage());
    }
}
