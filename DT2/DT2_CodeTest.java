import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryApprovalWorkflowTest {

    private InventoryApprovalWorkflow.InventoryApprovalService service;

    @BeforeEach
    public void setUp() {
        service = new InventoryApprovalWorkflow.InventoryApprovalService();
    }

    @Test
    public void testTaoPhieuNhapKhoThongThuongThanhCongBoiStaff() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Laptop", "Điện tử", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 10, 1000000.0);
        
        assertNotNull(po);
        assertEquals(InventoryApprovalWorkflow.PoStatus.DRAFT, po.getStatus());
        double totalValue = service.calculateTotalValue(po);
        assertEquals(10000000.0, totalValue);
    }

    @Test
    public void testQuanLyDuyetPOGiaTriNhoHonHoacBang20TrieuVND() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Mouse", "Phụ kiện", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 20, 1000000.0);
        service.approvePO(manager, po);
        
        assertEquals(InventoryApprovalWorkflow.PoStatus.APPROVED, po.getStatus());
    }

    @Test
    public void testGiamDocPheDuyetPOGiaTriLon() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.User director = new InventoryApprovalWorkflow.User("directorUser", InventoryApprovalWorkflow.Role.DIRECTOR);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Server", "IT", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 50, 1000000.0);
        service.forwardToDirector(manager, po);
        assertEquals(InventoryApprovalWorkflow.PoStatus.PENDING_DIRECTOR, po.getStatus());
        
        service.approvePO(director, po);
        assertEquals(InventoryApprovalWorkflow.PoStatus.APPROVED, po.getStatus());
    }

    @Test
    public void testStaffCoGangChuyenTrangThaiPOTrucTiepSangAPPROVED() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Keyboard", "Phụ kiện", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 5, 1000000.0);
        
        assertThrows(InventoryApprovalWorkflow.UnauthorizedException.class, () -> {
            service.approvePO(staff, po);
        });
        assertEquals(InventoryApprovalWorkflow.PoStatus.DRAFT, po.getStatus());
    }

    @Test
    public void testKiemSoatDanhMucHoaChatDeChay() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Acetone", "Hóa chất dễ cháy", false);
        
        InventoryApprovalWorkflow.SecurityException exception = assertThrows(InventoryApprovalWorkflow.SecurityException.class, () -> {
            service.createPO(staff, product, 5, 1000000.0);
        });
        
        assertTrue(exception.getMessage().contains("ERR-SEC-01"));
    }

    @Test
    public void testCoGangChinhSuaDuLieuPODaBiDongBang() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Monitor", "Điện tử", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 10, 1000000.0);
        service.approvePO(manager, po);
        
        assertThrows(InventoryApprovalWorkflow.FrozenDataException.class, () -> {
            service.updatePOData(po, 15, 1000000.0);
        });
    }

    @Test
    public void testThaoTacPheDuyetTrenPODaHetHan() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.User director = new InventoryApprovalWorkflow.User("directorUser", InventoryApprovalWorkflow.Role.DIRECTOR);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Printer", "Văn phòng phẩm", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 30, 1000000.0);
        service.forwardToDirector(manager, po);
        service.triggerDailyTimeoutCheck(po, 8);
        
        assertThrows(InventoryApprovalWorkflow.UnauthorizedException.class, () -> {
            service.approvePO(director, po);
        });
    }

    @Test
    public void testBienGiaTriPheDuyetCuaManagerTaiMoc20TrieuVND() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Item", "Khác", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 20, 1000000.0);
        service.approvePO(manager, po);
        
        assertEquals(InventoryApprovalWorkflow.PoStatus.APPROVED, po.getStatus());
    }

    @Test
    public void testBienGiaTriBatBuocChuyenDirectorTaiMocSatRat20Trieu01VND() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Item", "Khác", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 20, 1000000.0);
        po.setUnitPrice(1000000.05); // Total value > 20,000,000
        
        assertThrows(InventoryApprovalWorkflow.UnauthorizedException.class, () -> {
            service.approvePO(manager, po);
        });
    }

    @Test
    public void testKiemTraPhiLuuKhoTaiMocSanPhamCoGanTheHangCongKenh() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Large Crate", "Kho", true);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 1, 1000000.0);
        double totalValue = service.calculateTotalValue(po);
        
        assertEquals(1500000.0, totalValue);
    }

    @Test
    public void testTimeoutQuyTrinhTaiMocDung7Ngay() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Item", "Khác", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 30, 1000000.0);
        service.forwardToDirector(manager, po);
        
        service.triggerDailyTimeoutCheck(po, 7);
        assertEquals(InventoryApprovalWorkflow.PoStatus.PENDING_DIRECTOR, po.getStatus());
        
        service.triggerDailyTimeoutCheck(po, 1);
        assertEquals(InventoryApprovalWorkflow.PoStatus.EXPIRED, po.getStatus());
    }

    @Test
    public void testToHopManagerXuLyPOGiaTriLonVaCoHangCongKenh() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Furniture", "Nội thất", true);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 20, 1000000.0);
        
        assertThrows(InventoryApprovalWorkflow.UnauthorizedException.class, () -> {
            service.approvePO(manager, po);
        });
        
        service.forwardToDirector(manager, po);
        assertEquals(InventoryApprovalWorkflow.PoStatus.PENDING_DIRECTOR, po.getStatus());
    }

    @Test
    public void testToHopDirectorTuChoiPOGiaTriNho() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.User manager = new InventoryApprovalWorkflow.User("managerUser", InventoryApprovalWorkflow.Role.MANAGER);
        InventoryApprovalWorkflow.User director = new InventoryApprovalWorkflow.User("directorUser", InventoryApprovalWorkflow.Role.DIRECTOR);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Book", "Văn phòng phẩm", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 5, 1000000.0);
        service.forwardToDirector(manager, po);
        
        service.rejectPO(director, po);
        assertEquals(InventoryApprovalWorkflow.PoStatus.REJECTED, po.getStatus());
    }

    @Test
    public void testToHopStaffSuaDoiDuLieuPOKhiTrangThaiHopLe() {
        InventoryApprovalWorkflow.User staff = new InventoryApprovalWorkflow.User("staffUser", InventoryApprovalWorkflow.Role.STAFF);
        InventoryApprovalWorkflow.Product product = new InventoryApprovalWorkflow.Product("Pen", "Văn phòng phẩm", false);
        
        InventoryApprovalWorkflow.PurchaseOrder po = service.createPO(staff, product, 10, 1000.0);
        assertEquals(10000.0, service.calculateTotalValue(po));
        
        service.updatePOData(po, 20, 2000.0);
        assertEquals(20, po.getQuantity());
        assertEquals(2000.0, po.getUnitPrice());
        assertEquals(40000.0, service.calculateTotalValue(po));
    }
}
