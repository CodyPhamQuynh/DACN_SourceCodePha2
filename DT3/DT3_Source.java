package com.benchmark;

class CartCalculationService {

    // =========================================
    // 1. CÁC ENUM & EXCEPTION HỖ TRỢ
    // =========================================

    public enum CustomerTier {
        NEW, SILVER, GOLD
    }

    public enum PaymentMethod {
        COD, CREDIT_CARD, E_WALLET
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    // =========================================
    // 2. HÀM XỬ LÝ NGHIỆP VỤ CHÍNH
    // =========================================

    public double calculateFinalAmount(double orderTotal, CustomerTier customerTier,
                                       PaymentMethod paymentMethod, boolean hasFlashSaleItem) {

        // --- Ràng buộc dữ liệu đầu vào ---
        if (orderTotal < 50_000) {
            throw new InvalidOrderException("Đơn hàng tối thiểu phải từ 50.000 VNĐ");
        }

        // --- Quy tắc 1: Chiết khấu theo Hạng thành viên (Tier Discount) ---
        double discountAmount = 0;

        // Ưu tiên cao nhất: Nếu có sản phẩm Flash Sale -> Hủy bỏ toàn bộ chiết khấu (0%)
        if (!hasFlashSaleItem) {
            if (customerTier == CustomerTier.SILVER) {
                // Giảm 5%, tối đa 50.000 VNĐ
                discountAmount = Math.min(orderTotal * 0.05, 50_000);
            } else if (customerTier == CustomerTier.GOLD) {
                // Giảm 10%, tối đa 100.000 VNĐ
                discountAmount = Math.min(orderTotal * 0.10, 100_000);
            }
        }

        // --- Quy tắc 2: Phí vận chuyển (Shipping Fee) ---
        double shippingFee = 30_000; // Phí ship cơ bản mặc định

        if (orderTotal >= 500_000) {
            shippingFee = 0; // Đạt điều kiện Freeship
        } else if (paymentMethod == PaymentMethod.E_WALLET) {
            shippingFee = 15_000; // Không đạt Freeship nhưng thanh toán Ví điện tử -> giảm một nửa
        }

        // --- Quy tắc 3: Công thức tính luồng chính ---
        return orderTotal - discountAmount + shippingFee;
    }
}
