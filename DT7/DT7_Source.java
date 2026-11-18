package com.benchmark;

class AffiliateCommissionSystem {

    // 1. Định nghĩa các ngoại lệ (Exception)
    public static class LowValueOrderException extends RuntimeException {
        public LowValueOrderException(String message) {
            super(message);
        }
    }

    // 2. Định nghĩa các Enum theo Đặc tả
    public enum AffiliateLevel {
        BRONZE, SILVER, GOLD
    }

    public enum ProductCategory {
        FASHION, ELECTRONICS, DIGITAL_SOFTWARE
    }

    // 3. Hàm chính xử lý logic tính toán
    public long calculateCommission(long saleAmount, AffiliateLevel affiliateLevel, ProductCategory productCategory, boolean isFirstBuyer) {
        // Kiểm tra tính hợp lệ cơ bản
        if (saleAmount <= 0) {
            throw new IllegalArgumentException("Doanh thu đơn hàng phải lớn hơn 0");
        }

        // ==========================================
        // QUY TẮC 1: ĐIỀU KIỆN TỐI THIỂU
        // ==========================================
        if (saleAmount < 150_000) {
            throw new LowValueOrderException("Đơn hàng chưa đạt mức tối thiểu 150.000 VNĐ");
        }

        // ==========================================
        // QUY TẮC 2: TỶ LỆ HOA HỒNG CƠ BẢN
        // ==========================================
        double commissionRate;

        if (productCategory == ProductCategory.FASHION) {
            if (affiliateLevel == AffiliateLevel.BRONZE) {
                commissionRate = 0.05;
            } else if (affiliateLevel == AffiliateLevel.SILVER) {
                commissionRate = 0.10;
            } else {
                commissionRate = 0.15;
            }
        } else if (productCategory == ProductCategory.ELECTRONICS) {
            // Bẫy chặn ngành hàng ELECTRONICS đối với cấp BRONZE (hoa hồng = 0%)
            if (affiliateLevel == AffiliateLevel.BRONZE) {
                commissionRate = 0.0;
            } else if (affiliateLevel == AffiliateLevel.SILVER) {
                commissionRate = 0.02;
            } else {
                commissionRate = 0.05;
            }
        } else {
            // DIGITAL_SOFTWARE: Đồng giá 20% cho tất cả các cấp bậc
            commissionRate = 0.20;
        }

        long finalCommission = Math.round(saleAmount * commissionRate);

        // ==========================================
        // QUY TẮC 3: GIỚI HẠN HOA HỒNG TỐI ĐA (MAX CAP)
        // ==========================================
        if (productCategory != ProductCategory.DIGITAL_SOFTWARE && finalCommission > 500_000) {
            finalCommission = 500_000;
        }

        // ==========================================
        // QUY TẮC 4: THƯỞNG KHÁCH HÀNG MỚI (BẪY GHI ĐÈ) & QUY TẮC 5
        // ==========================================
        // Tiền thưởng 50.000 VNĐ CHỈ ĐƯỢC CỘNG nếu tổng hoa hồng ở Quy tắc 3 lớn hơn 0
        if (finalCommission > 0 && isFirstBuyer) {
            finalCommission += 50_000;
        }

        return finalCommission;
    }
}
