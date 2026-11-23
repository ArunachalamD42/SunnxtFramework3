package baseClass;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import config.ConfigReader;
import config.Log;
import helpers.NetworkUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import reporting.ExtentReportManager;

/**
 * TestBase - Base class for all test classes.
 * Provides browser setup, teardown, and common utilities.
 */
public class TestBase {

    public WebDriver driver;
    protected static ExtentReports extent;
    protected ExtentTest test;
    // Network capture is opt-in; regular tests leave it disabled.
    protected NetworkUtils networkUtils = null;

    public static final Logger logger = Log.getLogger(TestBase.class);

    @BeforeSuite
    public void setupReport() {
        ConfigReader.loadConfig();
        extent = ExtentReportManager.getReportInstance();
        logger.info("Test Suite started. Environment: {}", ConfigReader.get("env.name"));
    }

    @AfterSuite
    public void teardownReport() {
        extent.flush();
        logger.info("Test Suite completed. Report generated.");
    }

    /**
     * Sets up WebDriver based on config. Supports chrome and firefox.
     */
    public void setupDriver() {
        setupDriver(false);
    }

    public void setupDriver(boolean captureNetwork) {
        networkUtils = null;
        String browser = ConfigReader.getBrowser();
        boolean headless = ConfigReader.getBoolean("headless", false);
        if (captureNetwork && !browser.equalsIgnoreCase("chrome")) {
            throw new IllegalArgumentException("Browser network capture for ads requires Chrome");
        }

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            if (captureNetwork && Boolean.getBoolean("ads.seek.debug")) {
                prefs.put("devtools.preferences", Map.of("panel-selectedTab", "\"network\""));
                options.addArguments("--auto-open-devtools-for-tabs");
            }
            options.setExperimentalOption("prefs", prefs);
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            if (captureNetwork) {
                LoggingPreferences logs = new LoggingPreferences();
                logs.enable(LogType.PERFORMANCE, Level.ALL);
                options.setCapability("goog:loggingPrefs", logs);
                options.setExperimentalOption("perfLoggingPrefs", Map.of("enableNetwork", true, "enablePage", false));
            }

            if (headless) {
                options.addArguments("--headless=new");
            }

            if (System.getProperty("webdriver.chrome.driver", "").isBlank()) {
                WebDriverManager.chromedriver().setup();
            }
            driver = new ChromeDriver(options);

        } else if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            options.addPreference("dom.push.enabled", false);
            options.addPreference("permissions.default.desktop-notification", 2);

            if (headless) {
                options.addArguments("--headless");
            }

            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver(options);

        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        if (captureNetwork) {
            networkUtils = new NetworkUtils(driver);
            networkUtils.startCapture();
        }

        driver.manage().window().maximize();
        int implicitWait = ConfigReader.getInt("implicit.wait", 10);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        logger.info("WebDriver initiated. Browser: {}", browser);
    }

    /**
     * Launches SunNXT using the configured base URL.
     */
    public void launchSunnxt() {
        String baseUrl = ConfigReader.getBaseUrl();
        logger.info("Launching SunNXT at: {}", baseUrl);
        driver.get(baseUrl);
    }

    /**
     * Waits for login success by detecting the profile icon.
     */
    public void waitForLoginSuccess() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='relative group inline-block pointer-events-auto']")));
        logger.info("User login successful - profile icon visible");
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            logger.info("All browser sessions closed.");
        }
    }

    public void closeSession() {
        if (driver != null) {
            driver.close();
            logger.info("Current session closed.");
        }
    }

    public void implicitWait(int seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    public String randomEmail() {
        Random random = new Random();
        String email = "sunnxt_test" + random.nextInt(100000) + "@hotfail.com";
        logger.debug("Generated random email: {}", email);
        return email;
    }

    /**
     * Checks for playback or subscription screen, then quits driver.
     */
    public void exitIfPlaybackOrSubscription() {
        try {
            if (driver == null) return;

            boolean subscriptionShown = !driver.findElements(
                    By.xpath("//h2[text()='Subscriptions']")).isEmpty();

            if (subscriptionShown) {
                logger.info("Subscription screen detected. Closing browser.");
                quitDriver();
                return;
            }

            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean videoPlaying = (Boolean) js.executeScript(
                    "var v = document.querySelector('video');" +
                    "return !!v && !v.paused && v.currentTime > 0 && v.offsetParent !== null;");

            if (Boolean.TRUE.equals(videoPlaying)) {
                logger.info("Video playing. Waiting 8s before closing...");
                Thread.sleep(8000);
                quitDriver();
                return;
            }

            logger.info("No subscription/video detected. Closing browser.");
            quitDriver();

        } catch (Exception e) {
            logger.error("Error during teardown. Forcing quit.", e);
            quitDriver();
        }
    }
}
