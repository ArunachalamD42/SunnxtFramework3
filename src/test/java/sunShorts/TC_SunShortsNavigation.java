package sunShorts;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.apache.logging.log4j.Logger;

import baseClass.TestBase;
import config.Log;
import config.ConfigReader;
import pagesSunShorts.HomePageSunShorts;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

/** TC_SunShortsNavigation - Checks login and navigation to the Sun Shorts home page. */
public class TC_SunShortsNavigation extends TestBase {

    private static final Logger logger = Log.getLogger(TC_SunShortsNavigation.class);

    // ==================== SETUP ====================

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        logger.info("Starting test: {}", method.getName());
        setupDriver();
        launchSunnxt();
    }

    // ==================== SUN SHORTS NAVIGATION ====================

    @Test(groups = "sun-shorts")
    public void sunShortsNavigationTest() {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }
        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        test.pass("Sun Shorts navigation completed and carousel headings loaded");

        logger.info("Sun Shorts navigation completed and carousel headings loaded");
    }

    // ==================== CLEANUP ====================

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (test != null && !result.isSuccess()) {
                test.fail("Sun Shorts navigation failed; see TestNG results");
                logger.error("Sun Shorts navigation failed; see TestNG results");
            }
        } finally {
            quitDriver();
        }
    }
}
