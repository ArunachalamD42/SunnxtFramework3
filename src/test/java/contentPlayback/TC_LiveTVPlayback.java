package contentPlayback;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LiveTVDetailPage;
import pages.LoginPage;
import pages.PlayerControlsPage;
import config.ConfigReader;
import config.Log;
import reporting.ExtentReportManager;

public class TC_LiveTVPlayback extends TestBase {

    private static final Logger logger = Log.getLogger(TC_LiveTVPlayback.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void liveTvPlaybackTest() throws InterruptedException {
        logger.info("Starting Live TV playback test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));
        test.info("User logged in");

        waitForLoginSuccess();

        hp.navigateToLiveTv();
        test.info("Navigated to Live TV");

        hp.contentPlayBackCheck(-1, -1);
        test.info("Random channel selected");

        LiveTVDetailPage liveTvDetail = new LiveTVDetailPage(driver);
        liveTvDetail.clickPlayButton();

        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.moveFocusToPlayer();
        player.validatePlayback();

        test.pass("Live TV playback successful");
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
