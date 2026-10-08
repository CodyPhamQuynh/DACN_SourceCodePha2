public class EWalletTransactionSystem {

    // ==========================================
    // 1. CÁC LỚP NGOẠI LỆ (EXCEPTIONS)
    // ==========================================
    public static class InvalidAmountException extends RuntimeException {
        public InvalidAmountException(String message) {
            super(message);
        }
    }

    public static class SuspiciousActivityException extends RuntimeException {
        public SuspiciousActivityException(String message) {
            super(message);
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

    private static final double MAX_MONTHLY_CASHBACK = 500000.0;
    private static final double FRAUD_AMOUNT_THRESHOLD = 20000000.0;
    private static final double MIN_STANDARD_TRANSFER_FEE = 5000.0;

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
        private TransactionStatus status;
        private double fee;
        private double cashback;

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
            throw new SuspiciousActivityException("Giao dịch vượt hạn mức trên thiết bị mới");
            // Ghi chú: Nếu hệ thống bắt buộc trả về response BLOCKED thay vì ném lỗi làm crash app, 
            // Đặc tả yêu cầu: Khóa giao dịch, Fee=0, Cashback=0, và ném ngoại lệ.
            // Vì ném ngoại lệ sẽ ngắt luồng, ta ưu tiên throw exception theo đúng mô tả "Ném ngoại lệ SuspiciousActivityException".
        }

        // Nếu qua được chốt chặn gian lận -> Giao dịch SUCCESS
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
        switch (tier) {
            case VIP:
                // Hạng VIP: Miễn phí mọi giao dịch chuyển tiền
                return 0.0;
            
            case PREMIUM:
                // Hạng Cao cấp: Miễn phí 10 giao dịch đầu tiên (0 đến 9)
                if (dailyTransferCount < 10) {
                    return 0.0;
                } else {
                    // Từ giao dịch 11 trở đi (daily >= 10): phí 1% số tiền chuyển
                    return amount * 0.01;
                }
                
            case STANDARD:
                // Hạng Tiêu chuẩn: Miễn phí 3 giao dịch đầu tiên (0, 1, 2)
                if (dailyTransferCount < 3) {
                    return 0.0;
                } else {
                    // Từ giao dịch 4 trở đi: phí 2%, tối thiểu 5.000 VNĐ
                    double calculatedFee = amount * 0.02;
                    return Math.max(calculatedFee, MIN_STANDARD_TRANSFER_FEE);
                }
                
            default:
                return 0.0;
        }
    }

    private double calculateCashback(double amount, CustomerTier tier, double monthlyAccumulatedCashback) {
        double rawCashback = 0.0;

        // Tính toán Cashback thô dựa trên Hạng
        switch (tier) {
            case STANDARD:
                return 0.0; // Không hoàn tiền
                
            case PREMIUM:
                // Hoàn 5%, tối đa 50.000 VNĐ / 1 giao dịch
                rawCashback = Math.min(amount * 0.05, 50000.0);
                break;
                
            case VIP:
                // Hoàn 10%, tối đa 100.000 VNĐ / 1 giao dịch
                rawCashback = Math.min(amount * 0.10, 100000.0);
                break;
        }

        // Ràng buộc Lũy kế tháng (Monthly Cashback Cap - Tối đa 500.000 VNĐ)
        if (monthlyAccumulatedCashback >= MAX_MONTHLY_CASHBACK) {
            return 0.0; // Đã cạn kiệt lũy kế
        }

        double remainingCapacity = MAX_MONTHLY_CASHBACK - monthlyAccumulatedCashback;
        
        // Nếu phần cashback hiện tại cộng vào vượt quá sức chứa còn lại -> Chỉ cấp phần bù còn lại (Delta)
        return Math.min(rawCashback, remainingCapacity);
    }
}
