import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FlightRefundSystemTest {

    private FlightRefundSystem refundSystem;

    @BeforeEach
    public void setUp() {
        refundSystem = new FlightRefundSystem();
    }

    @Test
    public void testHuyVeHangBusinessTruocGioKhoiHanh72hChoKhachRegular() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            5000000L,
            FlightRefundSystem.TicketClass.BUSINESS,
            72,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(4800000L, refundAmount);
    }

    @Test
    public void testHuyVeHangSaverTruocGioKhoiHanhDu24h() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            2000000L,
            FlightRefundSystem.TicketClass.SAVER,
            12,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(0L, refundAmount);
    }

    @Test
    public void testBienThoiGian72hChoHangFlex() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            3000000L,
            FlightRefundSystem.TicketClass.FLEX,
            72,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(2500000L, refundAmount);
    }

    @Test
    public void testBienThoiGian72hSatNutOTrenMoc71hChoHangBusiness() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            4000000L,
            FlightRefundSystem.TicketClass.BUSINESS,
            71,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(3000000L, refundAmount);
    }

    @Test
    public void testBienThoiGian24hChoHangSaver() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            2000000L,
            FlightRefundSystem.TicketClass.SAVER,
            24,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(0L, refundAmount);
    }

    @Test
    public void testBienThoiGian24hSatNutOTrenMoc23hChoHangFlex() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            2000000L,
            FlightRefundSystem.TicketClass.FLEX,
            23,
            FlightRefundSystem.CustomerTier.REGULAR,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(0L, refundAmount);
    }

    @Test
    public void testKhachPlatinumHuyVeCoPhatSinhHoanTien() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            2000000L,
            FlightRefundSystem.TicketClass.SAVER,
            100,
            FlightRefundSystem.CustomerTier.PLATINUM,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(1400000L, refundAmount);
    }

    @Test
    public void testBatKhaKhangKichHoatChoVeFlexHuyGap() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            3000000L,
            FlightRefundSystem.TicketClass.FLEX,
            5,
            FlightRefundSystem.CustomerTier.REGULAR,
            true
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(3000000L, refundAmount);
    }

    @Test
    public void testNgoaiLeCuaNgoaiLeBatKhaKhangChoVeSaverHuySatGio() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            2000000L,
            FlightRefundSystem.TicketClass.SAVER,
            10,
            FlightRefundSystem.CustomerTier.REGULAR,
            true
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(0L, refundAmount);
    }

    @Test
    public void testHuyVeHangFlexTrongKhoang24hDen72hChoKhachPlatinum() {
        FlightRefundSystem.RefundRequest request = new FlightRefundSystem.RefundRequest(
            5000000L,
            FlightRefundSystem.TicketClass.FLEX,
            48,
            FlightRefundSystem.CustomerTier.PLATINUM,
            false
        );

        long refundAmount = refundSystem.calculateRefund(request);
        assertEquals(2500000L, refundAmount);
    }
}
