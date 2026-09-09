package entitlementValidation;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import config.Log;
import dataProvider.EntitlementDataProvider;
import dataProvider.PlanConfig;
import helpers.EntitlementHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import reporting.ExtentReportManager;

/**
 * TC_DeviceLimitValidation - Validates concurrent session/device limits per plan.
 * Launches multiple independent browser instances to test device limit enforcement.
 */
public class TC_DeviceLimitValidation extends TestBase {

    private static final Logger logger = Log.getLogger(TC_DeviceLimitValidation.class);
    private List<WebDriver> activeSessions = new ArrayList<>();

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        // Do NOT call setupDriver() here — this class manages multiple drivers manually
    }

    /**
     * Validates that exceeding device limit triggers restriction.
     * Launches deviceLimit+1 sessions, expects the last one to be blocked.
     */
    @Test(dataProvider = "planConfig", dataProviderClass = EntitlementDataProvider.class)
    public void validateDeviceLimitEnforced(Object[] planData) throws InterruptedException {
        PlanConfig plan = EntitlementHelper.parsePlanConfig(planData);
        int deviceLimit = plan.getDeviceLimit();
        test.info("Testing device limit for: " + plan.getPlanName() + " (limit: " + deviceLimit + ")");
        logger.info("=== Device Limit Validation: {} | Limit: {} ===", plan.getPlanName(), deviceLimit);

        // Launch deviceLimit + 1 sessions
        int totalSessions = deviceLimit + 1;

        for (int i = 1; i <= totalSessions; i++) {
            WebDriver sessionDriver = createNewSession();
            activeSessions.add(sessionDriver);

            try {
                // Navigate and login
                sessionDriver.get(ConfigReader.getBaseUrl());
                EntitlementHelper.loginWithPlanAccount(sessionDriver, plan.getUsername(), plan.getPassword());
                logger.info("Session {} logged in", i);
                test.info("Session " + i + " logged in");
                Thread.sleep(3000);
            } catch (Exception e) {
                logger.error("Error in session {}: {}", i, e.getMessage());
            }
        }

        // Check last session for device limit restriction
        WebDriver lastSession = activeSessions.get(activeSessions.size() - 1);
        Thread.sleep(5000);

        boolean deviceLimitReached = false;
        try {
            deviceLimitReached = !lastSession.findElements(
                    By.xpath("//h2[text()='Device Limit Reached'] | //h3[text()='Device Limit Reached']")
            ).isEmpty();
        } catch (Exception e) {
            logger.warn("Could not check device limit popup on last session");
        }

        Assert.assertTrue(deviceLimitReached,
                "[" + plan.getPlanName() + "] Session " + totalSessions + " should trigger device limit (limit=" + deviceLimit + ")");
        test.pass("Device limit enforced correctly for: " + plan.getPlanName());
    }

    /**
     * Validates that exactly deviceLimit concurrent sessions work without restriction.
     */
    @Test(dataProvider = "planConfig", dataProviderClass = EntitlementDataProvider.class)
    public void validateConcurrentSessionsAllowed(Object[] planData) throws InterruptedException {
        PlanConfig plan = EntitlementHelper.parsePlanConfig(planData);
        int deviceLimit = plan.getDeviceLimit();
        test.info("Testing concurrent sessions for: " + plan.getPlanName() + " (allowed: " + deviceLimit + ")");
        logger.info("=== Concurrent Sessions: {} | Allowed: {} ===", plan.getPlanName(), deviceLimit);

        // Launch exactly deviceLimit sessions
        for (int i = 1; i <= deviceLimit; i++) {
            WebDriver sessionDriver = createNewSession();
            activeSessions.add(sessionDriver);

            try {
                sessionDriver.get(ConfigReader.getBaseUrl());
                EntitlementHelper.loginWithPlanAccount(sessionDriver, plan.getUsername(), plan.getPassword());
                logger.info("Session {} logged in", i);
                Thread.sleep(3000);
            } catch (Exception e) {
                logger.error("Error in session {}: {}", i, e.getMessage());
            }
        }

        // Verify all sessions are active (no device limit popup)
        boolean allSessionsActive = true;
        for (int i = 0; i < activeSessions.size(); i++) {
            WebDriver session = activeSessions.get(i);
            try {
                boolean hasDeviceLimit = !session.findElements(
                        By.xpath("//h2[text()='Device Limit Reached'] | //h3[text()='Device Limit Reached']")
                ).isEmpty();
                if (hasDeviceLimit) {
                    allSessionsActive = false;
                    logger.warn("Session {} hit device limit unexpectedly", i + 1);
                    test.info("Session " + (i + 1) + " - FAIL: Device limit shown");
                } else {
                    test.info("Session " + (i + 1) + " - PASS: Active");
                }
            } catch (Exception e) {
                logger.warn("Could not verify session {}", i + 1);
            }
        }

        Assert.assertTrue(allSessionsActive,
                "[" + plan.getPlanName() + "] All " + deviceLimit + " sessions should be active without device limit");
        test.pass("Concurrent sessions validation passed for: " + plan.getPlanName());
    }

    /**
     * Creates a new independent ChromeDriver session.
     */
    private WebDriver createNewSession() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        WebDriverManager.chromedriver().setup();
        WebDriver newDriver = new ChromeDriver(options);
        newDriver.manage().window().maximize();
        newDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return newDriver;
    }

    @AfterMethod
    public void tearDown() {
        // Quit all sessions
        for (WebDriver session : activeSessions) {
            try {
                session.quit();
            } catch (Exception ignored) {
            }
        }
        activeSessions.clear();
        logger.info("All browser sessions closed");
    }
}
