package com.benchmark;

class LoanOverduePenaltySystem {

    // --- CÁC ENUM DỮ LIỆU ---
    public enum CustomerGroup {
        STANDARD, WARNING, BAD_DEBT
    }

    public enum CollateralType {
        REAL_ESTATE, VEHICLE, NONE
    }

    // --- CÁC LỚP NGOẠI LỆ (EXCEPTIONS) ---
    public static class InvalidLoanAmountException extends RuntimeException {
        public InvalidLoanAmountException(String message) {
            super(message);
        }
    }

    public static class InvalidDateException extends RuntimeException {
        public InvalidDateException(String message) {
            super(message);
        }
    }

    // --- HÀM NGHIỆP VỤ CHÍNH ---
    public double calculateFinalPenaltyAmount(double loanAmount, int overdueDays,
                                              CustomerGroup customerGroup,
                                              CollateralType collateralType) {

        // 2. Ràng buộc dữ liệu đầu vào
        if (loanAmount <= 0) {
            throw new InvalidLoanAmountException("Dư nợ gốc phải lớn hơn 0");
        }
        if (overdueDays < 0) {
            throw new InvalidDateException("Số ngày trễ hạn không được nhỏ hơn 0");
        }

        // Nếu không trễ hạn ngày nào, không có tiền phạt
        if (overdueDays == 0) {
            return 0.0;
        }

        // Quy tắc 1: Phí phạt cố định (Fixed Penalty Fee) & Ngoại lệ Ân hạn (Grace Period)
        double fixedPenaltyFee;
        if (overdueDays <= 10) {
            fixedPenaltyFee = (customerGroup == CustomerGroup.STANDARD && overdueDays <= 3) ? 0.0 : 100_000.0;
        } else if (overdueDays <= 30) {
            fixedPenaltyFee = 500_000.0;
        } else {
            fixedPenaltyFee = 2_000_000.0;
        }

        // Quy tắc 2: Lãi suất phạt cộng dồn (Penalty Interest)
        double penaltyInterest = 0.0;
        if (overdueDays > 10) {
            // 0.05% tương đương 0.0005, trần tối đa 10% dư nợ gốc
            double calculatedInterest = loanAmount * 0.0005 * (overdueDays - 10);
            penaltyInterest = Math.min(calculatedInterest, loanAmount * 0.10);
        }

        // Quy tắc 3: Giảm trừ tài sản thế chấp (Collateral Discount) & Ghi đè BAD_DEBT
        double rawPenalty = fixedPenaltyFee + penaltyInterest;
        double discountRate = 0.0;

        if (customerGroup != CustomerGroup.BAD_DEBT) {
            if (collateralType == CollateralType.REAL_ESTATE) {
                discountRate = 0.20; // Giảm 20%
            } else if (collateralType == CollateralType.VEHICLE) {
                discountRate = 0.10; // Giảm 10%
            }
        }

        // Quy tắc 4: Tính toán cuối cùng
        double discountAmount = rawPenalty * discountRate;
        return rawPenalty - discountAmount;
    }
}
