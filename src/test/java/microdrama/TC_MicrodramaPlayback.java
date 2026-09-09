package microdrama;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import pages.MicrodramaPage;
import pages.PlayerControlsPage;
import config.ConfigReader;
import config.Log;
import reporting.ExtentReportManager;

/**
 * TC_MicrodramaPlayback - Tests Microdrama content playback.
 */
public class TC_MicrodramaPlayback extends TestBase {

    private static final Logger logger = Log.getLogger(TC_MicrodramaPlayback.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void microdramaPlaybackTest() throws InterruptedException {
        logger.info("Starting Microdrama playback test...");

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

        MicrodramaPage md = new MicrodramaPage(driver);
        md.navigateToMicrodrama();
        test.info("Navigated to Microdrama");

        md.scrollToLoadContent();
        md.selectRandomContent();
        test.info("Random content selected");

        md.clickPlay();
        test.info("Play clicked");

        String title = md.getEpisodeTitle();
        test.info("Playing: " + title);

        PlayerControlsPage player = new PlayerControlsPage(driver);
        player.validatePlayback();

        test.pass("Microdrama playback successful");
    }

    @Test(enabled = true)
    public void microdramaEpisodeNavigationTest() throws InterruptedException {
        logger.info("Starting episode navigation test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));

        waitForLoginSuccess();

        MicrodramaPage md = new MicrodramaPage(driver);
        md.navigateToMicrodrama();
        md.selectContent(0);
        md.clickPlay();
        test.info("First content playing");

        String firstTitle = md.getEpisodeTitle();
        test.info("Episode 1: " + firstTitle);

        md.goToNextEpisode();
        Thread.sleep(3000);
        String secondTitle = md.getEpisodeTitle();
        test.info("Episode 2: " + secondTitle);

        md.goToPreviousEpisode();
        Thread.sleep(3000);
        String backTitle = md.getEpisodeTitle();
        test.info("Back to: " + backTitle);

        test.pass("Episode navigation successful");
    }

    @AfterMethod
    public void tearDown() {
        exitIfPlaybackOrSubscription();
    }
}
