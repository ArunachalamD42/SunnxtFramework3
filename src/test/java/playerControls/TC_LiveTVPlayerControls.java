package playerControls;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentDetailPageLiveTV;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import pages.PlayerControlsPage;
import pages.ProfilePage;
import config.ConfigReader;
import config.Log;
import reporting.ExtentReportManager;

/**
 * TC_LiveTVPlayerControls - Exercises player controls for a Live TV channel
 * (Shaka player): pause/play, volume, fullscreen enter/exit.
 * Seek, quality-advanced, and captions are skipped as they are not
 * applicable to live streams.
 */
public class TC_LiveTVPlayerControls extends TestBase {

    private static final Logger logger = Log.getLogger(TC_LiveTVPlayerControls.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void liveTvPlayerControlsTest() throws InterruptedException {
        logger.info("Starting Live TV Player Controls test...");

        // Language selection
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        // Login
        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();
        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));
        test.info("User logged in");

        // Profile selection
        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        test.info("Primary profile selected");

        // Navigate to Live TV and open the first channel of the first carousel
        hp.navigateToLiveTv();
        test.info("Navigated to Live TV");
        hp.selectFirstLiveChannel();
        test.info("First live channel selected");

        // Start live playback
        ContentDetailPageLiveTV liveTvDetail = new ContentDetailPageLiveTV(driver);
        String channelName = liveTvDetail.getChannelName();
        test.info("Playing channel: " + channelName);
        liveTvDetail.clickPlayButton();

        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.moveFocusToPlayer();
        player.validatePlayback();
        test.info("Live playback started");

        // --- Exercise applicable controls ---

        // Pause then resume
        player.moveFocusToPlayer();
        player.pause();
        test.info("Paused");
        Thread.sleep(2000);

        player.moveFocusToPlayer();
        player.play();
        test.info("Resumed");
        Thread.sleep(2000);

        // Volume
        player.setVolume(0.2);
        test.info("Volume set to 0.2");
        Thread.sleep(1000);
        player.setVolume(0.8);
        test.info("Volume set to 0.8");
        Thread.sleep(1000);

        // Fullscreen enter/exit
        player.moveFocusToPlayer();
        player.fullScreen();
        test.info("Entered fullscreen");
        Thread.sleep(2000);

        player.moveFocusToPlayer();
        player.exitFullScreen();
        test.info("Exited fullscreen");
        Thread.sleep(2000);

        // Note: Live channels do not support captions, so that control is skipped.

        // Confirm live playback is still healthy after control interactions
        player.moveFocusToPlayer();
        player.validatePlayback();

        test.pass("Live TV player controls exercised successfully for: " + channelName);
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
