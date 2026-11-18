package com.benchmark;

import java.time.DayOfWeek;

class LibraryFineSystem {

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

        // [EX-02] Xử lý sách mượn nội bộ (Sách Lưu Chiểu phát sinh ngày trễ > 0)
        if (bookType == BookType.ARCHIVAL) {
            throw new InvalidBorrowException("Lỗi hệ thống: Sách Lưu Chiểu không được phép phát sinh ngày trễ hạn");
        }

        // QUY TẮC CHIẾT KHẤU: Ân hạn cuối tuần (Weekend Grace)
        int effectiveDaysLate = daysLate;
        if (returnDay == DayOfWeek.SATURDAY || returnDay == DayOfWeek.SUNDAY) {
            effectiveDaysLate = daysLate - 1;
        }

        // QUY TẮC 2.1: Tính Base Fine và Hạn mức phạt (Fine Cap)
        long dailyRate = (bookType == BookType.STANDARD) ? 5_000L : 10_000L;
        long baseFine = Math.min(effectiveDaysLate * dailyRate, bookPrice);

        // QUY TẮC 2.2: Tăng nặng (Tái phạm)
        long finalFine = baseFine;
        boolean isRepeatOffender = pastViolations >= 3;

        if (isRepeatOffender) {
            finalFine += 50_000L;
        }

        // QUY TẮC 2.3: Quyết định Khóa thẻ (Card Suspension)
        CardStatus status = (daysLate >= 30 || (isRepeatOffender && finalFine >= 100_000L))
                ? CardStatus.LOCKED
                : CardStatus.ACTIVE;

        return new ReturnResult(finalFine, status);
    }
}
