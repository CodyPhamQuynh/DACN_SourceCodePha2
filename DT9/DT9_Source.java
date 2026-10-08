import java.time.DayOfWeek;

public class LibraryFineSystem {

    // ==========================================
    // 1. CÁC KIỂU DỮ LIỆU & ENUM
    // ==========================================
    public enum BookType {
        STANDARD,   // Sách thường
        REFERENCE,  // Sách tham khảo/giáo trình
        ARCHIVAL    // Sách lưu chiểu
    }

    public enum CardStatus {
        ACTIVE,
        LOCKED
    }

    // Lớp chứa kết quả trả về
    public static class ReturnResult {
        private final long fineAmount;
        private final CardStatus cardStatus;

        public ReturnResult(long fineAmount, CardStatus cardStatus) {
            this.fineAmount = fineAmount;
            this.cardStatus = cardStatus;
        }

        public long getFineAmount() {
            return fineAmount;
        }

        public CardStatus getCardStatus() {
            return cardStatus;
        }
    }

    // Ngoại lệ tùy chỉnh cho [EX-02]
    public static class InvalidBorrowException extends RuntimeException {
        public InvalidBorrowException(String message) {
            super(message);
        }
    }

    // ==========================================
    // 2. HÀM XỬ LÝ NGHIỆP VỤ CHÍNH
    // ==========================================
    public ReturnResult calculateFine(int daysLate, BookType bookType, long bookPrice, DayOfWeek returnDay, int pastViolations) {
        
        // [EX-01] Xử lý số ngày trễ âm hoặc trả đúng hạn (daysLate <= 0)
        if (daysLate <= 0) {
            return new ReturnResult(0, CardStatus.ACTIVE);
        }

        // [EX-02] Xử lý sách mượn nội bộ (Sách Lưu Chiểu)
        if (bookType == BookType.ARCHIVAL && daysLate > 0) {
            throw new InvalidBorrowException("Lỗi hệ thống: Sách Lưu Chiểu không được phép phát sinh ngày trễ hạn");
        }

        // ------------------------------------------
        // QUY TẮC CHIẾT KHẤU: Ân hạn cuối tuần (Weekend Grace)
        // ------------------------------------------
        int effectiveDaysLate = daysLate;
        if (returnDay == DayOfWeek.SATURDAY || returnDay == DayOfWeek.SUNDAY) {
            effectiveDaysLate = Math.max(0, daysLate - 1);
        }

        // ------------------------------------------
        // QUY TẮC 2.1: Tính Base Fine và Hạn mức phạt
        // ------------------------------------------
        long baseFine = 0;
        if (bookType == BookType.STANDARD) {
            baseFine = effectiveDaysLate * 5000L;
        } else if (bookType == BookType.REFERENCE) {
            baseFine = effectiveDaysLate * 10000L;
        }

        // Hạn mức phạt (Fine Cap) không vượt quá giá sách
        if (baseFine > bookPrice) {
            baseFine = bookPrice;
        }

        // ------------------------------------------
        // QUY TẮC 2.2: Tăng nặng (Tái phạm)
        // ------------------------------------------
        long finalFine = baseFine;
        boolean isRepeatOffender = pastViolations >= 3;
        
        if (isRepeatOffender) {
            finalFine += 50000L;
        }

        // ------------------------------------------
        // QUY TẮC 2.3: Quyết định Khóa thẻ (Card Suspension)
        // ------------------------------------------
        CardStatus status = CardStatus.ACTIVE;
        
        // Điều kiện A: Số ngày trễ gốc >= 30
        // Điều kiện B: Tái phạm VÀ tiền phạt cuối cùng >= 100.000đ
        if (daysLate >= 30 || (isRepeatOffender && finalFine >= 100000L)) {
            status = CardStatus.LOCKED;
        }

        return new ReturnResult(finalFine, status);
    }
}
