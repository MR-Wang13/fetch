package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.util.TestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReceiptControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Receipt testReceipt;

    @BeforeEach
    void setUp() throws Exception {
        // Arrange: Load testReceipt using TestUtils (ensure the testReceipt.json file is available)
        testReceipt = TestUtils.loadJson("testReceipt.json", Receipt.class);
    }

    @Test
    void test_storeReceipt_WithValidReceipt_ReturnsExpectedReceiptIdResponse() throws Exception {
        // Arrange: Ensure that the testReceipt is correctly loaded

        // Act: Perform a POST request to /receipts/process with the test receipt
        MvcResult mvcResult = mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReceipt)))
                .andReturn();

        // Assert: Check that the HTTP status is 200 and the response contains a non-empty receipt id
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        String content = mvcResult.getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});
        Truth.assertThat(responseMap.get("id")).isNotEmpty();
    }

    @Test
    void test_calculatePoints_WithValidReceipt_ReturnsExpectedPointsResponse() throws Exception {
        // Arrange: First, store the receipt to obtain its id.
        MvcResult processResult = mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReceipt)))
                .andReturn();
        String processContent = processResult.getResponse().getContentAsString();
        Map<String, String> processResponseMap = objectMapper.readValue(processContent, new TypeReference<Map<String, String>>() {});
        String receiptId = processResponseMap.get("id");

        // Act: Perform a GET request to /receipts/{id}/points using the obtained receipt id.
        MvcResult mvcResult = mockMvc.perform(get("/receipts/" + receiptId + "/points"))
                .andReturn();

        // Assert: Verify that the HTTP status is 200 and the response contains the 'points' field.
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(200);

        String pointsContent = mvcResult.getResponse().getContentAsString();
        Map<String, Integer> pointsResponseMap = objectMapper.readValue(pointsContent, new TypeReference<Map<String, Integer>>() {});
        Truth.assertThat(pointsResponseMap.get("points")).isNotNull();
    }

    @Test
    void test_calculatePoints_WithInvalidReceiptId_ReturnsNotFoundErrorResponse() throws Exception {
        // Arrange: Define an invalid receipt id.
        String invalidId = "invalid-id";

        // Act: Perform a GET request with the invalid receipt id.
        MvcResult mvcResult = mockMvc.perform(get("/receipts/" + invalidId + "/points"))
                .andReturn();

        // Assert: Verify that the HTTP status is 404 and the error message matches expectations.
        int status = mvcResult.getResponse().getStatus();
        Truth.assertThat(status).isEqualTo(404);

        String content = mvcResult.getResponse().getContentAsString();
        Truth.assertThat(content).isEqualTo("Receipt with ID 'invalid-id' not found");
    }
}
