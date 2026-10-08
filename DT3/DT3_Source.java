public class CartCalculationService {

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

    /**
     * Tính toán số tiền thanh toán cuối cùng của đơn hàng
     *
     * @param orderTotal       Tổng tiền hàng
     * @param customerTier     Hạng khách hàng (NEW, SILVER, GOLD)
     * @param paymentMethod    Phương thức thanh toán (COD, CREDIT_CARD, E_WALLET)
     * @param hasFlashSaleItem Cờ đánh dấu đơn hàng có chứa sản phẩm Flash Sale không
     * @return Số tiền thanh toán cuối cùng (finalAmount)
     */
    public double calculateFinalAmount(double orderTotal, CustomerTier customerTier, 
                                       PaymentMethod paymentMethod, boolean hasFlashSaleItem) {
        
        // --- Ràng buộc dữ liệu đầu vào ---
        if (orderTotal < 50000) {
            throw new InvalidOrderException("Đơn hàng tối thiểu phải từ 50.000 VNĐ");
        }

        // --- Quy tắc 1: Chiết khấu theo Hạng thành viên (Tier Discount) ---
        double discountAmount = 0;

        // Ưu tiên cao nhất: Nếu có sản phẩm Flash Sale -> Hủy bỏ toàn bộ chiết khấu (0%)
        if (!hasFlashSaleItem) {
            switch (customerTier) {
                case SILVER:
                    // Giảm 5%, tối đa 50.000 VNĐ
                    discountAmount = Math.min(orderTotal * 0.05, 50000);
                    break;
                case GOLD:
                    // Giảm 10%, tối đa 100.000 VNĐ
                    discountAmount = Math.min(orderTotal * 0.10, 100000);
                    break;
                case NEW:
                default:
                    // NEW: Không giảm giá
                    discountAmount = 0;
                    break;
            }
        }

        // --- Quy tắc 2: Phí vận chuyển (Shipping Fee) ---
        double shippingFee = 30000; // Phí ship cơ bản mặc định

        if (orderTotal >= 500000) {
            shippingFee = 0; // Đạt điều kiện Freeship
        } else if (paymentMethod == PaymentMethod.E_WALLET) {
            shippingFee = 15000; // Không đạt Freeship nhưng thanh toán Ví điện tử -> giảm nửa
        }

        // --- Quy tắc 3: Công thức tính luồng chính ---
        double finalAmount = orderTotal - discountAmount + shippingFee;

        return finalAmount;
    }
}
