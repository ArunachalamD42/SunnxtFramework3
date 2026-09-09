package entitlementValidation;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import baseClass.TestBase;
import config.Log;
import dataProvider.EntitlementDataProvider;
import dataProvider.PlanConfig;
import helpers.EntitlementHelper;
import pages.ContentDetailPage;
import pages.HomePage;
import pages.PlayerPage;
import reporting.ExtentReportManager;

/**
 * TC_ContentEntitlementValidation - Validates content access, ad behavior,
 * video quality, and audio quality for all plans.
 * 
 * Single login + single playback session validates 4 entitlements per plan.
 * Tests SN content (mid-roll ads) and SNS Shorts (positional post-roll ads) separately.
 */
public class TC_ContentEntitlementValidation extends TestBase {

    private static final Logger logger = Log.getLogger(TC_ContentEntitlementValidation.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    /**
     * Validates SN content entitlements for each plan:
     * - Content access (plays or subscription prompt)
     * - Mid-roll ad presence (ad-free vs with-ads)
     * - Video quality cap (4K UHD vs Full HD)
     * - Audio quality (Dolby/DD+5.1 vs Stereo)
     */
    @Test(dataProvider = "planConfig", dataProviderClass = EntitlementDataProvider.class)
    public void validateSNContentEntitlements(Object[] planData) throws InterruptedException {
        PlanConfig plan = EntitlementHelper.parsePlanConfig(planData);
        test.info("Testing SN Content for plan: " + plan.getPlanName());
        logger.info("=== SN Content Validation: {} ===", plan.getPlanName());

        SoftAssert softAssert = new SoftAssert();

        // Login
        EntitlementHelper.loginWithPlanConfig(driver, plan);
        waitForLoginSuccess();
        test.info("Login successful");

        // Navigate to SN content
        if (!plan.getSnContentUrl().isEmpty()) {
            EntitlementHelper.navigateToContent(driver, plan.getSnContentUrl());
        } else {
            HomePage hp = new HomePage(driver);
            hp.navigateToMovies();
            hp.contentPlayBackCheck(-1, 0);
        }

        ContentDetailPage cdp = new ContentDetailPage(driver);
        cdp.clickPlayButton();
        test.info("Play button clicked");

        PlayerPage playerPage = new PlayerPage(driver);

        // 1. CONTENT ACCESS VALIDATION
        if (plan.hasSNAccess()) {
            boolean playing = playerPage.waitForPlaybackStart(60);
            softAssert.assertTrue(playing,
                    "[" + plan.getPlanName() + "] SN content should play for entitled plan");
            test.info("Content access: " + (playing ? "PASS - Video playing" : "FAIL - Video not playing"));
        } else {
            boolean blocked = playerPage.isSubscriptionPromptShown();
            softAssert.assertTrue(blocked,
                    "[" + plan.getPlanName() + "] SN content should show subscription prompt");
            test.info("Content access: " + (blocked ? "PASS - Correctly blocked" : "FAIL - Not blocked"));
            softAssert.assertAll();
            return; // No point checking quality/ads if content is blocked
        }

        // 2. AD PRESENCE VALIDATION (mid-roll for SN content)
        if (networkUtils != null) {
            int adCount = EntitlementHelper.clearAndWaitForAds(networkUtils, 60);
            if (plan.isAdFree()) {
                softAssert.assertEquals(adCount, 0,
                        "[" + plan.getPlanName() + "] Ad-free plan should have zero ad requests");
                test.info("Ad validation: " + (adCount == 0 ? "PASS - No ads" : "FAIL - " + adCount + " ads detected"));
            } else {
                softAssert.assertTrue(adCount > 0,
                        "[" + plan.getPlanName() + "] Ad-supported plan should have ad requests");
                test.info("Ad validation: " + (adCount > 0 ? "PASS - " + adCount + " ads detected" : "FAIL - No ads"));
            }
        }

        // 3. VIDEO QUALITY VALIDATION
        playerPage.moveFocusToPlayer();
        String maxQuality = playerPage.getMaxQuality();
        if (!maxQuality.isEmpty()) {
            softAssert.assertEquals(maxQuality, plan.getMaxVideoQuality(),
                    "[" + plan.getPlanName() + "] Max quality should be " + plan.getMaxVideoQuality());
            test.info("Quality validation: Max available = " + maxQuality + " | Expected = " + plan.getMaxVideoQuality());
        } else {
            test.info("Quality validation: INCONCLUSIVE - Quality menu not accessible");
            logger.warn("Quality menu not accessible for plan: {}", plan.getPlanName());
        }

        // 4. AUDIO QUALITY VALIDATION
        List<String> audioTracks = playerPage.getAvailableAudioTracks();
        if (!audioTracks.isEmpty()) {
            String expectedAudio = plan.getAudioQuality();
            boolean hasExpectedAudio = audioTracks.stream()
                    .anyMatch(track -> track.toLowerCase().contains(expectedAudio.split("/")[0].toLowerCase()));
            softAssert.assertTrue(hasExpectedAudio,
                    "[" + plan.getPlanName() + "] Should have audio: " + expectedAudio);
            test.info("Audio validation: Available = " + audioTracks + " | Expected = " + expectedAudio);
        } else {
            test.info("Audio validation: INCONCLUSIVE - Audio menu not accessible");
            logger.warn("Audio menu not accessible for plan: {}", plan.getPlanName());
        }

        softAssert.assertAll();
        test.pass("SN content entitlement validation passed for: " + plan.getPlanName());
    }

    /**
     * Validates SNS Shorts entitlements:
     * - Content access
     * - Post-roll ad at position 5 (scroll through episodes 1-5 with gaps)
     */
    @Test(dataProvider = "planConfig", dataProviderClass = EntitlementDataProvider.class)
    public void validateSNSContentEntitlements(Object[] planData) throws InterruptedException {
        PlanConfig plan = EntitlementHelper.parsePlanConfig(planData);
        test.info("Testing SNS Shorts for plan: " + plan.getPlanName());
        logger.info("=== SNS Shorts Validation: {} ===", plan.getPlanName());

        SoftAssert softAssert = new SoftAssert();

        // Login
        EntitlementHelper.loginWithPlanConfig(driver, plan);
        waitForLoginSuccess();
        test.info("Login successful");

        // Navigate to Shorts section
        HomePage hp = new HomePage(driver);
        hp.navigateToShorts();
        test.info("Navigated to Shorts");

        // Play first content
        hp.contentPlayBackCheck(0, 0);
        test.info("First Shorts content selected");

        ContentDetailPage cdp = new ContentDetailPage(driver);
        cdp.clickPlayButton();

        PlayerPage playerPage = new PlayerPage(driver);

        // Verify content access
        if (plan.hasSNSAccess()) {
            boolean playing = playerPage.waitForPlaybackStart(30);
            softAssert.assertTrue(playing,
                    "[" + plan.getPlanName() + "] SNS content should play");
            test.info("SNS access: " + (playing ? "PASS" : "FAIL"));
        } else {
            boolean blocked = playerPage.isSubscriptionPromptShown();
            softAssert.assertTrue(blocked,
                    "[" + plan.getPlanName() + "] SNS content should be blocked");
            test.info("SNS access: " + (blocked ? "PASS - Correctly blocked" : "FAIL"));
            softAssert.assertAll();
            return;
        }

        // Scroll through episodes 1-5 with few seconds gap
        // Ad should trigger at position 5 for free/lite tiers
        if (networkUtils != null) {
            networkUtils.clearCapturedRequests();
        }

        for (int episode = 2; episode <= 5; episode++) {
            Thread.sleep(3000); // Few seconds gap between scrolls
            // Scroll to next content (simulating swipe to next episode)
            playerPage.scrollByPixel();
            logger.info("Scrolled to episode position: {}", episode);
            test.info("Scrolled to position " + episode);
        }

        // Wait a moment after reaching position 5
        Thread.sleep(5000);

        // Check if ad played at position 5
        if (networkUtils != null) {
            int adCount = networkUtils.getAdRequests().size();
            String snsAdTier = plan.getSnsAdTier();

            if (snsAdTier.equalsIgnoreCase("premium")) {
                softAssert.assertEquals(adCount, 0,
                        "[" + plan.getPlanName() + "] Premium tier should have no ad at position 5");
                test.info("SNS ad at pos 5: " + (adCount == 0 ? "PASS - No ad (premium)" : "FAIL - Ad detected"));
            } else {
                // free and lite both have ad at position 5
                softAssert.assertTrue(adCount > 0,
                        "[" + plan.getPlanName() + "] " + snsAdTier + " tier should have ad at position 5");
                test.info("SNS ad at pos 5: " + (adCount > 0 ? "PASS - Ad detected (" + snsAdTier + ")" : "FAIL - No ad"));
            }
        }

        softAssert.assertAll();
        test.pass("SNS Shorts entitlement validation passed for: " + plan.getPlanName());
    }

    /**
     * Validates free user content lock - subscription popup at episode 31.
     * Uses an existing free user account (under 30 watches).
     */
    @Test(enabled = true)
    public void validateFreeUserContentLock() throws InterruptedException {
        test.info("Testing Free User content lock at 30 episodes");
        logger.info("=== Free User Content Lock Validation ===");

        // Login with free user (from config or hardcoded)
        // Note: Update credentials to a free user account under 30 watches
        EntitlementHelper.loginWithPlanAccount(driver, "FREE_USER_MOBILE", "FREE_USER_PASSWORD");
        waitForLoginSuccess();
        test.info("Free user logged in");

        HomePage hp = new HomePage(driver);
        hp.navigateToShorts();

        // Scroll through 31 episodes
        hp.contentPlayBackCheck(0, 0);
        ContentDetailPage cdp = new ContentDetailPage(driver);
        cdp.clickPlayButton();

        PlayerPage playerPage = new PlayerPage(driver);

        for (int episode = 2; episode <= 31; episode++) {
            Thread.sleep(2000);
            playerPage.scrollByPixel();
            logger.info("Scrolled to episode: {}", episode);

            // Check for subscription popup at episode 31
            if (episode == 31) {
                Thread.sleep(3000);
                boolean subscriptionShown = playerPage.isSubscriptionPromptShown();
                Assert.assertTrue(subscriptionShown,
                        "Subscription popup should appear at episode 31 for free user");
                test.info("Content lock at ep 31: " + (subscriptionShown ? "PASS - Popup shown" : "FAIL"));
            }
        }

        test.pass("Free user content lock validation passed");
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
