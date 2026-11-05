package adsPlayback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Full-flow account data only; no browser or real credentials. */
public class AdsPlaybackDataTest {

    @Test
    public void missingCredentialsSkipTheTestRatherThanItsConfiguration() throws Exception {
        TC_AdsPlayback flow = new TC_AdsPlayback();
        String account = "UNIT_" + java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        var method = TC_AdsPlayback.class.getMethod("completeAdsPlaybackTest", String.class, String.class);
        flow.setupTest(method, new Object[] {account, "Unit fixture"});
        Assert.assertNull(flow.driver, "Report setup must not launch a browser");
        org.testng.SkipException skip = Assert.expectThrows(org.testng.SkipException.class,
                () -> flow.completeAdsPlaybackTest(account, "Unit fixture"));
        Assert.assertEquals(skip.getStackTrace()[0].getMethodName(), "completeAdsPlaybackTest");
        Assert.assertNull(flow.driver, "Missing credentials must skip before browser creation");
    }

    @Test
    public void enabledAccountsHaveDistinctAliasesAndPlans() {
        Object[][] rows = new TC_AdsPlayback().accounts();
        Assert.assertTrue(rows.length > 0 && rows.length <= 6, "Enable between one and six account rows");
        Set<Object> aliases = new HashSet<>();
        Set<Object> plans = new HashSet<>();
        for (Object[] row : rows) {
            Assert.assertEquals(row.length, 2);
            Assert.assertTrue(aliases.add(row[0]), "Duplicate account alias");
            Assert.assertTrue(plans.add(row[1]), "Duplicate plan");
        }
    }

    @Test
    public void preservesTheExistingEclipseAccountMappings() {
        Map<String, String> actual = new HashMap<>();
        for (Object[] row : new TC_AdsPlayback().accounts()) {
            actual.put((String) row[0], (String) row[1]);
        }
        Map<String, String> expected = Map.of("COMBO_ANNUAL", "Combo Annual Platinum",
                "SAMPLE", "SN Annual Premium", "COMBO_QUARTERLY", "Combo Quarterly Platinum",
                "SN_LITE", "SN Quarterly Lite", "SNS_PREMIUM", "SNS Annual Premium",
                "SNS_LITE", "SNS Quarterly Lite");
        for (Map.Entry<String, String> entry : actual.entrySet()) {
            Assert.assertEquals(entry.getValue(), expected.get(entry.getKey()), "Account alias must match its plan");
        }
    }
}
