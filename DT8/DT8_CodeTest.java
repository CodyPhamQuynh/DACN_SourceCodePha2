import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExpressLogisticsSystemTest {

    private ExpressLogisticsSystem logisticsSystem;

    @BeforeEach
    public void setUp() {
        logisticsSystem = new ExpressLogisticsSystem();
    }

    @Test
    public void testCalculateFinalShippingFee_StandardSmallPackageNormalWeather() {
        double fee = logisticsSystem.calculateFinalShippingFee(3.0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(40000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_InvalidMeasurementWeightZero() {
        assertThrows(ExpressLogisticsSystem.InvalidMeasurementException.class, () -> {
            logisticsSystem.calculateFinalShippingFee(0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        });
    }

    @Test
    public void testCalculateFinalShippingFee_InvalidMeasurementLengthZero() {
        assertThrows(ExpressLogisticsSystem.InvalidMeasurementException.class, () -> {
            logisticsSystem.calculateFinalShippingFee(3.0, 0, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        });
    }

    @Test
    public void testCalculateFinalShippingFee_OverweightException() {
        assertThrows(ExpressLogisticsSystem.OverweightException.class, () -> {
            logisticsSystem.calculateFinalShippingFee(51.0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        });
    }

    @Test
    public void testCalculateFinalShippingFee_WeightBoundary50kg() {
        double fee = logisticsSystem.calculateFinalShippingFee(50.0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(490000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_WeightBoundary5kg() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(40000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_WeightBoundary6kg() {
        double fee = logisticsSystem.calculateFinalShippingFee(6.0, 20, 20, 20, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(50000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_VolumetricWeightLarger() {
        double fee = logisticsSystem.calculateFinalShippingFee(2.0, 40, 40, 40, 30.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(120000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_ExtremeWeather() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 30.0, true, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(60000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_DistanceBoundary50km() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 50.0, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(40000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_DistanceBoundary50Point1km() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 50.1, false, ExpressLogisticsSystem.CustomerTier.STANDARD);
        assertEquals(90000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_B2BPartnerDistanceFree() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 60.0, false, ExpressLogisticsSystem.CustomerTier.B2B_PARTNER);
        assertEquals(40000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_VIPDiscount() {
        double fee = logisticsSystem.calculateFinalShippingFee(5.0, 20, 20, 20, 60.0, false, ExpressLogisticsSystem.CustomerTier.VIP);
        assertEquals(72000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_HighRiskTrapB2B() {
        double fee = logisticsSystem.calculateFinalShippingFee(30.0, 20, 20, 20, 60.0, false, ExpressLogisticsSystem.CustomerTier.B2B_PARTNER);
        assertEquals(440000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_HighRiskTrapVIPWithWeather() {
        double fee = logisticsSystem.calculateFinalShippingFee(30.0, 20, 20, 20, 60.0, true, ExpressLogisticsSystem.CustomerTier.VIP);
        assertEquals(585000.0, fee, 0.001);
    }

    @Test
    public void testCalculateFinalShippingFee_HighRiskTrapBoundaryNotActivated() {
        double fee = logisticsSystem.calculateFinalShippingFee(29.0, 20, 20, 20, 50.1, false, ExpressLogisticsSystem.CustomerTier.VIP);
        assertEquals(264000.0, fee, 0.001);
    }
}
