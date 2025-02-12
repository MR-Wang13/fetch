package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.exception.GlobalExceptionHandler;
import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Item;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReceiptControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceiptService receiptService;
    @InjectMocks
    private ReceiptController receiptController;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Receipt testReceipt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


        mockMvc = MockMvcBuilders.standaloneSetup(receiptController).setControllerAdvice(new GlobalExceptionHandler()).build();

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
    void testProcessReceipt() throws Exception {
        when(receiptService.storeReceipt(any(Receipt.class))).thenReturn("test-id");

        mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReceipt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"));

        verify(receiptService, times(1)).storeReceipt(any(Receipt.class));
    }

    @Test
    void testGetPoints() throws Exception {
        when(receiptService.calculatePoints("test-id")).thenReturn(32);

        mockMvc.perform(get("/receipts/test-id/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(32));

        verify(receiptService, times(1)).calculatePoints("test-id");
    }

    @Test
    void testGetPoints_ReceiptNotFound() throws Exception {
        when(receiptService.calculatePoints("invalid-id")).thenThrow(new ReceiptNotFoundException("invalid-id"));

        mockMvc.perform(get("/receipts/invalid-id/points"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Receipt with ID 'invalid-id' not found"));

        verify(receiptService, times(1)).calculatePoints("invalid-id");
    }
}
