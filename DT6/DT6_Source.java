package com.benchmark;

class FlightRefundSystem {

    // 1. Khai báo các hằng số và cấu trúc dữ liệu Enum
    public enum TicketClass {
        SAVER, FLEX, BUSINESS
    }

    public enum CustomerTier {
        REGULAR, PLATINUM
    }

    // 2. Tùy chỉnh Exception để bắt lỗi ràng buộc đầu vào
    public static class InvalidTicketException extends IllegalArgumentException {
        public InvalidTicketException(String message) {
            super(message);
        }
    }

    // 3. DTO chứa dữ liệu đầu vào của yêu cầu hủy vé
    public static class RefundRequest {
        private long ticketPrice;
        private TicketClass ticketClass;
        private int hoursToDeparture;
        private CustomerTier customerTier;
        private boolean isForceMajeure;

        public RefundRequest(long ticketPrice, TicketClass ticketClass, int hoursToDeparture,
                             CustomerTier customerTier, boolean isForceMajeure) {
            this.ticketPrice = ticketPrice;
            this.ticketClass = ticketClass;
            this.hoursToDeparture = hoursToDeparture;
            this.customerTier = customerTier;
            this.isForceMajeure = isForceMajeure;
        }

        public long getTicketPrice() { return ticketPrice; }
        public TicketClass getTicketClass() { return ticketClass; }
        public int getHoursToDeparture() { return hoursToDeparture; }
        public CustomerTier getCustomerTier() { return customerTier; }
        public boolean isForceMajeure() { return isForceMajeure; }
    }

    // 4. Hàm xử lý logic cốt lõi của hệ thống
    public long calculateRefund(RefundRequest request) {
        // Kiểm tra ràng buộc dữ liệu đầu vào
        if (request.getTicketPrice() <= 0) {
            throw new InvalidTicketException("Giá vé gốc phải lớn hơn 0");
        }
        if (request.getHoursToDeparture() < 0) {
            throw new InvalidTicketException("Số giờ tính tới lúc cất cánh không hợp lệ");
        }

        int hours = request.getHoursToDeparture();
        TicketClass tClass = request.getTicketClass();

        // Quy tắc 4: Quyền Lực Tối Thượng - Bất khả kháng (Force Majeure Override)
        if (request.isForceMajeure()) {
            // Ngoại lệ kép (Nested Trap): Khách hạng SAVER hủy sát giờ (<24h) không được áp dụng bất khả kháng
            if (!(tClass == TicketClass.SAVER && hours < 24)) {
                return request.getTicketPrice();
            }
        }

        // Quy tắc 1: Tỷ lệ phạt cơ bản (Base Penalty Rate)
        double penaltyRate;
        if (hours >= 72) {
            if (tClass == TicketClass.BUSINESS) {
                penaltyRate = 0.0;
            } else if (tClass == TicketClass.FLEX) {
                penaltyRate = 0.10;
            } else {
                penaltyRate = 0.30;
            }
        } else if (hours >= 24) { // 24h <= hours < 72h
            if (tClass == TicketClass.BUSINESS) {
                penaltyRate = 0.20;
            } else if (tClass == TicketClass.FLEX) {
                penaltyRate = 0.50;
            } else {
                penaltyRate = 1.0; // SAVER phạt 100%
            }
        } else { // < 24h
            if (tClass == TicketClass.BUSINESS) {
                penaltyRate = 0.50;
            } else {
                penaltyRate = 1.0; // FLEX và SAVER đều phạt 100%
            }
        }

        long penaltyAmount = (long) (request.getTicketPrice() * penaltyRate);
        long remainingAmount = request.getTicketPrice() - penaltyAmount;

        // Quy tắc 2: Phí xử lý hệ thống (System Surcharge)
        // Chỉ thu 200.000 VNĐ nếu tiền vé còn lại > 0 và khách không phải PLATINUM
        long systemSurcharge = (remainingAmount > 0 && request.getCustomerTier() != CustomerTier.PLATINUM) ? 200_000L : 0L;

        // Quy tắc 3: Chống nợ xấu (Negative Balance Prevention)
        return Math.max(0L, remainingAmount - systemSurcharge);
    }
}
