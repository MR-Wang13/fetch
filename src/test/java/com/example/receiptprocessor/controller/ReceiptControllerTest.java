package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.exception.GlobalExceptionHandler;
import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ReceiptControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private ReceiptController receiptController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Receipt testReceipt;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(receiptController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        InputStream is = getClass().getClassLoader().getResourceAsStream("testReceipt.json");
        testReceipt = objectMapper.readValue(is, Receipt.class);
    }

    @Test
    void testProcessReceipt() throws Exception {
        // Stub the service method to return a ReceiptIdResponse with "test-id"
        when(receiptService.storeReceipt(any(Receipt.class)))
                .thenReturn(new com.example.receiptprocessor.dto.ReceiptIdResponse("test-id"));

        MvcResult mvcResult = mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReceipt)))
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        // Parse the JSON response into a Map
        String content = mvcResult.getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});
        // Assert that the "id" field in the response equals "test-id"
        Truth.assertThat(responseMap.get("id")).isEqualTo("test-id");

        verify(receiptService, times(1)).storeReceipt(any(Receipt.class));
    }

    @Test
    void testGetPoints() throws Exception {
        // Stub the service method to return a PointsResponse with 32 points.
        when(receiptService.calculatePoints("test-id"))
                .thenReturn(new com.example.receiptprocessor.dto.PointsResponse(32));

        MvcResult mvcResult = mockMvc.perform(get("/receipts/test-id/points"))
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        String content = mvcResult.getResponse().getContentAsString();
        // Parse the response into a Map and assert that the "points" field equals 32.
        Map<String, Integer> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, Integer>>() {});
        Truth.assertThat(responseMap.get("points")).isEqualTo(32);

        verify(receiptService, times(1)).calculatePoints("test-id");
    }

    @Test
    void testGetPoints_ReceiptNotFound() throws Exception {
        // Stub the service method to throw ReceiptNotFoundException when the ID is "invalid-id"
        when(receiptService.calculatePoints("invalid-id"))
                .thenThrow(new ReceiptNotFoundException("invalid-id"));

        MvcResult mvcResult = mockMvc.perform(get("/receipts/invalid-id/points"))
                .andReturn();

        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(404);

        String content = mvcResult.getResponse().getContentAsString();
        Truth.assertThat(content).isEqualTo("Receipt with ID 'invalid-id' not found");

        verify(receiptService, times(1)).calculatePoints("invalid-id");
    }
}
