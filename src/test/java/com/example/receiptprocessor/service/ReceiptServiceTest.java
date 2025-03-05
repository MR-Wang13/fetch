package com.example.receiptprocessor.service;

import com.example.receiptprocessor.dto.PointsResponse;
import com.example.receiptprocessor.dto.ReceiptIdResponse;
import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.repository.ReceiptRepository;
import com.example.receiptprocessor.util.PointsCalculator;
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
        Truth.assertThat(pointsResponse.getPoints()).isEqualTo(1113);
    }

    @Test
    void test_calculatePoints_NoTimeBonus_ReturnExpectedResult() throws Exception {
        // Arrange: Load a receipt with no time bonus from JSON
        Receipt receipt = TestUtils.loadJson("testReceipt_NoTimeBonus.json", Receipt.class);

        // Act: Store the receipt and then calculate points
        ReceiptIdResponse response = receiptService.storeReceipt(receipt);
        PointsResponse pointsResponse = receiptService.calculatePoints(response.getId());

        // Assert: Verify that the calculated points equal the expected result (20)
        Truth.assertThat(pointsResponse.getPoints()).isEqualTo(1020);
    }

    @Test
    void test_firstReceiptBonus() throws Exception {
        // Arrange: Load a test receipt and modify fields to ensure uniqueness.
        Receipt receipt = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt.setUserId("testUserBonus1");
        receipt.setRetailer("UniqueRetailer1");
        receipt.setPurchaseDate("2025-03-05");
        receipt.setPurchaseTime("12:00");

        // Act: Store the receipt and retrieve its points.
        ReceiptIdResponse response = receiptService.storeReceipt(receipt);
        PointsResponse pointsResponse = receiptService.calculatePoints(response.getId());

        // Calculate expected points: base points + 1000 bonus for the first receipt.
        int basePoints = PointsCalculator.calculateBasePoints(receipt);
        int expectedPoints = basePoints + 1000;

        // Assert:
        Truth.assertThat(pointsResponse.getPoints()).isEqualTo(expectedPoints);
    }

    @Test
    void test_secondReceiptBonus() throws Exception {
        // Arrange: Create two unique receipts for the same user.
        // First receipt
        Receipt receipt1 = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt1.setUserId("testUserBonus2");
        receipt1.setRetailer("UniqueRetailer2-1");
        receipt1.setPurchaseDate("2025-03-05");
        receipt1.setPurchaseTime("12:00");
        receiptService.storeReceipt(receipt1);

        // Second receipt
        Receipt receipt2 = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt2.setUserId("testUserBonus2");
        receipt2.setRetailer("UniqueRetailer2-2"); // ensure uniqueness
        receipt2.setPurchaseDate("2025-03-06");
        receipt2.setPurchaseTime("13:00");
        ReceiptIdResponse response2 = receiptService.storeReceipt(receipt2);

        // Act: Get points for the second receipt.
        PointsResponse pointsResponse2 = receiptService.calculatePoints(response2.getId());

        // Expected bonus for the second receipt is 500.
        int basePoints = PointsCalculator.calculateBasePoints(receipt2);
        int expectedPoints = basePoints + 500;

        // Assert:
        Truth.assertThat(pointsResponse2.getPoints()).isEqualTo(expectedPoints);
    }

    @Test
    void test_thirdReceiptBonus() throws Exception {
        // Arrange: Create three unique receipts for the same user.
        // First receipt
        Receipt receipt1 = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt1.setUserId("testUserBonus3");
        receipt1.setRetailer("UniqueRetailer3-1");
        receipt1.setPurchaseDate("2025-03-05");
        receipt1.setPurchaseTime("12:00");
        receiptService.storeReceipt(receipt1);

        // Second receipt
        Receipt receipt2 = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt2.setUserId("testUserBonus3");
        receipt2.setRetailer("UniqueRetailer3-2");
        receipt2.setPurchaseDate("2025-03-06");
        receipt2.setPurchaseTime("13:00");
        receiptService.storeReceipt(receipt2);

        // Third receipt
        Receipt receipt3 = TestUtils.loadJson("testReceipt.json", Receipt.class);
        receipt3.setUserId("testUserBonus3");
        receipt3.setRetailer("UniqueRetailer3-3");
        receipt3.setPurchaseDate("2025-03-07");
        receipt3.setPurchaseTime("14:00");
        ReceiptIdResponse response3 = receiptService.storeReceipt(receipt3);

        // Act: Get points for the third receipt.
        PointsResponse pointsResponse3 = receiptService.calculatePoints(response3.getId());

        // Expected bonus for the third receipt is 250.
        int basePoints = PointsCalculator.calculateBasePoints(receipt3);
        int expectedPoints = basePoints + 250;

        // Assert:
        Truth.assertThat(pointsResponse3.getPoints()).isEqualTo(expectedPoints);
    }

}
