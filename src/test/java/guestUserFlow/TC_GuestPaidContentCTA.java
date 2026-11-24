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
import pagesSunnxt.SubscriptionPage;
import reporting.ExtentReportManager;

public class TC_GuestPaidContentCTA extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());

        setupDriver();
        driver.manage().deleteAllCookies();
        launchSunnxt();
    }

    @Test(enabled=true)
    public void guestMovieSubscribeToWatchTest() throws InterruptedException {
        test.info("Guest Movie Subscribe to Watch test starts...");

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
        hp.selectFirstContent();
        test.info("Paid Movie content selected");

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String subscribeText = cdp.getSubscribeToWatchText();
        Assert.assertEquals(subscribeText, "Subscribe to Watch",
                "Paid Movie should display Subscribe to Watch CTA");
        test.info("Movie Subscribe to Watch CTA verified");

        cdp.clickSubscribeToWatchButton();

        SubscriptionPage subscriptionPage = new SubscriptionPage(driver);
        Assert.assertTrue(subscriptionPage.isSubscriptionPageDisplayed(),
                "Guest user was not redirected to the Subscription page from Movie content");

        test.pass("Guest Movie Subscribe to Watch flow verified successfully");
    }

    @Test(enabled=false)
    public void guestLiveTvSubscribeToWatchTest() throws InterruptedException {
        test.info("Guest Live TV Subscribe to Watch test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.waitForHomePageLoaded();
        hp.navigateToLiveTv();
        hp.waitForSectionLoaded("Live TV");
        hp.selectLiveChannelWithoutFreeTag();
        test.info("Paid Live TV channel selected");

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String subscribeText = cdp.getSubscribeToWatchText();
        Assert.assertEquals(subscribeText, "Subscribe to Watch",
                "Paid Live TV should display Subscribe to Watch CTA");
        test.info("Live TV Subscribe to Watch CTA verified");

        cdp.clickSubscribeToWatchButton();

        SubscriptionPage subscriptionPage = new SubscriptionPage(driver);
        Assert.assertTrue(subscriptionPage.isSubscriptionPageDisplayed(),
                "Guest user was not redirected to the Subscription page from Live TV content");

        test.pass("Guest Live TV Subscribe to Watch flow verified successfully");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
