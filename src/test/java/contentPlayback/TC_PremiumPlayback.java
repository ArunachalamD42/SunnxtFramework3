package contentPlayback;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import config.Log;
import pagesSunShorts.HomePageSunShorts;
import pagesSunShorts.PlayerSunShorts;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.PlayerControlsPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

public class TC_PremiumPlayback extends TestBase {

    private static final Logger logger = Log.getLogger(TC_PremiumPlayback.class);

    @DataProvider(name = "premiumAccounts")
    public Object[][] premiumAccounts() {
        return new Object[][] {
                {"Combo Annual Platinum", "test.annualPlatinum", "test.platinumPassword"},
                {"SN Annual Premium", "test.username", "test.password"}
        };
    }

    @BeforeMethod
    public void setup(Object[] data) {
        test = ExtentReportManager.createTest("Premium playback - " + data[0]);
    }

    @Test(enabled = true, dataProvider = "premiumAccounts")
    public void premiumPlaybackTest(String plan, String usernameKey, String passwordKey) throws InterruptedException {
        String username = ConfigReader.getRuntime(usernameKey);
        String password = ConfigReader.getRuntime(passwordKey);
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            test.info(plan + " - account credentials are not configured");
            throw new SkipException(plan + " - account credentials are not configured");
        }

        logger.info("Starting playback checks for {}", plan);
        setupDriver();
        launchSunnxt();

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();
        test.info("Navigated to Login page");

        LoginPage lp = new LoginPage(driver);
        lp.enterLoginId(username);
        lp.clickContinueButton();
        lp.waitForPasswordScreen();
        lp.enterPassword(password);
        lp.clickLoginButton();
        lp.handleDeviceLimit();

        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        waitForLoginSuccess();
        test.info(plan + " - logged in and primary profile selected");
        String homeUrl = driver.getCurrentUrl();

        hp.navigateToMovies();
        hp.contentPlayBackCheck(-1, -1);
        ContentDetailPage cdp = new ContentDetailPage(driver);
        test.info("Movies content selected: " + cdp.getContentName());
        cdp.clickPlayButton();
        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.moveFocusToPlayer();
        Assert.assertTrue(player.isVideoElementDisplayed(), plan + " - Movies player not displayed");
        Assert.assertTrue(player.isVideoPlaying(), plan + " - Movies content is not playing");
        test.pass(plan + " - Movies playback passed");

        driver.get(homeUrl);
        hp.navigateToLiveTv();
        hp.selectRandomLiveChannel();
        test.info("Random Live TV channel selected");
        cdp.clickPlayButton();
        player.moveFocusToPlayer();
        Assert.assertTrue(player.isVideoElementDisplayed(), plan + " - Live TV player not displayed");
        Assert.assertTrue(player.isVideoPlaying(), plan + " - Live TV content is not playing");
        test.pass(plan + " - Live TV playback passed");

        driver.get(homeUrl);
        hp.navigateToTvShows();
        hp.contentPlayBackCheck(-1, -1);
        test.info("TV Shows content selected: " + cdp.getContentName());
        cdp.clickPlayButton();
        player.moveFocusToPlayer();
        Assert.assertTrue(player.isVideoElementDisplayed(), plan + " - TV Shows player not displayed");
        Assert.assertTrue(player.isVideoPlaying(), plan + " - TV Shows content is not playing");
        test.pass(plan + " - TV Shows playback passed");

        driver.get(homeUrl);
        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        String shortsTitle = shortsHome.contentPlayBackCheck(-1, -1);
        test.info("Sun Shorts content selected: " + shortsTitle);
        PlayerSunShorts shortsPlayer = new PlayerSunShorts(driver);
        Assert.assertTrue(shortsPlayer.waitForPlaybackProgress(30), plan + " - Sun Shorts content is not playing");
        test.pass(plan + " - Sun Shorts playback passed");
        Thread.sleep(10000);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            quitDriver();
        } finally {
            driver = null;
        }
    }
}
