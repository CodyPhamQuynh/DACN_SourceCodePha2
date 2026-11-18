package com.benchmark;

class ExpressLogisticsSystem {

    // ==========================================
    // 1. CÁC LỚP NGOẠI LỆ (EXCEPTIONS)
    // ==========================================
    // Kế thừa IllegalArgumentException để tương thích mọi kiểm thử ngoại lệ đầu vào
    public static class InvalidMeasurementException extends IllegalArgumentException {
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

    public double calculateFinalShippingFee(
            double actualWeight,
            double length, double width, double height,
            double distance,
            boolean isExtremeWeather,
            CustomerTier customerTier) {

        // KIỂM TRA RÀNG BUỘC ĐẦU VÀO (Input Constraints)
        if (actualWeight <= 0 || length <= 0 || width <= 0 || height <= 0 || distance <= 0) {
            throw new InvalidMeasurementException("Trọng lượng thực tế, kích thước (dài, rộng, cao) và khoảng cách phải lớn hơn 0");
        }

        // QUY TẮC 1: Tính trọng lượng tính cước (Chargeable Weight)
        double dimWeight = (length * width * height) / 5000.0;
        int roundedDimWeight = (int) Math.ceil(dimWeight);
        int roundedActualWeight = (int) Math.ceil(actualWeight);

        int chargeableWeight = Math.max(roundedActualWeight, roundedDimWeight);

        // Ngoại lệ từ chối: Quá 50kg
        if (chargeableWeight > 50) {
            throw new OverweightException("Trọng lượng tính cước vượt quá 50kg, hệ thống từ chối phục vụ");
        }

        // QUY TẮC 2: Cước cơ bản (Base Fare)
        double baseFare = 40_000;
        if (chargeableWeight > 5) {
            baseFare += (chargeableWeight - 5) * 10_000;
        }

        // QUY TẮC 3: Phụ phí (Surcharges)
        if (isExtremeWeather) {
            baseFare *= 1.5;
        }

        double distanceSurcharge = (distance > 50) ? 50_000 : 0;

        // QUY TẮC 5: Bẫy Rủi ro cao (High-Risk Override Trap)
        boolean isHighRisk = (chargeableWeight >= 30 && distance > 50);

        if (isHighRisk) {
            // Vô hiệu hóa toàn bộ quyền lợi Quy tắc 4 + Thu thêm 100.000đ phí bốc vác
            return baseFare + distanceSurcharge + 100_000;
        }

        // QUY TẮC 4: Quyền lợi Hạng Khách hàng (Tier Benefits)
        if (customerTier == CustomerTier.B2B_PARTNER) {
            distanceSurcharge = 0; // Miễn phí hoàn toàn phụ phí khoảng cách
        }

        double finalFee = baseFare + distanceSurcharge;

        if (customerTier == CustomerTier.VIP) {
            finalFee *= 0.8; // VIP giảm 20% trên Tổng tiền
        }

        return finalFee;
    }
}
