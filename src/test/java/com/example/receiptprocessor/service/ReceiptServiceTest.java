package com.example.receiptprocessor.service;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    // Helper method to load a Receipt object from a JSON file located in src/test/resources.
    private Receipt loadReceiptFromJson(String filename) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        return objectMapper.readValue(is, Receipt.class);
    }

    @Test
    void testStoreReceipt() throws Exception {
        Receipt testReceipt = loadReceiptFromJson("testReceipt.json");
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);

        // Now the storeReceipt method returns a ReceiptIdResponse.
        ReceiptIdResponse response = receiptService.storeReceipt(testReceipt);

        Truth.assertThat(response.getId()).isNotNull();
        verify(receiptRepository, times(1)).save(testReceipt);
    }

    @Test
    void testCalculatePoints() throws Exception {
        Receipt testReceipt = loadReceiptFromJson("testReceipt.json");
        when(receiptRepository.findById(anyString())).thenReturn(Optional.of(testReceipt));

        // Now calculatePoints returns a PointsResponse.
        PointsResponse response = receiptService.calculatePoints("test-id");

        Truth.assertThat(response.getPoints()).isGreaterThan(0);
        verify(receiptRepository, times(1)).findById("test-id");
    }

    @Test
    void testCalculatePoints_ReceiptNotFound() throws Exception {
        when(receiptRepository.findById("invalid-id")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            receiptService.calculatePoints("invalid-id");
        });

        Truth.assertThat(exception).hasMessageThat().isEqualTo("Receipt with ID 'invalid-id' not found");
    }

    @Test
    void testCalculatePoints_AllRules() throws Exception {
        Receipt receipt = loadReceiptFromJson("testReceipt_AllRules.json");
        when(receiptRepository.findById("all-rules-id")).thenReturn(Optional.of(receipt));

        PointsResponse response = receiptService.calculatePoints("all-rules-id");
        Truth.assertThat(response.getPoints()).isEqualTo(113);
        verify(receiptRepository, times(1)).findById("all-rules-id");
    }

    @Test
    void testCalculatePoints_NoTimeBonus() throws Exception {
        Receipt receipt = loadReceiptFromJson("testReceipt_NoTimeBonus.json");
        when(receiptRepository.findById("no-time-bonus-id")).thenReturn(Optional.of(receipt));

        PointsResponse response = receiptService.calculatePoints("no-time-bonus-id");
        Truth.assertThat(response.getPoints()).isEqualTo(20);
        verify(receiptRepository, times(1)).findById("no-time-bonus-id");
    }
}
