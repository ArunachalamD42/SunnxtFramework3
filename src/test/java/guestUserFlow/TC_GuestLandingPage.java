package guestUserFlow;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import reporting.ExtentReportManager;

public class TC_GuestLandingPage extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());

        setupDriver();
        driver.manage().deleteAllCookies();
        launchSunnxt();
    }

    @Test
    public void guestUserLandingPageTest() {
        test.info("Guest user landing page test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        test.info("SN language selected");

        cls.proceedToSNSScreen();
        test.info("Navigated to SNS language screen");

        cls.selectSNSLanguages("tamil");
        test.info("SNS language selected");

        cls.clickProceed();
        test.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.waitForHomePageLoaded();

        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "Guest user should land on Home page, not the Login page");

        test.pass("Guest user landed on Home page after selecting content language");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
