package contentPlayback;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentDetailPage;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import pages.PlayerControlsPage;
import pages.ProfilePage;
import config.ConfigReader;
import config.Log;
import reporting.ExtentReportManager;

public class TC_HomePlayback extends TestBase {

    private static final Logger logger = Log.getLogger(TC_HomePlayback.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void homeContentPlaybackTest() throws InterruptedException {
        logger.info("Starting Home playback test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
       // cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));
        test.info("User logged in");

        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        test.info("Primary profile selected");

        hp.contentPlayBackCheck(-1, -1);
        test.info("Content selected from carousel");

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String contentName = cdp.getContentName();
        test.info("Playing: " + contentName);
        cdp.clickPlayButton();

        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.moveFocusToPlayer();
        player.fullScreen();
        player.toggleCaptions("disable");
        player.validatePlayback();

        test.pass("Playback successful in Home section");
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
