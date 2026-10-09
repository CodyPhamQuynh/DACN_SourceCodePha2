public class InventoryApprovalWorkflow {

    // 1. CÁC ENUM TRẠNG THÁI VÀ QUYỀN HẠN
    public enum Role {
        STAFF, MANAGER, DIRECTOR
    }

    public enum PoStatus {
        DRAFT, PENDING_DIRECTOR, APPROVED, REJECTED, EXPIRED
    }

    // 2. CÁC LỚP THỰC THỂ (ENTITIES)
    public static class User {
        private String username;
        private Role role;

        public User(String username, Role role) {
            this.username = username;
            this.role = role;
        }

        public Role getRole() { return role; }
    }

    public static class Product {
        private String name;
        private String category;
        private boolean isBulky; // Hàng cồng kềnh

        public Product(String name, String category, boolean isBulky) {
            this.name = name;
            this.category = category;
            this.isBulky = isBulky;
        }

        public String getCategory() { return category; }
        public boolean isBulky() { return isBulky; }
    }

    public static class PurchaseOrder {
        private Product product;
        private int quantity;
        private double unitPrice;
        private PoStatus status;
        private int daysInPending;

        public PurchaseOrder(Product product, int quantity, double unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.status = PoStatus.DRAFT;
            this.daysInPending = 0;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public PoStatus getStatus() { return status; }
        public void setStatus(PoStatus status) { this.status = status; }
        public int getDaysInPending() { return daysInPending; }
        public void setDaysInPending(int daysInPending) { this.daysInPending = daysInPending; }
    }

    // 3. CÁC LỚP XỬ LÝ NGOẠI LỆ (CUSTOM EXCEPTIONS)
    public static class SecurityException extends RuntimeException {
        private PurchaseOrder rejectedPo;

        public SecurityException(String message, PurchaseOrder rejectedPo) { 
            super(message); 
            this.rejectedPo = rejectedPo;
        }
    
        public PurchaseOrder getRejectedPo() { return rejectedPo; }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }

    public static class FrozenDataException extends RuntimeException {
        public FrozenDataException(String message) { super(message); }
    }

    // 4. LỚP NGHIỆP VỤ LÕI (CORE SERVICE)
    public static class InventoryApprovalService {

        private static final double MANAGER_APPROVAL_LIMIT = 20000000.0; // 20 triệu VNĐ
        private static final double BULKY_SURCHARGE = 500000.0; // 500k VNĐ

        // [Rule 2.2 & EX-01] Staff tạo PO và Bẫy khóa Fail-fast
        public PurchaseOrder createPO(User user, Product product, int quantity, double unitPrice) {
            if (user.getRole() != Role.STAFF) {
                throw new UnauthorizedException("Chỉ Nhân viên kho (Staff) mới được phép tạo PO mới.");
            }

            PurchaseOrder po = new PurchaseOrder(product, quantity, unitPrice);

            if ("Hóa chất dễ cháy".equalsIgnoreCase(product.getCategory())) {
                po.setStatus(PoStatus.REJECTED);
                throw new SecurityException("ERR-SEC-01: Hệ thống từ chối nhập Hóa chất dễ cháy.", po);
            }

            return po;
        }

        // [Rule 2.1] Tính toán tổng giá trị phiếu nhập
        public double calculateTotalValue(PurchaseOrder po) {
            double total = po.getQuantity() * po.getUnitPrice();
            if (po.getProduct().isBulky()) {
                total += BULKY_SURCHARGE;
            }
            return total;
        }

        // [Rule 2.3] Quy tắc đóng băng dữ liệu (Data Freeze Constraint)
        public void updatePOData(PurchaseOrder po, int newQuantity, double newUnitPrice) {
            if (po.getStatus() != PoStatus.DRAFT) {
                throw new FrozenDataException("Dữ liệu đã đóng băng ở trạng thái " + po.getStatus() + ". Không thể chỉnh sửa.");
            }
            po.setQuantity(newQuantity);
            po.setUnitPrice(newUnitPrice);
        }

        // [EX-02] Timeout quy trình vòng đời (Batch job ngầm gọi hàm này)
        public void triggerDailyTimeoutCheck(PurchaseOrder po, int passedDays) {
            if (po.getStatus() == PoStatus.PENDING_DIRECTOR) {
                po.setDaysInPending(po.getDaysInPending() + passedDays);
                if (po.getDaysInPending() > 7) {
                    po.setStatus(PoStatus.EXPIRED);
                }
            }
        }

        // [Rule 2.2] Đẩy PO lên Giám đốc
        public void forwardToDirector(User user, PurchaseOrder po) {
            checkExpired(po);

            if (user.getRole() == Role.STAFF) {
                throw new UnauthorizedException("Nhân viên kho không có quyền chuyển trạng thái.");
            }

            if (user.getRole() == Role.MANAGER) {
                if (po.getStatus() != PoStatus.DRAFT) {
                    throw new UnauthorizedException("Manager chỉ có thể đẩy PO từ trạng thái DRAFT.");
                }
                po.setStatus(PoStatus.PENDING_DIRECTOR);
            }
        }

        // [Rule 2.2] Ma trận phê duyệt
        public void approvePO(User user, PurchaseOrder po) {
            checkExpired(po);

            if (user.getRole() == Role.STAFF) {
                throw new UnauthorizedException("Nhân viên kho không có quyền phê duyệt.");
            }

            if (user.getRole() == Role.MANAGER) {
                if (po.getStatus() != PoStatus.DRAFT) {
                    throw new UnauthorizedException("Manager chỉ có thể duyệt PO ở trạng thái DRAFT.");
                }
                
                double totalValue = calculateTotalValue(po);
                if (totalValue > MANAGER_APPROVAL_LIMIT) {
                    throw new UnauthorizedException("Vượt quá thẩm quyền. Tổng PO > 20.000.000 VNĐ, Manager chỉ được chuyển sang PENDING_DIRECTOR.");
                }
                po.setStatus(PoStatus.APPROVED);
            } 
            else if (user.getRole() == Role.DIRECTOR) {
                if (po.getStatus() == PoStatus.DRAFT || po.getStatus() == PoStatus.PENDING_DIRECTOR) {
                    po.setStatus(PoStatus.APPROVED);
                } else {
                    throw new UnauthorizedException("Director chỉ có thể duyệt PO đang ở DRAFT hoặc PENDING_DIRECTOR.");
                }
            }
        }

        // [Rule 2.2] Từ chối PO thủ công
        public void rejectPO(User user, PurchaseOrder po) {
            checkExpired(po);

            if (user.getRole() == Role.DIRECTOR) {
                if (po.getStatus() == PoStatus.DRAFT || po.getStatus() == PoStatus.PENDING_DIRECTOR) {
                    po.setStatus(PoStatus.REJECTED);
                } else {
                    throw new UnauthorizedException("Trạng thái hiện tại không hợp lệ để từ chối.");
                }
            } else {
                throw new UnauthorizedException("Chỉ Director mới có quyền từ chối PO.");
            }
        }

        // Hàm tiện ích kiểm tra nhanh trạng thái Timeout
        private void checkExpired(PurchaseOrder po) {
            if (po.getStatus() == PoStatus.EXPIRED) {
                throw new UnauthorizedException("PO đã hết hạn (EXPIRED). Mọi quyền phê duyệt bị tước bỏ.");
            }
        }
    }
}
