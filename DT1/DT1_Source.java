import java.util.List;

public class CheckoutService {

    // Định nghĩa các hạng thành viên theo đặc tả
    public enum CustomerTier {
        NEW, STANDARD, SILVER, GOLD, DIAMOND
    }

    // Định nghĩa khu vực giao hàng
    public enum Location {
        NOI_THANH, NGOAI_THANH
    }

    // Lớp đại diện cho sản phẩm trong giỏ hàng
    public static class CartItem {
        private long price;
        private boolean isLimitedEdition;

        public CartItem(long price, boolean isLimitedEdition) {
            this.price = price;
            this.isLimitedEdition = isLimitedEdition;
        }

        public long getPrice() { return price; }
        public boolean isLimitedEdition() { return isLimitedEdition; }
    }

    // DTO hứng dữ liệu đầu vào
    public static class OrderRequest {
        private List<CartItem> items;
        private CustomerTier tier;
        private String couponCode;
        private Location location;

        public OrderRequest(List<CartItem> items, CustomerTier tier, String couponCode, Location location) {
            this.items = items;
            this.tier = tier;
            this.couponCode = couponCode;
            this.location = location;
        }

        public List<CartItem> getItems() { return items; }
        public CustomerTier getTier() { return tier; }
        public String getCouponCode() { return couponCode; }
        public Location getLocation() { return location; }
    }

    // --- CÁC EXCEPTION THEO LUỒNG NGOẠI LỆ ---
    public static class InvalidCouponException extends RuntimeException {
        public InvalidCouponException(String message) { super(message); }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); } // Dùng cho lỗi HTTP 404
    }

    // --- PHƯƠNG THỨC XỬ LÝ CHÍNH ---
    public long calculateTotalAmount(OrderRequest request) {
        long totalCartValue = 0;
        boolean hasLimitedItem = false;

        // BƯỚC 1: Tính tổng giá trị giỏ hàng & Kiểm tra tính chất sản phẩm
        for (CartItem item : request.getItems()) {
            totalCartValue += item.getPrice();
            if (item.isLimitedEdition()) {
                hasLimitedItem = true;
            }
        }

        // BƯỚC 2: Quy tắc Phí vận chuyển (Shipping Rules)
        long shippingFee = 0;
        boolean freeshipConditionA = totalCartValue >= 1_500_000;
        boolean freeshipConditionB = (request.getTier() == CustomerTier.GOLD || request.getTier() == CustomerTier.DIAMOND) 
                                      && totalCartValue >= 800_000;

        if (freeshipConditionA || freeshipConditionB) {
            shippingFee = 0;
        } else {
            shippingFee = (request.getLocation() == Location.NOI_THANH) ? 30_000 : 50_000;
        }

        // BƯỚC 3: Quy tắc Mã giảm giá (Coupon Rules)
        long discountAmount = 0;
        String couponCode = request.getCouponCode();

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            // Ràng buộc sản phẩm Limited (Kiểm tra trước khi xử lý logic mã)
            if (hasLimitedItem) {
                throw new InvalidCouponException("Không áp dụng mã giảm giá cho sản phẩm Limited");
            }

            if (couponCode.equals("NEWBIE")) {
                if (request.getTier() != CustomerTier.NEW) {
                    throw new InvalidCouponException("Mã giảm giá áp dụng sai đối tượng");
                }
                long discount = (long) (totalCartValue * 0.10);
                discountAmount = Math.min(discount, 100_000); // Max cap 100k

            } else if (couponCode.equals("SNEAKERVIP")) {
                if (totalCartValue < 2_000_000) {
                    throw new InvalidCouponException("Không đủ điều kiện áp dụng mã");
                }
                
                if (request.getTier() == CustomerTier.STANDARD || request.getTier() == CustomerTier.SILVER) {
                    discountAmount = 300_000;
                } else if (request.getTier() == CustomerTier.GOLD || request.getTier() == CustomerTier.DIAMOND) {
                    discountAmount = 500_000;
                }
            } else {
                // [EX-01]: Mã giảm giá hết hạn hoặc không tồn tại (ném HTTP 404)
                throw new ResourceNotFoundException("Mã không hợp lệ");
            }
        }

        // BƯỚC 4: Chốt tổng tiền & [EX-02] Luồng ngoại lệ tiền âm
        long finalAmount = totalCartValue + shippingFee - discountAmount;
        
        if (finalAmount < 0) {
            return 0; // Làm tròn về 0 VNĐ
        }

        return finalAmount;
    }
}
