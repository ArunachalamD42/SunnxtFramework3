package contentPlayback;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import config.Log;
import pagesSunnxt.ContentDetailPageLiveTV;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.PlayerControlsPage;
import pagesSunnxt.ProfilePage;
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
        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");
        lp.enterLoginId(username);
        lp.clickContinueButton();
        if (!lp.isPasswordFieldVisible()) {
            lp.clickContinueButton();
        }
        lp.enterPassword(password);
        lp.clickLoginButton();
        lp.handleDeviceLimit();
        test.info("User logged in");

        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        test.info("Primary profile selected");

        hp.navigateToLiveTv();
        test.info("Navigated to Live TV");

        hp.selectRandomLiveChannel();
        test.info("Random live channel selected");

        ContentDetailPageLiveTV liveTvDetail = new ContentDetailPageLiveTV(driver);
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
