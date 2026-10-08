public class ExpressLogisticsSystem {

    // ==========================================
    // 1. CÁC LỚP NGOẠI LỆ (EXCEPTIONS)
    // ==========================================
    public static class InvalidMeasurementException extends RuntimeException {
        public InvalidMeasurementException(String message) {
            super(message);
        }
    }

    public static class OverweightException extends RuntimeException {
        public OverweightException(String message) {
            super(message);
        }
    }

    // ==========================================
    // 2. ENUM HẠNG KHÁCH HÀNG
    // ==========================================
    public enum CustomerTier {
        STANDARD, B2B_PARTNER, VIP
    }

    // ==========================================
    // 3. LOGIC XỬ LÝ CHÍNH (BUSINESS LOGIC)
    // ==========================================
    
    /**
     * Phương thức tính toán Tổng cước phí vận chuyển.
     */
    public double calculateFinalShippingFee(
            double actualWeight,
            double length, double width, double height,
            double distance,
            boolean isExtremeWeather,
            CustomerTier customerTier) {

        // KIỂM TRA RÀNG BUỘC ĐẦU VÀO (Input Constraints)
        if (actualWeight <= 0 || length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidMeasurementException("Trọng lượng thực tế và các kích thước (dài, rộng, cao) phải lớn hơn 0");
        }
        if (distance <= 0) {
            throw new IllegalArgumentException("Khoảng cách (distance) phải lớn hơn 0"); // Xử lý khoảng cách không hợp lệ
        }

        // QUY TẮC 1: Tính trọng lượng tính cước (Chargeable Weight)
        // Tính Trọng lượng thể tích và làm tròn lên
        double dimWeight = (length * width * height) / 5000.0;
        int roundedDimWeight = (int) Math.ceil(dimWeight);
        
        // Trọng lượng thực tế làm tròn lên
        int roundedActualWeight = (int) Math.ceil(actualWeight);

        // Lấy giá trị lớn hơn
        int chargeableWeight = Math.max(roundedActualWeight, roundedDimWeight);

        // Ngoại lệ từ chối: Quá 50kg
        if (chargeableWeight > 50) {
            throw new OverweightException("Trọng lượng tính cước vượt quá 50kg, hệ thống từ chối phục vụ");
        }

        // QUY TẮC 2: Cước cơ bản (Base Fare)
        double baseFare = 40000;
        if (chargeableWeight > 5) {
            baseFare += (chargeableWeight - 5) * 10000;
        }

        // QUY TẮC 3: Phụ phí (Surcharges)
        // Phụ phí thời tiết: Tăng 50% cước cơ bản
        if (isExtremeWeather) {
            baseFare *= 1.5;
        }

        // Phụ phí khoảng cách cơ bản
        double distanceSurcharge = 0;
        if (distance > 50) {
            distanceSurcharge = 50000;
        }

        // QUY TẮC 5: Bẫy Rủi ro cao (High-Risk Override Trap) - Kiểm tra trước để xem có bị mất quyền lợi không
        boolean isHighRisk = (chargeableWeight >= 30 && distance > 50);

        double finalFee = 0;

        if (isHighRisk) {
            // Hàng rủi ro cao: Vô hiệu hóa toàn bộ quyền lợi Quy tắc 4 + Thu thêm 100.000đ bốc vác
            finalFee = baseFare + distanceSurcharge + 100000;
        } else {
            // QUY TẮC 4: Quyền lợi Hạng Khách hàng (Tier Benefits) - Áp dụng khi KHÔNG phải hàng rủi ro cao
            if (customerTier == CustomerTier.B2B_PARTNER) {
                distanceSurcharge = 0; // Miễn phí hoàn toàn phụ phí khoảng cách
            }

            finalFee = baseFare + distanceSurcharge; // Tính tổng tiền trước khi giảm giá VIP

            if (customerTier == CustomerTier.VIP) {
                finalFee *= 0.8; // VIP giảm 20% trên Tổng tiền
            }
        }

        return finalFee;
    }
}
