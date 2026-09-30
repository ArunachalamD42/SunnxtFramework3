package entitlementValidation;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import baseClass.TestBase;
import config.Log;
import dataProvider.EntitlementDataProvider;
import dataProvider.PlanConfig;
import helpers.EntitlementHelper;
import pages.ContentDetailPageLiveTV;
import pages.LiveTVPage;
import reporting.ExtentReportManager;

/**
 * TC_LiveTVAccessValidation - Validates Live TV access per subscription plan.
 * Plans with liveTvAccess=yes should play live channels.
 * Plans with liveTvAccess=no (SNS-only) should be blocked or Live TV tab hidden.
 */
public class TC_LiveTVAccessValidation extends TestBase {

    private static final Logger logger = Log.getLogger(TC_LiveTVAccessValidation.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(dataProvider = "planConfig", dataProviderClass = EntitlementDataProvider.class)
    public void validateLiveTVAccess(Object[] planData) throws InterruptedException {
        PlanConfig plan = EntitlementHelper.parsePlanConfig(planData);
        test.info("Testing Live TV access for: " + plan.getPlanName());
        logger.info("=== Live TV Validation: {} ===", plan.getPlanName());

        SoftAssert softAssert = new SoftAssert();

        // Login
        EntitlementHelper.loginWithPlanConfig(driver, plan);
        waitForLoginSuccess();
        test.info("Login successful");

        LiveTVPage liveTVPage = new LiveTVPage(driver);
        ContentDetailPageLiveTV liveTVDetailPage = new ContentDetailPageLiveTV(driver);

        if (plan.isLiveTvAccess()) {
            // Plan SHOULD have Live TV access
            liveTVPage.navigateToLiveTV();
            Thread.sleep(3000);
            liveTVPage.selectFirstChannel();
            Thread.sleep(3000);

            // Channel selection lands on the Live TV detail page — start playback
            liveTVDetailPage.clickPlayButton();
            Thread.sleep(5000);

            boolean isPlaying = liveTVPage.isLiveChannelPlaying();
            softAssert.assertTrue(isPlaying,
                    "[" + plan.getPlanName() + "] Live TV should play for entitled plan");
            test.info("Live TV: " + (isPlaying ? "PASS - Channel playing" : "FAIL - Not playing"));

        } else {
            // Plan should NOT have Live TV access
            boolean liveTvVisible = liveTVPage.isLiveTVSectionVisible();

            if (!liveTvVisible) {
                // Live TV tab not visible — valid access denial
                softAssert.assertFalse(liveTvVisible,
                        "[" + plan.getPlanName() + "] Live TV tab should not be visible");
                test.info("Live TV: PASS - Tab not visible (correctly hidden)");
            } else {
                // Tab visible but should be blocked when navigated / channel opened
                liveTVPage.navigateToLiveTV();
                Thread.sleep(3000);
                liveTVPage.selectFirstChannel();
                Thread.sleep(3000);

                boolean accessDenied = liveTVPage.isAccessDeniedShown()
                        || liveTVDetailPage.isSubscriptionPromptShown();
                softAssert.assertTrue(accessDenied,
                        "[" + plan.getPlanName() + "] Live TV should show subscription prompt");
                test.info("Live TV: " + (accessDenied ? "PASS - Subscription prompt shown" : "FAIL - No restriction"));
            }
        }

        softAssert.assertAll();
        test.pass("Live TV validation passed for: " + plan.getPlanName());
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
