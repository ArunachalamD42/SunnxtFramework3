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
import reporting.ExtentReportManager;

public class TC_GuestContentBrowsing extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());

        setupDriver();
        driver.manage().deleteAllCookies();
        launchSunnxt();
    }

    @Test
    public void guestUserContentBrowsingTest() {
        test.info("Guest user content browsing test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.waitForHomePageLoaded();

        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "Guest user should land on Home page, not the Login page");

        String homeUrl = driver.getCurrentUrl();

        hp.navigateToTvShows();
        hp.waitForSectionLoaded("TV Shows");
        hp.selectContentWithoutScrolling(0, 0);

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String tvShowName = cdp.getContentName();
        Assert.assertFalse(tvShowName.isBlank(), "Guest user could not open a TV Shows detail page");
        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "Guest user should browse TV Shows without login");
        test.info("Guest user opened TV Shows content: " + tvShowName);

        driver.get(homeUrl);
        hp.waitForHomePageLoaded();

        hp.navigateToMovies();
        hp.waitForSectionLoaded("Movies");
        hp.selectContentWithoutScrolling(0, 0);

        cdp = new ContentDetailPage(driver);
        String movieName = cdp.getContentName();
        Assert.assertFalse(movieName.isBlank(), "Guest user could not open a Movies detail page");
        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "Guest user should browse Movies without login");
        test.info("Guest user opened Movies content: " + movieName);

        driver.get(homeUrl);
        hp.waitForHomePageLoaded();

        hp.navigateToFree();
        hp.waitForSectionLoaded("Free");
        hp.selectContentWithoutScrolling(0, 0);

        cdp = new ContentDetailPage(driver);
        String freeContentName = cdp.getContentName();
        Assert.assertFalse(freeContentName.isBlank(), "Guest user could not open a Free content detail page");
        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "Guest user should browse Free content without login");
        test.info("Guest user opened Free content: " + freeContentName);

        test.pass("Guest user browsed TV Shows, Movies and Free content detail pages successfully");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
