package entitlement.tests;

import java.util.Set;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import entitlement.config.AccountType;
import entitlement.config.AdEntitlement;
import entitlement.config.ContentAccess;
import entitlement.config.PlanConfig;
import entitlement.config.PlanConfig.VodTier;

/** Pure configuration tests: no TestBase, browser, credentials, or external services. */
public class PlanConfigTest {

    /** Independent expected rows transcribed from the subscription matrix. */
    @DataProvider(name = "paidPlanMatrix")
    public Object[][] paidPlanMatrix() {
        return new Object[][] {
                {"combo_annual_platinum", "Combo Annual Platinum",
                        ContentAccess.FULL, ContentAccess.FULL,
                        AdEntitlement.AD_FREE, 4, 4, true, VodTier.PREMIUM},
                {"combo_quarterly_platinum", "Combo Quarterly Platinum",
                        ContentAccess.FULL, ContentAccess.FULL,
                        AdEntitlement.WITH_ADS, 2, 1, true, VodTier.LITE},
                {"sn_annual_premium", "SN Annual Premium",
                        ContentAccess.FULL, ContentAccess.FREE_CATALOG,
                        AdEntitlement.AD_FREE, 3, 2, true, VodTier.PREMIUM},
                {"sn_quarterly_lite", "SN Quarterly Lite",
                        ContentAccess.FULL, ContentAccess.FREE_CATALOG,
                        AdEntitlement.WITH_ADS, 2, 1, true, VodTier.LITE},
                {"sns_annual_premium", "SNS Annual Premium",
                        ContentAccess.FREE_CATALOG, ContentAccess.FULL,
                        AdEntitlement.AD_FREE, 3, 2, false, VodTier.PREMIUM},
                {"sns_quarterly_lite", "SNS Quarterly Lite",
                        ContentAccess.FREE_CATALOG, ContentAccess.FULL,
                        AdEntitlement.WITH_ADS, 2, 1, false, VodTier.LITE}
        };
    }

    @Test(dataProvider = "paidPlanMatrix")
    public void mapsEveryPaidPlan(String id, String name,
            ContentAccess snAccess, ContentAccess snsAccess,
            AdEntitlement ads, int loginLimit, int concurrentLimit,
            boolean liveTvAccess, VodTier vodTier) {
        PlanConfig plan = PlanConfig.forId(id);

        Assert.assertEquals(plan.planId(), id, "Stable plan ID");
        Assert.assertEquals(plan.planName(), name, "Exact matrix display name");
        Assert.assertEquals(plan.accountType(), AccountType.PAID);
        Assert.assertEquals(plan.snAccess(), snAccess, "SN catalog access");
        Assert.assertEquals(plan.snsAccess(), snsAccess, "SNS catalog access");
        Assert.assertEquals(plan.adEntitlement(), ads);
        Assert.assertEquals(plan.loginLimit(), loginLimit, "Login limit");
        Assert.assertEquals(plan.concurrentPlaybackLimit(), concurrentLimit,
                "Concurrent playback limit must be mapped separately from login limit");
        Assert.assertEquals(plan.liveTvAccess(), liveTvAccess);
        Assert.assertEquals(plan.maxWebQuality(), 1080, "Web cap applies to every plan");
        Assert.assertEquals(plan.vodTier(), vodTier);
        Assert.assertEquals(plan.allowedVodsThreshold(), 200, "Paid VOD threshold");
    }

    @Test
    public void containsExactlyTheSixPaidPlans() {
        Set<String> expectedIds = Set.of(
                "combo_annual_platinum", "combo_quarterly_platinum",
                "sn_annual_premium", "sn_quarterly_lite",
                "sns_annual_premium", "sns_quarterly_lite");
        Set<String> actualIds = PlanConfig.paidPlans().stream()
                .map(PlanConfig::planId).collect(Collectors.toSet());

        Assert.assertEquals(PlanConfig.paidPlans().size(), 6, "No duplicate or extra paid plans");
        Assert.assertEquals(actualIds, expectedIds);
    }

    @DataProvider(name = "vodThresholds")
    public Object[][] vodThresholds() {
        return new Object[][] {
                {VodTier.FREE, 30},
                {VodTier.GUEST, 30},
                {VodTier.LITE, 200},
                {VodTier.BASIC, 200},
                {VodTier.PREMIUM, 200}
        };
    }

    @Test(dataProvider = "vodThresholds")
    public void preservesSuppliedVodThresholds(VodTier tier, int expectedThreshold) {
        Assert.assertEquals(tier.allowedVodsThreshold(), expectedThreshold);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void preventsModificationOfPaidPlanCatalog() {
        PlanConfig.paidPlans().clear();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsUnknownPlanInsteadOfDefaulting() {
        PlanConfig.forId("unknown_plan");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void rejectsNullPlanId() {
        PlanConfig.forId(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsMobileOrTvQualityForWeb() {
        PlanConfig existing = PlanConfig.forId("combo_annual_platinum");
        new PlanConfig(existing.planId(), existing.planName(), existing.accountType(),
                existing.snAccess(), existing.snsAccess(), existing.adEntitlement(),
                existing.loginLimit(), existing.concurrentPlaybackLimit(),
                existing.liveTvAccess(), 2160, existing.vodTier());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsFreeThresholdForPaidAccount() {
        PlanConfig existing = PlanConfig.forId("sn_annual_premium");
        new PlanConfig(existing.planId(), existing.planName(), existing.accountType(),
                existing.snAccess(), existing.snsAccess(), existing.adEntitlement(),
                existing.loginLimit(), existing.concurrentPlaybackLimit(),
                existing.liveTvAccess(), existing.maxWebQuality(), VodTier.FREE);
    }
}
