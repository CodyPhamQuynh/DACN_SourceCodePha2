import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoanOverduePenaltySystemTest {

    private LoanOverduePenaltySystem penaltySystem;

    @BeforeEach
    public void setUp() {
        penaltySystem = new LoanOverduePenaltySystem();
    }

    @Test
    public void testStandardLoanLowOverdueGracePeriod() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 2, 
            LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
            LoanOverduePenaltySystem.CollateralType.REAL_ESTATE
        );
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testBadDebtLongOverdueWithCollateral() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 40, 
            LoanOverduePenaltySystem.CustomerGroup.BAD_DEBT, 
            LoanOverduePenaltySystem.CollateralType.REAL_ESTATE
        );
        assertEquals(3500000.0, result, 0.001);
    }

    @Test
    public void testInvalidLoanAmountZero() {
        assertThrows(LoanOverduePenaltySystem.InvalidLoanAmountException.class, () -> {
            penaltySystem.calculateFinalPenaltyAmount(
                0.0, 5, 
                LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
                LoanOverduePenaltySystem.CollateralType.NONE
            );
        });
    }

    @Test
    public void testInvalidDateNegative() {
        assertThrows(LoanOverduePenaltySystem.InvalidDateException.class, () -> {
            penaltySystem.calculateFinalPenaltyAmount(
                50000000.0, -1, 
                LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
                LoanOverduePenaltySystem.CollateralType.VEHICLE
            );
        });
    }

    @Test
    public void testBoundaryStandardThreeDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 3, 
            LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testBoundaryStandardFourDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 4, 
            LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(100000.0, result, 0.001);
    }

    @Test
    public void testBoundaryTenDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 10, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(100000.0, result, 0.001);
    }

    @Test
    public void testBoundaryElevenDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 11, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(550000.0, result, 0.001);
    }

    @Test
    public void testBoundaryThirtyDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 30, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(1500000.0, result, 0.001);
    }

    @Test
    public void testBoundaryThirtyOneDays() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 31, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(3050000.0, result, 0.001);
    }

    @Test
    public void testMaxCapPenaltyInterest() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 300, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.NONE
        );
        assertEquals(12000000.0, result, 0.001);
    }

    @Test
    public void testBadDebtRealEstateOverride() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 20, 
            LoanOverduePenaltySystem.CustomerGroup.BAD_DEBT, 
            LoanOverduePenaltySystem.CollateralType.REAL_ESTATE
        );
        assertEquals(1000000.0, result, 0.001);
    }

    @Test
    public void testWarningRealEstateDiscount() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 20, 
            LoanOverduePenaltySystem.CustomerGroup.WARNING, 
            LoanOverduePenaltySystem.CollateralType.REAL_ESTATE
        );
        assertEquals(800000.0, result, 0.001);
    }

    @Test
    public void testStandardVehicleDiscountOutsideGrace() {
        double result = penaltySystem.calculateFinalPenaltyAmount(
            100000000.0, 20, 
            LoanOverduePenaltySystem.CustomerGroup.STANDARD, 
            LoanOverduePenaltySystem.CollateralType.VEHICLE
        );
        assertEquals(900000.0, result, 0.001);
    }
}
