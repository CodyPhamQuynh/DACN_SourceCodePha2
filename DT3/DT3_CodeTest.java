import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CartCalculationServiceTest {

    private CartCalculationService cartCalculationService;

    @BeforeEach
    public void setUp() {
        cartCalculationService = new CartCalculationService();
    }

    @Test
    public void testMainFlowNewCustomerCOD() {
        double result = cartCalculationService.calculateFinalAmount(
            200000, 
            CartCalculationService.CustomerTier.NEW, 
            CartCalculationService.PaymentMethod.COD, 
            false
        );
        assertEquals(230000, result, 0.001);
    }

    @Test
    public void testExceptionOrderTotalBelowMinimum() {
        assertThrows(CartCalculationService.InvalidOrderException.class, () -> {
            cartCalculationService.calculateFinalAmount(
                49999, 
                CartCalculationService.CustomerTier.NEW, 
                CartCalculationService.PaymentMethod.COD, 
                false
            );
        });
    }

    @Test
    public void testBoundaryOrderTotalAtMinimum() {
        double result = cartCalculationService.calculateFinalAmount(
            50000, 
            CartCalculationService.CustomerTier.NEW, 
            CartCalculationService.PaymentMethod.COD, 
            false
        );
        assertEquals(80000, result, 0.001);
    }

    @Test
    public void testBoundaryOrderTotalAtFreeship() {
        double result = cartCalculationService.calculateFinalAmount(
            500000, 
            CartCalculationService.CustomerTier.NEW, 
            CartCalculationService.PaymentMethod.COD, 
            false
        );
        assertEquals(500000, result, 0.001);
    }

    @Test
    public void testBoundaryOrderTotalJustBelowFreeship() {
        double result = cartCalculationService.calculateFinalAmount(
            499999, 
            CartCalculationService.CustomerTier.NEW, 
            CartCalculationService.PaymentMethod.COD, 
            false
        );
        assertEquals(529999, result, 0.001);
    }

    @Test
    public void testCrossConditionSilverCustomerNormalOrder() {
        double result = cartCalculationService.calculateFinalAmount(
            400000, 
            CartCalculationService.CustomerTier.SILVER, 
            CartCalculationService.PaymentMethod.CREDIT_CARD, 
            false
        );
        assertEquals(410000, result, 0.001);
    }

    @Test
    public void testCrossConditionSilverCustomerMaxCap() {
        double result = cartCalculationService.calculateFinalAmount(
            2000000, 
            CartCalculationService.CustomerTier.SILVER, 
            CartCalculationService.PaymentMethod.CREDIT_CARD, 
            false
        );
        assertEquals(1950000, result, 0.001);
    }

    @Test
    public void testCrossConditionGoldCustomerNormalOrder() {
        double result = cartCalculationService.calculateFinalAmount(
            600000, 
            CartCalculationService.CustomerTier.GOLD, 
            CartCalculationService.PaymentMethod.CREDIT_CARD, 
            false
        );
        assertEquals(540000, result, 0.001);
    }

    @Test
    public void testCrossConditionGoldCustomerMaxCap() {
        double result = cartCalculationService.calculateFinalAmount(
            1500000, 
            CartCalculationService.CustomerTier.GOLD, 
            CartCalculationService.PaymentMethod.CREDIT_CARD, 
            false
        );
        assertEquals(1400000, result, 0.001);
    }

    @Test
    public void testCrossConditionEWalletDiscountShipping() {
        double result = cartCalculationService.calculateFinalAmount(
            300000, 
            CartCalculationService.CustomerTier.NEW, 
            CartCalculationService.PaymentMethod.E_WALLET, 
            false
        );
        assertEquals(315000, result, 0.001);
    }

    @Test
    public void testCrossConditionFlashSaleCancelDiscount() {
        double result = cartCalculationService.calculateFinalAmount(
            1000000, 
            CartCalculationService.CustomerTier.GOLD, 
            CartCalculationService.PaymentMethod.COD, 
            true
        );
        assertEquals(1000000, result, 0.001);
    }
}
