import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DualVoucherSystemTest {

    private DualVoucherSystem system;

    @BeforeEach
    public void setUp() {
        system = new DualVoucherSystem();
    }

    @Test
    public void testGioHangCoGiaTriNhoHonHoacBang0() {
        assertThrows(DualVoucherSystem.InvalidCartException.class, () -> {
            system.calculateFinalAmount(0, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, null, null);
        });
    }

    @Test
    public void testGioHangGiaTriAm() {
        assertThrows(DualVoucherSystem.InvalidCartException.class, () -> {
            system.calculateFinalAmount(-100, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, null, null);
        });
    }

    @Test
    public void testLuongChinhKhongDungVoucher() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(300000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, null, null);
        assertEquals(300000, result.getFinalAmount());
    }

    @Test
    public void testApDungShop50KDatChuan() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(250000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_50K, null);
        assertEquals(50000, result.getShopDiscount());
        assertEquals(200000, result.getFinalAmount());
    }

    @Test
    public void testBienDuoiApDungShop50K() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(200000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_50K, null);
        assertEquals(50000, result.getShopDiscount());
        assertEquals(150000, result.getFinalAmount());
    }

    @Test
    public void testDuBienApDungShop50K() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(199999, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_50K, null);
        });
    }

    @Test
    public void testApDungShop10PTDuoiMucToiDa() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(200000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_10PT, null);
        assertEquals(20000, result.getShopDiscount());
        assertEquals(180000, result.getFinalAmount());
    }

    @Test
    public void testApDungShop10PTDatMucToiDa30000() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(400000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_10PT, null);
        assertEquals(30000, result.getShopDiscount());
        assertEquals(370000, result.getFinalAmount());
    }

    @Test
    public void testBienDuoiApDungShop10PT() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(150000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_10PT, null);
        assertEquals(15000, result.getShopDiscount());
        assertEquals(135000, result.getFinalAmount());
    }

    @Test
    public void testDuBienApDungShop10PT() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(149999, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_10PT, null);
        });
    }

    @Test
    public void testNganHangElectronicsGhiDeShopVoucher() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(500000, DualVoucherSystem.Category.ELECTRONICS, DualVoucherSystem.PaymentMethod.COD, DualVoucherSystem.ShopVoucher.SHOP_50K, null);
        assertEquals(0, result.getShopDiscount());
        assertEquals(500000, result.getFinalAmount());
    }

    @Test
    public void testApDungPlatformFreeshipVoiVnpay() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(150000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_FREESHIP);
        assertEquals(30000, result.getPlatformDiscount());
        assertEquals(120000, result.getFinalAmount());
    }

    @Test
    public void testApDungPlatformFreeshipVoiCreditCard() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(150000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.CREDIT_CARD, null, DualVoucherSystem.PlatformVoucher.PLATFORM_FREESHIP);
        assertEquals(30000, result.getPlatformDiscount());
        assertEquals(120000, result.getFinalAmount());
    }

    @Test
    public void testLoiPlatformFreeshipVoiCod() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(150000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.COD, null, DualVoucherSystem.PlatformVoucher.PLATFORM_FREESHIP);
        });
    }

    @Test
    public void testBienDuoiApDungPlatformFreeship() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(100000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_FREESHIP);
        assertEquals(30000, result.getPlatformDiscount());
        assertEquals(70000, result.getFinalAmount());
    }

    @Test
    public void testDuBienApDungPlatformFreeship() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(99999, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_FREESHIP);
        });
    }

    @Test
    public void testApDungPlatform20PtDungDieuKienVnpay() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(600000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        assertEquals(120000, result.getPlatformDiscount());
        assertEquals(480000, result.getFinalAmount());
    }

    @Test
    public void testApDungPlatform20PtChamMucToiDa100000() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(1000000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        assertEquals(100000, result.getPlatformDiscount());
        assertEquals(900000, result.getFinalAmount());
    }

    @Test
    public void testLoiPlatform20PtVoiCreditCard() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(600000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.CREDIT_CARD, null, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        });
    }

    @Test
    public void testBienDuoiApDungPlatform20Pt() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(500000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        assertEquals(100000, result.getPlatformDiscount());
        assertEquals(400000, result.getFinalAmount());
    }

    @Test
    public void testDuBienApDungPlatform20Pt() {
        assertThrows(DualVoucherSystem.VoucherConditionException.class, () -> {
            system.calculateFinalAmount(499999, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, null, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        });
    }

    @Test
    public void testToHopApDungCaShopVoucherVaPlatformVoucherTheoDungTrinhTu() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(500000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, DualVoucherSystem.ShopVoucher.SHOP_50K, DualVoucherSystem.PlatformVoucher.PLATFORM_20PT);
        assertEquals(50000, result.getShopDiscount());
        assertEquals(90000, result.getPlatformDiscount());
        assertEquals(360000, result.getFinalAmount());
    }

    @Test
    public void testKiemTraChongAmTienKhiTongGiamGiaVượtQuaGiaTriDonHang() {
        DualVoucherSystem.CheckoutResult result = system.calculateFinalAmount(40000, DualVoucherSystem.Category.FASHION, DualVoucherSystem.PaymentMethod.VNPAY, DualVoucherSystem.ShopVoucher.SHOP_50K, null);
        assertEquals(0, result.getFinalAmount());
    }
}
