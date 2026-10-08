public class FlightRefundSystem {

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

        // Quy tắc 4: Quyền Lực Tối Thượng - Bất khả kháng (Force Majeure Override)
        if (request.isForceMajeure()) {
            // Ngoại lệ kép (Nested Trap): Khách hạng SAVER hủy sát giờ (<24h) không được áp dụng bất khả kháng
            boolean isSaverShattered = (request.getTicketClass() == TicketClass.SAVER 
                                        && request.getHoursToDeparture() < 24);
            
            if (!isSaverShattered) {
                // Vô hiệu hóa Quy tắc 1 và 2, hoàn 100% tiền vé
                return request.getTicketPrice();
            }
            // Nếu rơi vào isSaverShattered, hệ thống bỏ qua lệnh if này và đi tiếp xuống luồng phạt bên dưới
        }

        // Quy tắc 1: Tỷ lệ phạt cơ bản (Base Penalty Rate)
        double penaltyRate = 0.0;
        int hours = request.getHoursToDeparture();
        TicketClass tClass = request.getTicketClass();

        if (hours >= 72) {
            if (tClass == TicketClass.BUSINESS) penaltyRate = 0.0;
            else if (tClass == TicketClass.FLEX) penaltyRate = 0.10;
            else if (tClass == TicketClass.SAVER) penaltyRate = 0.30;
        } else if (hours >= 24) { // 24h <= hours < 72h
            if (tClass == TicketClass.BUSINESS) penaltyRate = 0.20;
            else if (tClass == TicketClass.FLEX) penaltyRate = 0.50;
            else if (tClass == TicketClass.SAVER) penaltyRate = 1.0; // Phạt 100%
        } else { // < 24h
            if (tClass == TicketClass.BUSINESS) penaltyRate = 0.50;
            else if (tClass == TicketClass.FLEX) penaltyRate = 1.0; // Phạt 100%
            else if (tClass == TicketClass.SAVER) penaltyRate = 1.0; // Phạt 100%
        }

        long penaltyAmount = (long) (request.getTicketPrice() * penaltyRate);
        long remainingAmount = request.getTicketPrice() - penaltyAmount;

        // Quy tắc 2: Phí xử lý hệ thống (System Surcharge)
        long systemSurcharge = 200_000;
        
        // Điều kiện 1: Nếu tiền vé đã bị phạt sạch (còn <= 0), không thu thêm phí xử lý
        if (remainingAmount <= 0) {
            systemSurcharge = 0;
        }
        
        // Điều kiện 2: Hạng PLATINUM miễn hoàn toàn phí xử lý trong mọi trường hợp
        if (request.getCustomerTier() == CustomerTier.PLATINUM) {
            systemSurcharge = 0;
        }

        // Quy tắc 3: Chống nợ xấu (Negative Balance Prevention)
        long refundAmount = remainingAmount - systemSurcharge;
        
        // Nếu phí xử lý cao hơn tiền vé còn lại, tự động làm tròn về 0, không bắt khách bù thêm
        if (refundAmount < 0) {
            refundAmount = 0;
        }

        return refundAmount;
    }
}
