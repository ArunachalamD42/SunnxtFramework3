package playerControls;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import config.Log;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.PlayerControlsPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

/**
 * TC_PlayerControls - Exercises the VOD video player controls (Shaka player):
 * pause/play, fullscreen enter/exit, quality change, captions toggle, volume, seek.
 * Uses the first title of the first carousel (no scroll/random) for speed.
 */
public class TC_PlayerControls extends TestBase {

    private static final Logger logger = Log.getLogger(TC_PlayerControls.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void playerControlsTest() throws InterruptedException {
        logger.info("Starting Player Controls test...");

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

        // Profile selection
        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        test.info("Primary profile selected");

        // Navigate to Movies and open the first title of the first carousel
        hp.navigateToMovies();
        test.info("Navigated to Movies");
        hp.selectFirstContent();
        test.info("First content selected from Movies");

        // Start playback
        ContentDetailPage cdp = new ContentDetailPage(driver);
        String contentName = cdp.getContentName();
        test.info("Playing: " + contentName);
        cdp.clickPlayButton();

        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.moveFocusToPlayer();
        player.validatePlayback();
        test.info("Playback started");

        // --- Exercise controls ---

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

        // Seek forward
        player.seekContent(300);
        test.info("Seeked to 60s");
        Thread.sleep(1000);

        // Fullscreen enter/exit
        player.moveFocusToPlayer();
        player.fullScreen();
        test.info("Entered fullscreen");
        Thread.sleep(1000);

        player.moveFocusToPlayer();
        player.exitFullScreen();
        test.info("Exited fullscreen");
        Thread.sleep(1000);

        // Quality change (Advanced -> 720p)
        player.moveFocusToPlayer();
        player.changeQuality("720");
        test.info("Quality changed to 720p");
        Thread.sleep(2000);

        // Captions off
        player.moveFocusToPlayer();
        player.toggleCaptions("enable");
        test.info("Captions disabled");
        Thread.sleep(2000);

        // Confirm playback is still healthy after control interactions
        player.moveFocusToPlayer();
        player.validatePlayback();

        test.pass("Player controls exercised successfully for: " + contentName);
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
