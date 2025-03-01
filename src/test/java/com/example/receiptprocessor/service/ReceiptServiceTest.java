package com.example.receiptprocessor.service;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import com.example.receiptprocessor.util.TestUtils;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional // Ensures that each test runs in isolation
class ReceiptServiceTest {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ReceiptService receiptService;
    
    @Test
    void test_storeReceipt_WithValidReceipt_ReturnsReceiptIdResponse() throws Exception {
        // Arrange: Load test receipt from JSON
        Receipt testReceipt = TestUtils.loadJson("testReceipt.json", Receipt.class);

        // Act: Store the receipt using the service
        ReceiptIdResponse response = receiptService.storeReceipt(testReceipt);

        // Assert: Verify that the receipt ID is not null and the receipt is persisted
        Truth.assertThat(response.getId()).isNotNull();
        Optional<Receipt> storedReceipt = receiptRepository.findById(response.getId());
        Truth.assertThat(storedReceipt.isPresent()).isTrue();
    }

    @Test
    void test_storeReceipt_DuplicateSubmission_ReturnsSameId() throws Exception {
        // Arrange: Load test receipt from JSON
        Receipt testReceipt = TestUtils.loadJson("testReceipt.json", Receipt.class);

        // Act: Store the receipt twice to simulate a duplicate submission
        ReceiptIdResponse response1 = receiptService.storeReceipt(testReceipt);
        ReceiptIdResponse response2 = receiptService.storeReceipt(testReceipt);

        // Assert: Verify that both submissions return the same receipt ID
        Truth.assertThat(response2.getId()).isEqualTo(response1.getId());
    }

    @Test
    void test_calculatePoints_WithInvalidReceiptId_ThrowsReceiptNotFoundException() {
        // Arrange: Use an invalid receipt ID "invalid-id"

        // Act & Assert: Verify that calculating points with an invalid ID throws the expected exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            receiptService.calculatePoints("invalid-id");
        });
        Truth.assertThat(exception).hasMessageThat().isEqualTo("Receipt with ID 'invalid-id' not found");
    }

    @Test
    void test_calculatePoints_MeetAllRules_ReturnExpectedResult() throws Exception {
        // Arrange: Load a receipt that meets all rules from JSON
        Receipt receipt = TestUtils.loadJson("testReceipt_AllRules.json", Receipt.class);

        // Act: Store the receipt and then calculate points
        ReceiptIdResponse response = receiptService.storeReceipt(receipt);
        PointsResponse pointsResponse = receiptService.calculatePoints(response.getId());

        // Assert: Verify that the calculated points equal the expected result (113)
        Truth.assertThat(pointsResponse.getPoints()).isEqualTo(113);
    }

    @Test
    void test_calculatePoints_NoTimeBonus_ReturnExpectedResult() throws Exception {
        // Arrange: Load a receipt with no time bonus from JSON
        Receipt receipt = TestUtils.loadJson("testReceipt_NoTimeBonus.json", Receipt.class);

        // Act: Store the receipt and then calculate points
        ReceiptIdResponse response = receiptService.storeReceipt(receipt);
        PointsResponse pointsResponse = receiptService.calculatePoints(response.getId());

        // Assert: Verify that the calculated points equal the expected result (20)
        Truth.assertThat(pointsResponse.getPoints()).isEqualTo(20);
    }
}
