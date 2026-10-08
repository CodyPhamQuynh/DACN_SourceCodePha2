import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AffiliateCommissionSystemTest {

    private final AffiliateCommissionSystem system = new AffiliateCommissionSystem();

    @Test
    public void testMainFlowFashionGoldFirstBuyer() {
        long result = system.calculateCommission(2000000L, AffiliateLevel.GOLD, ProductCategory.FASHION, true);
        assertEquals(350000L, result);
    }

    @Test
    public void testExceptionLowValueOrder() {
        assertThrows(LowValueOrderException.class, () -> {
            system.calculateCommission(100000L, AffiliateLevel.GOLD, ProductCategory.FASHION, true);
        });
    }

    @Test
    public void testBoundaryMinOrderSilverFashion() {
        long result = system.calculateCommission(150000L, AffiliateLevel.SILVER, ProductCategory.FASHION, false);
        assertEquals(15000L, result);
    }

    @Test
    public void testBoundaryBelowMinOrder() {
        assertThrows(LowValueOrderException.class, () -> {
            system.calculateCommission(149999L, AffiliateLevel.SILVER, ProductCategory.FASHION, false);
        });
    }

    @Test
    public void testCrossConditionElectronicsBronze() {
        long result = system.calculateCommission(1000000L, AffiliateLevel.BRONZE, ProductCategory.ELECTRONICS, true);
        assertEquals(0L, result);
    }

    @Test
    public void testCrossConditionMaxCapFashion() {
        long result = system.calculateCommission(5000000L, AffiliateLevel.GOLD, ProductCategory.FASHION, false);
        assertEquals(500000L, result);
    }

    @Test
    public void testCrossConditionDigitalSoftwareNoCap() {
        long result = system.calculateCommission(10000000L, AffiliateLevel.BRONZE, ProductCategory.DIGITAL_SOFTWARE, false);
        assertEquals(2000000L, result);
    }

    @Test
    public void testCrossConditionFirstBuyerOverrideTrap() {
        assertThrows(LowValueOrderException.class, () -> {
            system.calculateCommission(50000L, AffiliateLevel.BRONZE, ProductCategory.ELECTRONICS, true);
        });
    }

    @Test
    public void testBoundaryElectronicsSilver() {
        long result = system.calculateCommission(1000000L, AffiliateLevel.SILVER, ProductCategory.ELECTRONICS, false);
        assertEquals(20000L, result);
    }

    @Test
    public void testExceptionFashionBronze() {
        long result = system.calculateCommission(500000L, AffiliateLevel.BRONZE, ProductCategory.FASHION, false);
        assertEquals(25000L, result);
    }
}
