package com.benchmark;

class DualVoucherSystem {

    // =========================================================================
    // 1. CÁC ENUM ĐỊNH NGHĨA DỮ LIỆU ĐẦU VÀO
    // =========================================================================
    public enum Category {
        FASHION, ELECTRONICS, GROCERY
    }

    public enum PaymentMethod {
        COD, VNPAY, CREDIT_CARD
    }

    public enum ShopVoucher {
        SHOP_50K, SHOP_10PT
    }

    public enum PlatformVoucher {
        PLATFORM_FREESHIP, PLATFORM_20PT
    }

    // =========================================================================
    // 2. CÁC LỚP NGOẠI LỆ (EXCEPTIONS)
    // =========================================================================
    public static class InvalidCartException extends RuntimeException {
        public InvalidCartException(String message) {
            super(message);
        }
    }

    public static class VoucherConditionException extends RuntimeException {
        public VoucherConditionException(String message) {
            super(message);
        }
    }

    // =========================================================================
    // 3. LỚP LƯU TRỮ KẾT QUẢ ĐẦU RA (DTO)
    // =========================================================================
    public static class CheckoutResult {
        private final long cartValue;
        private final long shopDiscount;
        private final long platformDiscount;
        private final long finalAmount;

        public CheckoutResult(long cartValue, long shopDiscount, long platformDiscount, long finalAmount) {
            this.cartValue = cartValue;
            this.shopDiscount = shopDiscount;
            this.platformDiscount = platformDiscount;
            this.finalAmount = finalAmount;
        }

        public long getCartValue() { return cartValue; }
        public long getShopDiscount() { return shopDiscount; }
        public long getPlatformDiscount() { return platformDiscount; }
        public long getFinalAmount() { return finalAmount; }
    }

    // =========================================================================
    // 4. LỚP XỬ LÝ LOGIC NGHIỆP VỤ CHÍNH (SERVICE)
    // =========================================================================
    public CheckoutResult calculateFinalAmount(
            long cartValue,
            Category category,
            PaymentMethod paymentMethod,
            ShopVoucher shopVoucher,
            PlatformVoucher platformVoucher) {

        // --- RÀNG BUỘC ĐẦU VÀO ---
        if (cartValue <= 0) {
            throw new InvalidCartException("Giá trị giỏ hàng phải lớn hơn 0");
        }

        long shopDiscount = 0;
        long platformDiscount = 0;

        // --- BƯỚC 1 & 2: TÍNH TOÁN SHOP VOUCHER ---
        // Ghi đè ngoại lệ: Ngành hàng ELECTRONICS KHÔNG ĐƯỢC PHÉP dùng Shop Voucher
        if (category != Category.ELECTRONICS && shopVoucher != null) {
            if (shopVoucher == ShopVoucher.SHOP_50K) {
                if (cartValue >= 200_000) {
                    shopDiscount = 50_000;
                }
            } else {
                // SHOP_10PT: Giảm 10%, tối đa 30.000 VNĐ cho đơn từ 150.000 VNĐ
                if (cartValue >= 150_000) {
                    shopDiscount = Math.min((long) (cartValue * 0.10), 30_000L);
                }
            }
        }

        // --- BƯỚC 3: TÍNH GIÁ TẠM TÍNH ---
        long tempValue = cartValue - shopDiscount;

        // --- BƯỚC 4: TÍNH TOÁN PLATFORM VOUCHER ---
        if (platformVoucher != null) {
            if (platformVoucher == PlatformVoucher.PLATFORM_FREESHIP) {
                // Min Spend xét trên Giá gốc (cartValue)
                if (cartValue < 100_000 || (paymentMethod != PaymentMethod.VNPAY && paymentMethod != PaymentMethod.CREDIT_CARD)) {
                    throw new VoucherConditionException("Không đủ điều kiện áp dụng PLATFORM_FREESHIP");
                }
                platformDiscount = 30_000;

            } else {
                // PLATFORM_20PT: Đơn từ 500.000 VNĐ và bắt buộc thanh toán bằng VNPAY
                if (cartValue < 500_000 || paymentMethod != PaymentMethod.VNPAY) {
                    throw new VoucherConditionException("Không đủ điều kiện áp dụng PLATFORM_20PT");
                }
                // Giảm 20% trên GIÁ TẠM TÍNH (tempValue), tối đa 100.000 VNĐ
                platformDiscount = Math.min((long) (tempValue * 0.20), 100_000L);
            }
        }

        // --- BƯỚC 5: CHỐT TỔNG TIỀN VÀ CHỐNG ÂM ---
        long finalAmount = Math.max(0L, tempValue - platformDiscount);

        return new CheckoutResult(cartValue, shopDiscount, platformDiscount, finalAmount);
    }
}
