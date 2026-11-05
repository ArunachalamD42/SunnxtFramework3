package sunShorts;

import java.lang.reflect.Method;
import java.time.Duration;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.apache.logging.log4j.Logger;

import baseClass.TestBase;
import config.Log;
import config.ConfigReader;
import pagesSunShorts.HomePageSunShorts;
import pagesSunShorts.PlayerSunShorts;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

/** Separate cases for Shorts player controls, rating, episodes and watchlist. */
public class TC_SunShortsPlayerControls extends TestBase {

    private static final Logger logger = Log.getLogger(TC_SunShortsPlayerControls.class);

    private PlayerSunShorts player;
    private String selectedTitle;

    // ==================== SETUP ====================

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        logger.info("Starting test: {}", method.getName());
        player = null;
        setupDriver();
        launchSunnxt();

    }

    // ==================== COMPLETE PLAYER FLOW ====================

    @Test(enabled = false)
    public void completePlayerControlsTest() throws InterruptedException {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        selectedTitle = shortsHome.contentPlayBackCheck(0, 0);
        test.info("First-carousel Sun Shorts content selected: " + selectedTitle);
        logger.info("First-carousel Sun Shorts content selected: " + selectedTitle);

        player = new PlayerSunShorts(driver);
        Assert.assertTrue(player.waitForPlaybackProgress(30), "First-carousel content did not autoplay");

        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Pause and resume
            player.moveFocusToPlayer();
            player.pause();
            Assert.assertTrue(wait.until(d -> player.isPaused()), "Player did not pause");
            test.info("Pause verified");
            logger.info("Pause verified");
            Thread.sleep(2000);

            player.moveFocusToPlayer();
            player.play();
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Playback did not advance after Resume");
            test.info("Resume verified");
            logger.info("Resume verified");

            // Autoplay may start muted. Establish an unmuted state first.
            player.moveFocusToPlayer();
            if (player.isMuted()) {
                player.unMute();
                Assert.assertTrue(wait.until(d -> !player.isMuted()), "Initial unmute failed");
            }
            player.moveFocusToPlayer();
            player.mute();
            Assert.assertTrue(wait.until(d -> player.isMuted()), "Player did not mute");
            test.info("Mute verified");
            logger.info("Mute verified");

            player.moveFocusToPlayer();
            player.unMute();
            Assert.assertTrue(wait.until(d -> !player.isMuted()), "Player did not unmute");
            test.info("Unmute verified");
            logger.info("Unmute verified");

            // Fullscreen enter and exit
            player.moveFocusToPlayer();
            player.fullScreen();
            Assert.assertTrue(wait.until(d -> player.isFullScreen()), "Player did not enter fullscreen");
            test.info("Fullscreen entry verified");
            logger.info("Fullscreen entry verified");
            Thread.sleep(2000);

            player.moveFocusToPlayer();
            player.exitFullScreen();
            Assert.assertTrue(wait.until(d -> !player.isFullScreen()), "Player did not exit fullscreen");
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Playback stopped after fullscreen exit");
            test.info("Fullscreen exit verified");
            logger.info("Fullscreen exit verified");

            test.info("Basic player controls verified");

            logger.info("Basic player controls verified");
        }
        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            boolean initiallyFullScreen = player.isFullScreen();

            try {
                if (!initiallyFullScreen) {
                    player.moveFocusToPlayer();
                    player.fullScreen();
                    Assert.assertTrue(wait.until(d -> player.isFullScreen()), "Player did not enter fullscreen");
                    test.info("Fullscreen entry verified");
                    logger.info("Fullscreen entry verified");
                }

                Assert.assertTrue(player.isNextEpisodeEnabled(), "Next episode is unavailable or disabled");
                player.nextEpisode();
                Assert.assertTrue(player.waitForPlaybackProgress(30), "Next episode did not play");
                test.info("Playback advanced after clicking Next");
                logger.info("Playback advanced after clicking Next");

                Assert.assertTrue(player.isPreviousEpisodeEnabled(), "Previous episode is unavailable or disabled");
                player.previousEpisode();
                Assert.assertTrue(player.waitForPlaybackProgress(30), "Previous episode did not play");
                test.info("Playback advanced after clicking Previous");
                logger.info("Playback advanced after clicking Previous");
            } finally {
                if (!initiallyFullScreen && player.isFullScreen()) {
                    try {
                        // Restore the window without depending on an auto-hidden player button.
                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                                "if(document.fullscreenElement){document.exitFullscreen();}");
                        wait.until(d -> !player.isFullScreen());
                    } catch (RuntimeException cleanupError) {
                        logger.warn("Could not restore fullscreen after episode navigation; teardown will close the browser");
                    }
                }
            }
        }
        {
            String initialRating = player.getSelectedRating();
            String[] ratings = {"love", "like", "not for me"};
            if (initialRating.equals("love")) {
                ratings = new String[] {"like", "not for me", "love"};
            }
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                for (String rating : ratings) {
                    player.rateContent(rating);
                    Assert.assertTrue(wait.until(d -> rating.equals(player.getSelectedRating())),
                            "Rate icon did not change to: " + rating);
                    test.info("Rating verified from icon source: " + rating);
                    logger.info("Rating verified from icon source: " + rating);
                }
            } finally {
                // Restore an existing rating without toggling an already selected option.
                if (!initialRating.isEmpty() && !initialRating.equals(player.getSelectedRating())) {
                    player.rateContent(initialRating);
                    wait.until(d -> initialRating.equals(player.getSelectedRating()));
                }
            }
            test.info("All rating icons verified");
            logger.info("All rating icons verified");
        }

        {
            String contentUrl = driver.getCurrentUrl();

            {
                shortsHome.navigateToHome();
                Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Home carousels did not load");
                String homeUrl = driver.getCurrentUrl();
                shortsHome.navigateToWatchlist();
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !homeUrl.equals(d.getCurrentUrl()));
                // A fresh Watchlist load also verifies persistence and excludes leftover Home cards.
                driver.navigate().refresh();
            }
            if (shortsHome.isContentDisplayed(selectedTitle)) {
                throw new SkipException("First-carousel title is already saved; use an account without this title in Watchlist");
            }

            driver.get(contentUrl);
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Selected content did not reopen");
            player.clickWatchlist();

            {
                shortsHome.navigateToHome();
                Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Home carousels did not load");
                String homeUrl = driver.getCurrentUrl();
                shortsHome.navigateToWatchlist();
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !homeUrl.equals(d.getCurrentUrl()));
                // A fresh Watchlist load also verifies persistence and excludes leftover Home cards.
                driver.navigate().refresh();
            }
            Assert.assertTrue(shortsHome.isContentDisplayed(selectedTitle),
                    "Selected title was not found in Watchlist after adding it: " + selectedTitle);
            test.info("Selected title is present in Watchlist: " + selectedTitle);
            logger.info("Selected title is present in Watchlist: " + selectedTitle);
        }
        test.info("Keeping the final result visible for 10 seconds before closing the browser");
        logger.info("Keeping the final result visible for 10 seconds before closing the browser");
        Thread.sleep(10000);
        test.pass("Player controls, rating, Next, Previous and Watchlist verified in one session");
        logger.info("Player controls, rating, Next, Previous and Watchlist verified in one session");
    }
    // ==================== PLAYER CONTROLS ====================

    @Test(enabled = false)
    public void playerControlsTest() throws InterruptedException {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        selectedTitle = shortsHome.contentPlayBackCheck(0, 0);
        test.info("First-carousel Sun Shorts content selected: " + selectedTitle);
        logger.info("First-carousel Sun Shorts content selected: " + selectedTitle);

        player = new PlayerSunShorts(driver);
        Assert.assertTrue(player.waitForPlaybackProgress(30), "First-carousel content did not autoplay");

        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Pause and resume
            player.moveFocusToPlayer();
            player.pause();
            Assert.assertTrue(wait.until(d -> player.isPaused()), "Player did not pause");
            test.info("Pause verified");
            logger.info("Pause verified");
            Thread.sleep(2000);

            player.moveFocusToPlayer();
            player.play();
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Playback did not advance after Resume");
            test.info("Resume verified");
            logger.info("Resume verified");

            // Autoplay may start muted. Establish an unmuted state first.
            player.moveFocusToPlayer();
            if (player.isMuted()) {
                player.unMute();
                Assert.assertTrue(wait.until(d -> !player.isMuted()), "Initial unmute failed");
            }
            player.moveFocusToPlayer();
            player.mute();
            Assert.assertTrue(wait.until(d -> player.isMuted()), "Player did not mute");
            test.info("Mute verified");
            logger.info("Mute verified");

            player.moveFocusToPlayer();
            player.unMute();
            Assert.assertTrue(wait.until(d -> !player.isMuted()), "Player did not unmute");
            test.info("Unmute verified");
            logger.info("Unmute verified");

            // Fullscreen enter and exit
            player.moveFocusToPlayer();
            player.fullScreen();
            Assert.assertTrue(wait.until(d -> player.isFullScreen()), "Player did not enter fullscreen");
            test.info("Fullscreen entry verified");
            logger.info("Fullscreen entry verified");
            Thread.sleep(2000);

            player.moveFocusToPlayer();
            player.exitFullScreen();
            Assert.assertTrue(wait.until(d -> !player.isFullScreen()), "Player did not exit fullscreen");
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Playback stopped after fullscreen exit");
            test.info("Fullscreen exit verified");
            logger.info("Fullscreen exit verified");

            test.info("Basic player controls verified");

            logger.info("Basic player controls verified");
        }
        test.info("Keeping playback visible for 10 seconds before closing the browser");
        logger.info("Keeping playback visible for 10 seconds before closing the browser");
        Thread.sleep(10000);
        test.pass("Sun Shorts pause, resume, mute, unmute and fullscreen controls passed");
        logger.info("Sun Shorts pause, resume, mute, unmute and fullscreen controls passed");
    }

    // ==================== RATING ====================

    @Test(enabled = false)
    public void ratingTest() {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        selectedTitle = shortsHome.contentPlayBackCheck(0, 0);
        test.info("First-carousel Sun Shorts content selected: " + selectedTitle);
        logger.info("First-carousel Sun Shorts content selected: " + selectedTitle);

        player = new PlayerSunShorts(driver);
        Assert.assertTrue(player.waitForPlaybackProgress(30), "First-carousel content did not autoplay");

        {
            String initialRating = player.getSelectedRating();
            String[] ratings = {"love", "like", "not for me"};
            if (initialRating.equals("love")) {
                ratings = new String[] {"like", "not for me", "love"};
            }
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                for (String rating : ratings) {
                    player.rateContent(rating);
                    Assert.assertTrue(wait.until(d -> rating.equals(player.getSelectedRating())),
                            "Rate icon did not change to: " + rating);
                    test.info("Rating verified from icon source: " + rating);
                    logger.info("Rating verified from icon source: " + rating);
                }
            } finally {
                // Restore an existing rating without toggling an already selected option.
                if (!initialRating.isEmpty() && !initialRating.equals(player.getSelectedRating())) {
                    player.rateContent(initialRating);
                    wait.until(d -> initialRating.equals(player.getSelectedRating()));
                }
            }
            test.info("All rating icons verified");
            logger.info("All rating icons verified");
        }
        test.pass("Love, Like and Not for me rating icons verified");
        logger.info("Love, Like and Not for me rating icons verified");
    }

    // ==================== NEXT AND PREVIOUS EPISODE ====================

    @Test(enabled = true)
    public void nextAndPreviousEpisodeTest() throws InterruptedException {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        selectedTitle = shortsHome.contentPlayBackCheck(0, 0);
        test.info("First-carousel Sun Shorts content selected: " + selectedTitle);
        logger.info("First-carousel Sun Shorts content selected: " + selectedTitle);

        player = new PlayerSunShorts(driver);
        Assert.assertTrue(player.waitForPlaybackProgress(30), "First-carousel content did not autoplay");

        {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            boolean initiallyFullScreen = player.isFullScreen();

            try {
                if (!initiallyFullScreen) {
                    player.moveFocusToPlayer();
                    player.fullScreen();
                    Assert.assertTrue(wait.until(d -> player.isFullScreen()), "Player did not enter fullscreen");
                    test.info("Fullscreen entry verified");
                    logger.info("Fullscreen entry verified");
                }

                Assert.assertTrue(player.isNextEpisodeEnabled(), "Next episode is unavailable or disabled");
                player.nextEpisode();
                Assert.assertTrue(player.waitForPlaybackProgress(30), "Next episode did not play");
                test.info("Playback advanced after clicking Next");
                logger.info("Playback advanced after clicking Next");

                Assert.assertTrue(player.isPreviousEpisodeEnabled(), "Previous episode is unavailable or disabled");
                player.previousEpisode();
                Assert.assertTrue(player.waitForPlaybackProgress(30), "Previous episode did not play");
                test.info("Playback advanced after clicking Previous");
                logger.info("Playback advanced after clicking Previous");
            } finally {
                if (!initiallyFullScreen && player.isFullScreen()) {
                    try {
                        // Restore the window without depending on an auto-hidden player button.
                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                                "if(document.fullscreenElement){document.exitFullscreen();}");
                        wait.until(d -> !player.isFullScreen());
                    } catch (RuntimeException cleanupError) {
                        logger.warn("Could not restore fullscreen after episode navigation; teardown will close the browser");
                    }
                }
            }
        }
        test.pass("Next and Previous controls clicked; playback verified after each");
        logger.info("Next and Previous controls clicked; playback verified after each");
    }

    // ==================== WATCHLIST ====================

    @Test(enabled = false)
    public void addToWatchlistTest() {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selection completed");
        logger.info("Language selection completed");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach the profile selection page");
        test.info("Login successful");
        logger.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile for this test");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30, org.openqa.selenium.By.xpath(
                "//div[@class='relative group inline-block pointer-events-auto']"));
        test.info("Primary profile selected");
        logger.info("Primary profile selected");

        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
                "Experience selection dialog did not close");
        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
        Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Sun Shorts carousels were not loaded");

        selectedTitle = shortsHome.contentPlayBackCheck(0, 0);
        test.info("First-carousel Sun Shorts content selected: " + selectedTitle);
        logger.info("First-carousel Sun Shorts content selected: " + selectedTitle);

        player = new PlayerSunShorts(driver);
        Assert.assertTrue(player.waitForPlaybackProgress(30), "First-carousel content did not autoplay");

        {
            String contentUrl = driver.getCurrentUrl();

            {
                shortsHome.navigateToHome();
                Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Home carousels did not load");
                String homeUrl = driver.getCurrentUrl();
                shortsHome.navigateToWatchlist();
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !homeUrl.equals(d.getCurrentUrl()));
                // A fresh Watchlist load also verifies persistence and excludes leftover Home cards.
                driver.navigate().refresh();
            }
            if (shortsHome.isContentDisplayed(selectedTitle)) {
                throw new SkipException("First-carousel title is already saved; use an account without this title in Watchlist");
            }

            driver.get(contentUrl);
            Assert.assertTrue(player.waitForPlaybackProgress(30), "Selected content did not reopen");
            player.clickWatchlist();

            {
                shortsHome.navigateToHome();
                Assert.assertTrue(shortsHome.getCarouselCount() > 0, "Home carousels did not load");
                String homeUrl = driver.getCurrentUrl();
                shortsHome.navigateToWatchlist();
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !homeUrl.equals(d.getCurrentUrl()));
                // A fresh Watchlist load also verifies persistence and excludes leftover Home cards.
                driver.navigate().refresh();
            }
            Assert.assertTrue(shortsHome.isContentDisplayed(selectedTitle),
                    "Selected title was not found in Watchlist after adding it: " + selectedTitle);
            test.info("Selected title is present in Watchlist: " + selectedTitle);
            logger.info("Selected title is present in Watchlist: " + selectedTitle);
        }
        test.pass("Selected title is present in Watchlist");
        logger.info("Selected title is present in Watchlist");
    }

    // ==================== CLEANUP ====================

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (test != null && result.getStatus() == ITestResult.SKIP) {
                test.skip("Case skipped; see the TestNG result for the reason");
                logger.warn("Case skipped; see the TestNG result for the reason");
            } else if (test != null && !result.isSuccess()) {
                test.fail("Sun Shorts player test failed; see TestNG results");
                logger.error("Sun Shorts player test failed; see TestNG results");
            }
        } finally {
            quitDriver();
            driver = null;
        }
    }
}
