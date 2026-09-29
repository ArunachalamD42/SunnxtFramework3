package helpers;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import config.ConfigReader;
import config.Log;
import dataProvider.PlanConfig;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import pages.PlayerEntitlementPage;

/**
 * EntitlementHelper - Shared validation logic for entitlement tests.
 */
public class EntitlementHelper {

    private static final Logger logger = Log.getLogger(EntitlementHelper.class);

    /**
     * Performs full login with plan-specific credentials.
     * Handles language selection, navigation to login, 2-step login, and device limit.
     */
    public static void loginWithPlanAccount(WebDriver driver, String username, String password) {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(username, password);
        logger.info("Logged in with plan account: {}", username);
    }

    /**
     * Performs login using PlanConfig object.
     */
    public static void loginWithPlanConfig(WebDriver driver, PlanConfig plan) {
        loginWithPlanAccount(driver, plan.getUsername(), plan.getPassword());
    }

    /**
     * Validates that video playback started successfully within timeout.
     */
    public static boolean validatePlaybackSuccess(PlayerEntitlementPage playerPage, int timeoutSeconds) {
        boolean playing = playerPage.waitForPlaybackStart(timeoutSeconds);
        if (playing) {
            logger.info("Playback validation PASSED");
        } else {
            logger.warn("Playback validation FAILED");
        }
        return playing;
    }

    /**
     * Navigates directly to a content URL (base URL + content path).
     */
    public static void navigateToContent(WebDriver driver, String contentPath) {
        String baseUrl = ConfigReader.getBaseUrl();
        String fullUrl = baseUrl + contentPath;
        driver.get(fullUrl);
        logger.info("Navigated to content: {}", fullUrl);
    }

    /**
     * Clears network captures, waits for specified time, then returns ad request count.
     */
    public static int clearAndWaitForAds(NetworkUtils networkUtils, int waitSeconds) {
        networkUtils.clearCapturedRequests();
        logger.info("Cleared network captures. Waiting {}s for ad requests...", waitSeconds);
        try {
            Thread.sleep(waitSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        int adCount = networkUtils.getAdRequests().size();
        logger.info("Ad requests captured: {}", adCount);
        return adCount;
    }

    /**
     * Parses an Excel row into a PlanConfig object.
     */
    public static PlanConfig parsePlanConfig(Object[] excelRow) {
        return new PlanConfig(excelRow);
    }
}
