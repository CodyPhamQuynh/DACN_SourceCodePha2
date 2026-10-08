// File: AffiliateCommissionSystem.java

// 1. Định nghĩa các ngoại lệ (Exception)
class LowValueOrderException extends RuntimeException {
    public LowValueOrderException(String message) {
        super(message);
    }
}

// 2. Định nghĩa các Enum theo Đặc tả
enum AffiliateLevel {
    BRONZE, SILVER, GOLD
}

enum ProductCategory {
    FASHION, ELECTRONICS, DIGITAL_SOFTWARE
}

// 3. Class chính xử lý logic tính toán
public class AffiliateCommissionSystem {

    public long calculateCommission(long saleAmount, AffiliateLevel affiliateLevel, ProductCategory productCategory, boolean isFirstBuyer) {
        // Kiểm tra tính hợp lệ cơ bản
        if (saleAmount <= 0) {
            throw new IllegalArgumentException("Doanh thu đơn hàng phải lớn hơn 0");
        }

        // ==========================================
        // QUY TẮC 1: ĐIỀU KIỆN TỐI THIỂU
        // ==========================================
        if (saleAmount < 150000) {
            throw new LowValueOrderException("Đơn hàng chưa đạt mức tối thiểu 150.000 VNĐ");
        }

        // Bẫy chặn ngành hàng ELECTRONICS đối với cấp BRONZE
        if (productCategory == ProductCategory.ELECTRONICS && affiliateLevel == AffiliateLevel.BRONZE) {
            return 0; // Từ chối chia hoa hồng
        }

        // ==========================================
        // QUY TẮC 2: TỶ LỆ HOA HỒNG CƠ BẢN
        // ==========================================
        double commissionRate = 0.0;

        switch (productCategory) {
            case FASHION:
                if (affiliateLevel == AffiliateLevel.BRONZE) commissionRate = 0.05;
                else if (affiliateLevel == AffiliateLevel.SILVER) commissionRate = 0.10;
                else if (affiliateLevel == AffiliateLevel.GOLD) commissionRate = 0.15;
                break;
                
            case ELECTRONICS:
                // BRONZE đã bị chặn ở Quy tắc 1
                if (affiliateLevel == AffiliateLevel.SILVER) commissionRate = 0.02;
                else if (affiliateLevel == AffiliateLevel.GOLD) commissionRate = 0.05;
                break;
                
            case DIGITAL_SOFTWARE:
                // Đồng giá 20% cho tất cả các cấp bậc
                commissionRate = 0.20;
                break;
        }

        long rawCommission = Math.round(saleAmount * commissionRate);

        // ==========================================
        // QUY TẮC 3: GIỚI HẠN HOA HỒNG TỐI ĐA (MAX CAP)
        // ==========================================
        long maxCap = -1; // -1 đại diện cho vô cực (Không giới hạn)
        if (productCategory == ProductCategory.FASHION || productCategory == ProductCategory.ELECTRONICS) {
            maxCap = 500000; 
        }

        long finalCommission = rawCommission;
        if (maxCap != -1 && finalCommission > maxCap) {
            finalCommission = maxCap;
        }

        // ==========================================
        // QUY TẮC 4: THƯỞNG KHÁCH HÀNG MỚI (BẪY GHI ĐÈ) & QUY TẮC 5
        // ==========================================
        // Tiền thưởng 50.000 VNĐ CHỈ ĐƯỢC CỘNG nếu tổng hoa hồng ở Quy tắc 3 lớn hơn 0
        if (finalCommission > 0 && isFirstBuyer) {
            finalCommission += 50000;
        }

        return finalCommission;
    }
}
