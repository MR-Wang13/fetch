package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.exception.GlobalExceptionHandler;
import com.example.receiptprocessor.exception.ReceiptNotFoundException;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import com.example.receiptprocessor.util.TestUtils;
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

        // Arrange: Load testReceipt using the utility class
        testReceipt = TestUtils.loadJson("testReceipt.json", Receipt.class);
    }

    @Test
    void test_storeReceipt_WithValidReceipt_ReturnsExpectedReceiptIdResponse() throws Exception {
        // Arrange: Stub the service method to return a ReceiptIdResponse with "test-id"
        when(receiptService.storeReceipt(any(Receipt.class)))
                .thenReturn(new ReceiptIdResponse("test-id"));

        // Act: Perform a POST request to the /receipts/process endpoint
        MvcResult mvcResult = mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReceipt)))
                .andReturn();

        // Assert: Verify the HTTP status is 200 and the response JSON contains the expected "id"
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        String content = mvcResult.getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});
        Truth.assertThat(responseMap.get("id")).isEqualTo("test-id");

        // Verify: Ensure the service method was called exactly once
        verify(receiptService, times(1)).storeReceipt(any(Receipt.class));
    }

    @Test
    void test_calculatePoints_WithValidReceipt_ReturnsExpectedPointsResponse() throws Exception {
        // Arrange: Stub the service method to return a PointsResponse with 32 points.
        when(receiptService.calculatePoints("test-id"))
                .thenReturn(new PointsResponse(32));

        // Act: Perform a GET request to the /receipts/test-id/points endpoint
        MvcResult mvcResult = mockMvc.perform(get("/receipts/test-id/points"))
                .andReturn();

        // Assert: Verify the HTTP status and that the JSON response has "points" equal to 32.
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Integer> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, Integer>>() {});
        Truth.assertThat(responseMap.get("points")).isEqualTo(32);

        // Verify: Ensure the service method was called exactly once
        verify(receiptService, times(1)).calculatePoints("test-id");
    }

    @Test
    void test_calculatePoints_WithInvalidReceiptId_ReturnsNotFoundErrorResponse() throws Exception {
        // Arrange: Stub the service method to throw ReceiptNotFoundException for "invalid-id"
        when(receiptService.calculatePoints("invalid-id"))
                .thenThrow(new ReceiptNotFoundException("invalid-id"));

        // Act: Perform a GET request to the /receipts/invalid-id/points endpoint
        MvcResult mvcResult = mockMvc.perform(get("/receipts/invalid-id/points"))
                .andReturn();

        // Assert: Verify the HTTP status is 404 and the error message matches expectations
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(404);

        String content = mvcResult.getResponse().getContentAsString();
        Truth.assertThat(content).isEqualTo("Receipt with ID 'invalid-id' not found");

        // Verify: Ensure the service method was called exactly once
        verify(receiptService, times(1)).calculatePoints("invalid-id");
    }
}
