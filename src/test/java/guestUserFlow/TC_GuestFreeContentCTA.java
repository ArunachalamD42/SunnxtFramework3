package guestUserFlow;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import reporting.ExtentReportManager;

public class TC_GuestFreeContentCTA extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());

        setupDriver();
        driver.manage().deleteAllCookies();
        launchSunnxt();
    }

    @Test
    public void guestFreeContentCtaTest() {
        test.info("Guest Free content CTA test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.waitForHomePageLoaded();

        hp.navigateToFree();
        hp.waitForSectionLoaded("Free");
        test.info("Navigated to Free content page");

        hp.selectContentWithoutScrolling(0, 0);

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String contentName = cdp.getContentName();
        Assert.assertFalse(contentName.isBlank(), "Free content detail page did not open");
        test.info("Free content selected: " + contentName);

        String ctaText = cdp.getWatchNowText();
        Assert.assertFalse(ctaText.isBlank(), "Watch Now CTA is not displayed");

        boolean movieCta = ctaText.equalsIgnoreCase("Watch Now");
        boolean tvShowCta = ctaText.toLowerCase().contains("watch now") && ctaText.matches(".*\\d+.*");

        Assert.assertTrue(movieCta || tvShowCta,
                "Expected Watch Now for Movies or Watch Now with an episode number for TV Shows. Actual CTA: "
                        + ctaText);
        test.info("Watch Now CTA verified: " + ctaText);

        cdp.clickPlayButton();

        LoginPage lp = new LoginPage(driver);
        Assert.assertTrue(lp.isLoginPageDisplayed(),
                "Login popup did not open after clicking Watch Now for Free content");

        test.pass("Guest user Free content CTA and login popup verified successfully");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
