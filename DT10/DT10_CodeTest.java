import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EWalletTransactionSystemTest {

    private EWalletTransactionSystem transactionSystem;

    @BeforeEach
    public void setUp() {
        transactionSystem = new EWalletTransactionSystem();
    }

    @Test
    public void testStandardTransferUnderFreeLimit() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                1000000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                false,
                1,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testVipPaymentMaxCashback() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                2000000.0,
                EWalletTransactionSystem.TransactionType.PAYMENT,
                EWalletTransactionSystem.CustomerTier.VIP,
                false,
                0,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(100000.0, response.getCashback());
    }

    @Test
    public void testInvalidAmountException() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                0.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                false,
                0,
                0.0
        );

        EWalletTransactionSystem.InvalidAmountException exception = assertThrows(
                EWalletTransactionSystem.InvalidAmountException.class,
                () -> transactionSystem.processTransaction(request)
        );

        assertEquals("Số tiền giao dịch không hợp lệ", exception.getMessage());
    }

    @Test
    public void testSuspiciousActivityException() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                20000000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.VIP,
                true,
                0,
                0.0
        );

        EWalletTransactionSystem.SuspiciousActivityException exception = assertThrows(
                EWalletTransactionSystem.SuspiciousActivityException.class,
                () -> transactionSystem.processTransaction(request)
        );

        assertEquals("Giao dịch vượt hạn mức trên thiết bị mới", exception.getMessage());
    }

    @Test
    public void testStandardTransferFeeActivationBoundary() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                250000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                false,
                4,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(5000.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testStandardTransferMinFeeBoundary() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                100000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                false,
                4,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(5000.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testStandardTransferFreeCountBoundary() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                1000000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                false,
                3,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(20000.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testPremiumPaymentMaxCashbackBoundary() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                2000000.0,
                EWalletTransactionSystem.TransactionType.PAYMENT,
                EWalletTransactionSystem.CustomerTier.PREMIUM,
                false,
                0,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(50000.0, response.getCashback());
    }

    @Test
    public void testMonthlyCashbackCapBoundary() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                500000.0,
                EWalletTransactionSystem.TransactionType.PAYMENT,
                EWalletTransactionSystem.CustomerTier.VIP,
                false,
                0,
                480000.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(20000.0, response.getCashback());
    }

    @Test
    public void testPremiumTransferExceedFreeLimitCrossCondition() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                1000000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.PREMIUM,
                false,
                11,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(10000.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testNewDeviceAmountBelowFraudThresholdCrossCondition() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                19999999.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.STANDARD,
                true,
                1,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }

    @Test
    public void testFamiliarDeviceLargeAmountVipCrossCondition() {
        EWalletTransactionSystem.TransactionRequest request = new EWalletTransactionSystem.TransactionRequest(
                50000000.0,
                EWalletTransactionSystem.TransactionType.TRANSFER,
                EWalletTransactionSystem.CustomerTier.VIP,
                false,
                100,
                0.0
        );

        EWalletTransactionSystem.TransactionResponse response = transactionSystem.processTransaction(request);

        assertEquals(EWalletTransactionSystem.TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(0.0, response.getFee());
        assertEquals(0.0, response.getCashback());
    }
}
