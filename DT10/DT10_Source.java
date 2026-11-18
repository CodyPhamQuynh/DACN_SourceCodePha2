package com.benchmark;

class EWalletTransactionSystem {

    // ==========================================
    // 1. CÁC LỚP NGOẠI LỆ (EXCEPTIONS)
    // ==========================================
    public static class InvalidAmountException extends RuntimeException {
        public InvalidAmountException(String message) {
            super(message);
        }
    }

    public static class SuspiciousActivityException extends RuntimeException {
        private final TransactionResponse response;

        public SuspiciousActivityException(String message, TransactionResponse response) {
            super(message);
            this.response = response;
        }

        public TransactionResponse getResponse() {
            return response;
        }
    }

    // ==========================================
    // 2. CÁC KIỂU DỮ LIỆU ENUM & CONSTANTS
    // ==========================================
    public enum TransactionType {
        TRANSFER, PAYMENT, TOPUP
    }

    public enum CustomerTier {
        STANDARD, PREMIUM, VIP
    }

    public enum TransactionStatus {
        SUCCESS, BLOCKED
    }

    private static final double MAX_MONTHLY_CASHBACK = 500_000.0;
    private static final double FRAUD_AMOUNT_THRESHOLD = 20_000_000.0;
    private static final double MIN_STANDARD_TRANSFER_FEE = 5_000.0;

    // ==========================================
    // 3. ĐỐI TƯỢNG DỮ LIỆU ĐẦU VÀO / ĐẦU RA
    // ==========================================
    public static class TransactionRequest {
        private final double amount;
        private final TransactionType type;
        private final CustomerTier tier;
        private final boolean isNewDevice;
        private final int dailyTransferCount;
        private final double monthlyAccumulatedCashback;

        public TransactionRequest(double amount, TransactionType type, CustomerTier tier,
                                  boolean isNewDevice, int dailyTransferCount, double monthlyAccumulatedCashback) {
            this.amount = amount;
            this.type = type;
            this.tier = tier;
            this.isNewDevice = isNewDevice;
            this.dailyTransferCount = dailyTransferCount;
            this.monthlyAccumulatedCashback = monthlyAccumulatedCashback;
        }

        public double getAmount() { return amount; }
        public TransactionType getType() { return type; }
        public CustomerTier getTier() { return tier; }
        public boolean isNewDevice() { return isNewDevice; }
        public int getDailyTransferCount() { return dailyTransferCount; }
        public double getMonthlyAccumulatedCashback() { return monthlyAccumulatedCashback; }
    }

    public static class TransactionResponse {
        private final TransactionStatus status;
        private final double fee;
        private final double cashback;

        public TransactionResponse(TransactionStatus status, double fee, double cashback) {
            this.status = status;
            this.fee = fee;
            this.cashback = cashback;
        }

        public TransactionStatus getStatus() { return status; }
        public double getFee() { return fee; }
        public double getCashback() { return cashback; }
    }

    // ==========================================
    // 4. LỚP XỬ LÝ LOGIC NGHIỆP VỤ CHÍNH
    // ==========================================
    public TransactionResponse processTransaction(TransactionRequest request) {

        // 3. [EX-01] Kiểm tra dữ liệu đầu vào (Input Validation)
        if (request.getAmount() <= 0) {
            throw new InvalidAmountException("Số tiền giao dịch không hợp lệ");
        }

        // 2.3. Quy tắc Kiểm soát Gian lận (Fraud Control) - Ưu tiên Cao nhất
        if (request.isNewDevice() && request.getAmount() >= FRAUD_AMOUNT_THRESHOLD) {
            TransactionResponse blockedResponse = new TransactionResponse(TransactionStatus.BLOCKED, 0.0, 0.0);
            throw new SuspiciousActivityException("Giao dịch vượt hạn mức trên thiết bị mới", blockedResponse);
        }

        TransactionStatus finalStatus = TransactionStatus.SUCCESS;
        double finalFee = 0.0;
        double finalCashback = 0.0;

        // 2.1. Quy tắc Tính phí giao dịch (Transaction Fee Rules)
        if (request.getType() == TransactionType.TRANSFER) {
            finalFee = calculateTransferFee(request.getAmount(), request.getTier(), request.getDailyTransferCount());
        }

        // 2.2. Quy tắc Hoàn tiền (Cashback Rules)
        if (request.getType() == TransactionType.PAYMENT) {
            finalCashback = calculateCashback(request.getAmount(), request.getTier(), request.getMonthlyAccumulatedCashback());
        }

        return new TransactionResponse(finalStatus, finalFee, finalCashback);
    }

    private double calculateTransferFee(double amount, CustomerTier tier, int dailyTransferCount) {
        if (tier == CustomerTier.VIP) {
            return 0.0;
        } else if (tier == CustomerTier.PREMIUM) {
            if (dailyTransferCount < 10) {
                return 0.0;
            }
            return amount * 0.01;
        } else {
            // Hạng STANDARD: Miễn phí 3 giao dịch đầu tiên (0, 1, 2)
            if (dailyTransferCount < 3) {
                return 0.0;
            }
            return Math.max(amount * 0.02, MIN_STANDARD_TRANSFER_FEE);
        }
    }

    private double calculateCashback(double amount, CustomerTier tier, double monthlyAccumulatedCashback) {
        if (tier == CustomerTier.STANDARD || monthlyAccumulatedCashback >= MAX_MONTHLY_CASHBACK) {
            return 0.0;
        }

        double rawCashback = (tier == CustomerTier.PREMIUM)
                ? Math.min(amount * 0.05, 50_000.0)
                : Math.min(amount * 0.10, 100_000.0);

        double remainingCapacity = MAX_MONTHLY_CASHBACK - monthlyAccumulatedCashback;
        return Math.min(rawCashback, remainingCapacity);
    }
}
