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

public class TC_GuestRestrictedAction extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());

        setupDriver();
        driver.manage().deleteAllCookies();
        launchSunnxt();
    }

    @Test
    public void guestWatchlistRedirectionTest() throws InterruptedException {
        test.info("Guest Watchlist redirection test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.waitForHomePageLoaded();
        hp.navigateToMovies();
        hp.waitForSectionLoaded("Movies");
        hp.watchlistFirstContentFromCarousel();;
        test.info("Clicked the watchlist of the first carousel");


        LoginPage lp = new LoginPage(driver);
        Assert.assertTrue(lp.isLoginPageDisplayed(),
                "Guest user was not redirected to Login after clicking Watchlist");

        test.pass("Guest user was redirected to Login after clicking Watchlist");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
