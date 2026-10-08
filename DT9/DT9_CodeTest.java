import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryFineSystemTest {

    private LibraryFineSystem fineSystem;

    @BeforeEach
    public void setUp() {
        fineSystem = new LibraryFineSystem();
    }

    @Test
    public void testTinhTienPhatSachThuongTraTreBinhThuong() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                5, 
                LibraryFineSystem.BookType.STANDARD, 
                200000L, 
                DayOfWeek.WEDNESDAY, 
                1
        );
        assertEquals(25000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testTraSachSomHoacDungHanDaysLateAm() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                -2, 
                LibraryFineSystem.BookType.STANDARD, 
                100000L, 
                DayOfWeek.MONDAY, 
                0
        );
        assertEquals(0L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testXuLySachLuuChieuPhatSinhTreHan() {
        Exception exception = assertThrows(LibraryFineSystem.InvalidBorrowException.class, () -> {
            fineSystem.calculateFine(
                    2, 
                    LibraryFineSystem.BookType.ARCHIVAL, 
                    500000L, 
                    DayOfWeek.TUESDAY, 
                    0
            );
        });
        assertEquals("Lỗi hệ thống: Sách Lưu Chiểu không được phép phát sinh ngày trễ hạn", exception.getMessage());
    }

    @Test
    public void testKiemTraGiaTriBienSoNgayTreBang0() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                0, 
                LibraryFineSystem.BookType.STANDARD, 
                100000L, 
                DayOfWeek.TUESDAY, 
                0
        );
        assertEquals(0L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testKiemTraGiaTriBienNgayTraLaThu7ApDungAnHan() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                5, 
                LibraryFineSystem.BookType.STANDARD, 
                200000L, 
                DayOfWeek.SATURDAY, 
                0
        );
        assertEquals(20000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testKiemTraGiaTriBienNgayTraLaChuNhatApDungAnHan() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                5, 
                LibraryFineSystem.BookType.REFERENCE, 
                200000L, 
                DayOfWeek.SUNDAY, 
                0
        );
        assertEquals(40000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testKiemTraGiaTriBienSoNgayTreDung1NgayVaoCuoiTuânDaysLateTinhTien0() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                1, 
                LibraryFineSystem.BookType.STANDARD, 
                100000L, 
                DayOfWeek.SATURDAY, 
                0
        );
        assertEquals(0L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testDatHanMucPhatFineCapChoSachThuong() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                30, 
                LibraryFineSystem.BookType.STANDARD, 
                100000L, 
                DayOfWeek.TUESDAY, 
                0
        );
        assertEquals(100000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.LOCKED, result.getCardStatus());
    }

    @Test
    public void testDatHanMucPhatFineCapChoSachThamKhao() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                30, 
                LibraryFineSystem.BookType.REFERENCE, 
                250000L, 
                DayOfWeek.TUESDAY, 
                0
        );
        assertEquals(250000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.LOCKED, result.getCardStatus());
    }

    @Test
    public void testKhoaTheDoSoNgayTreLonHonHoacBang30Ngay() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                30, 
                LibraryFineSystem.BookType.STANDARD, 
                500000L, 
                DayOfWeek.TUESDAY, 
                0
        );
        assertEquals(150000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.LOCKED, result.getCardStatus());
    }

    @Test
    public void testKhoaTheSatRanhGioiTre29Ngay() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                29, 
                LibraryFineSystem.BookType.STANDARD, 
                500000L, 
                DayOfWeek.TUESDAY, 
                0
        );
        assertEquals(145000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testToHopTaiPhamVaPhuPhiCoDinh() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                5, 
                LibraryFineSystem.BookType.STANDARD, 
                200000L, 
                DayOfWeek.TUESDAY, 
                3
        );
        assertEquals(75000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testToHopTaiPhamVoiTienPhatLonHonHoacBang100000VNDDanDenKhoaThe() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                6, 
                LibraryFineSystem.BookType.REFERENCE, 
                500000L, 
                DayOfWeek.TUESDAY, 
                3
        );
        assertEquals(110000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.LOCKED, result.getCardStatus());
    }

    @Test
    public void testToHopTaiPhamNhungTienPhatDuoi100000VNDVaDaysLateNhoHon30KhongKhoaThe() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                4, 
                LibraryFineSystem.BookType.STANDARD, 
                500000L, 
                DayOfWeek.TUESDAY, 
                3
        );
        assertEquals(70000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.ACTIVE, result.getCardStatus());
    }

    @Test
    public void testToHopAnHanCuoiTuânTaiPhamVaChamMocFineCap() {
        LibraryFineSystem.ReturnResult result = fineSystem.calculateFine(
                20, 
                LibraryFineSystem.BookType.REFERENCE, 
                150000L, 
                DayOfWeek.SUNDAY, 
                3
        );
        assertEquals(150000L, result.getFineAmount());
        assertEquals(LibraryFineSystem.CardStatus.LOCKED, result.getCardStatus());
    }
}
