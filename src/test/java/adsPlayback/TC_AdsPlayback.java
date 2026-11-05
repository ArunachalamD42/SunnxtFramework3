package adsPlayback;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import pagesSunShorts.HomePageSunShorts;
import pagesSunShorts.PlayerSunShorts;
import pagesSunnxt.AdsPage;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentDetailPageLiveTV;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.PlayerEntitlementPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

public class TC_AdsPlayback extends TestBase {

    private String username;
    private String password;
    private int observationSeconds;
    private AdsPage adsPage;

    @DataProvider(name = "accounts")
    public Object[][] accounts() {
        String configuredAccounts = ConfigReader.get("ads.accounts");
        if (configuredAccounts == null || configuredAccounts.isBlank()) {
            throw new IllegalArgumentException("Set ads.accounts in the ads user configuration");
        }
        List<Object[]> users = new ArrayList<>();
        Set<String> aliases = new HashSet<>();
        for (String value : configuredAccounts.split(",", -1)) {
            String account = value.trim();
            if (!account.matches("[A-Z][A-Z0-9_]*") || !aliases.add(account)) {
                throw new IllegalArgumentException("Ads account aliases must be valid and unique");
            }
            String prefix = "ads." + account + ".";
            String enabled = ConfigReader.get(prefix + "enabled");
            if (!"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
                throw new IllegalArgumentException("Set " + prefix + "enabled to true or false");
            }
            if ("true".equalsIgnoreCase(enabled)) {
                String plan = ConfigReader.get(prefix + "plan");
                if (plan == null || plan.isBlank()) {
                    throw new IllegalArgumentException("Missing plan name for " + account);
                }
                users.add(new Object[] {account, plan});
            }
        }
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Enable at least one account in the ads user configuration");
        }
        return users.toArray(new Object[0][]);
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method, Object[] data) {
        String plan = (String) data[1];
        test = ExtentReportManager.createTest(method.getName() + " - " + plan);
    }

    @Test(enabled = true, dataProvider = "accounts")
    public void completeAdsPlaybackTest(String account, String plan) throws InterruptedException {
        String prefix = "SUNNXT_ENTITLEMENT_" + account + "_CREDENTIAL_";
        String runtimeUsername = System.getenv(prefix + "MOBILE");
        String runtimePassword = System.getenv(prefix + "PASSWORD");
        boolean hasRuntimeUsername = runtimeUsername != null && !runtimeUsername.isBlank();
        boolean hasRuntimePassword = runtimePassword != null && !runtimePassword.isBlank();
        if (hasRuntimeUsername != hasRuntimePassword) {
            throw new SkipException("Incomplete environment credentials for " + account
                    + "; supply both MOBILE and PASSWORD or remove both to use ConfigReader");
        }
        username = ConfigReader.getRuntime(prefix + "MOBILE");
        password = ConfigReader.getRuntime(prefix + "PASSWORD");
        if ((username == null || username.isBlank()) && (password == null || password.isBlank())) {
            String usernameKey = ConfigReader.get("ads." + account + ".username.config.key");
            String passwordKey = ConfigReader.get("ads." + account + ".password.config.key");
            if (usernameKey != null && !usernameKey.isBlank() && passwordKey != null && !passwordKey.isBlank()) {
                username = ConfigReader.get(usernameKey);
                password = ConfigReader.get(passwordKey);
            }
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Missing complete credentials for " + account
                    + "; configure " + prefix + "MOBILE and " + prefix + "PASSWORD in the environment or ConfigReader");
        }
        test.info("Credential source for " + account + ": " + (hasRuntimeUsername ? "environment" : "ConfigReader"));
        String seconds = ConfigReader.getRuntime("SUNNXT_AD_OBSERVATION_SECONDS");
        if (seconds == null || seconds.isBlank()) {
            throw new SkipException("Set SUNNXT_AD_OBSERVATION_SECONDS in the environment or ConfigReader");
        }
        observationSeconds = Integer.parseInt(seconds.trim());
        Assert.assertTrue(observationSeconds > 0, "Ad observation seconds must be positive");
        String userSettings = "ads." + account + ".";
        String snExpected = ConfigReader.get(userSettings + "sn.expected");
        String adsExpected = ConfigReader.get(userSettings + "ads.expected");
        String shortsExpected = ConfigReader.get(userSettings + "shorts.expected");
        Assert.assertTrue("PLAY".equals(snExpected) || "BLOCK".equals(snExpected), "Expected SN result must be PLAY or BLOCK");
        Assert.assertTrue("ADS".equals(adsExpected) || "NO_ADS".equals(adsExpected), "Expected ad result must be ADS or NO_ADS");
        Assert.assertTrue("FULL".equals(shortsExpected) || "FIRST_7".equals(shortsExpected), "Expected Shorts access must be FULL or FIRST_7");
        boolean snsOnly = snExpected.equals("BLOCK");
        boolean expectsAds = adsExpected.equals("ADS");
        boolean limitedShorts = shortsExpected.equals("FIRST_7");
        int episodesToWatch = limitedShorts ? 7 : 6;
        boolean pendingAdCheck = false;
        setupDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        launchSunnxt();
        adsPage = new AdsPage(driver, networkUtils);

        test.info(plan + " - Movies, Live TV, TV Shows and Shorts");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();
        LoginPage lp = new LoginPage(driver);
        try {
            lp.enterLoginId(username);
            lp.clickContinueButton();
            if (!lp.isPasswordFieldVisible()) {
                lp.clickContinueButton();
            }
            lp.enterPassword(password);
            lp.clickLoginButton();
        } catch (RuntimeException e) {
            Assert.fail("Credential entry failed; check the local account configuration");
        }
        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }
        Assert.assertTrue(lp.isLoggedIn(), "Login did not reach profile selection");
        test.info("Login successful");

        ProfilePage profile = new ProfilePage(driver);
        Assert.assertTrue(profile.isProfilePageDisplayed(), "Profile selection page was not displayed");
        Assert.assertFalse(profile.isProfileLocked(1), "Primary profile requires a PIN");
        Assert.assertFalse(profile.isKidsProfile(1), "Use an adult primary profile");
        profile.selectPrimaryProfile();
        hp.waitForElementToBeVisible(30,
                By.xpath("//div[@class='relative group inline-block pointer-events-auto']"));
        String snHomeUrl = driver.getCurrentUrl();
        test.info("Primary profile selected");
        PlayerEntitlementPage restriction = new PlayerEntitlementPage(driver);

        // ==================== Movie ====================
        hp.navigateToMovies();
        hp.contentPlayBackCheck(-1, -1);
        ContentDetailPage movie = new ContentDetailPage(driver);
        String movieTitle = movie.getContentName();
        Assert.assertFalse(movieTitle.isBlank(), "Movie content detail page was not reached");
        test.info("Selected Movie title: " + movieTitle);
        adsPage.startAdObservation();
        if (snsOnly) {
            if (!movie.isSubscribeToWatchDisplayed()) {
                movie.clickPlayButton();
            }
            Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    movie.isSubscribeToWatchDisplayed() || restriction.isSubscriptionPromptShown()),
                    "SNS-only account was not shown a Movie subscription restriction");
            Assert.assertFalse(adsPage.waitForContentPlayback(5), "Restricted Movie content played");
            test.info("Expected Movie access restriction verified");
        } else {
            movie.clickPlayButton();
            adsPage.observeAds(observationSeconds);
            Assert.assertTrue(adsPage.waitForContentPlayback(60), "Movie playback did not advance");
            test.info("Movie playback verified independently of ads");
            adsPage.checkAds();
            boolean moviePreRollVisible = adsPage.wasAdVisible();
            boolean moviePreRollRequested = adsPage.wasAdRequested();
            boolean moviePreRollCaptureAvailable = adsPage.isNetworkCaptureAvailable();
            String moviePreRollRequestResult = moviePreRollCaptureAvailable ? String.valueOf(moviePreRollRequested) : "capture unavailable or no traffic observed";
            test.info("Movie pre-roll - ad visible: " + moviePreRollVisible);
            test.info("Movie pre-roll - ad request: " + moviePreRollRequestResult);
            logger.info("Movie pre-roll: ad visible={}, ad request={}", moviePreRollVisible, moviePreRollRequestResult);
            if (expectsAds) {
                if (!moviePreRollVisible && !moviePreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("Movie pre-roll could not be verified; continuing to the next section");
                } else {
                    Assert.assertTrue(moviePreRollVisible || moviePreRollRequested, "Movie pre-roll: neither ad nor ad request detected");
                }
            } else {
                Assert.assertFalse(moviePreRollVisible || moviePreRollRequested, "Movie pre-roll: unexpected ad detected");
                if (!moviePreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("Movie pre-roll: network evidence unavailable; absence of ads is unverified");
                }
            }

            if (adsPage.canSeekToMidRoll()) {
                adsPage.startAdObservation();
                adsPage.seekToMidRoll();
                adsPage.observeAds(observationSeconds);
                Assert.assertTrue(adsPage.waitForContentPlayback(60), "Movie playback failed after 15:00 seek");
                adsPage.checkAds();
                boolean movieMidRollVisible = adsPage.wasAdVisible();
                boolean movieMidRollRequested = adsPage.wasAdRequested();
                boolean movieMidRollCaptureAvailable = adsPage.isNetworkCaptureAvailable();
                String movieMidRollRequestResult = movieMidRollCaptureAvailable ? String.valueOf(movieMidRollRequested) : "capture unavailable or no traffic observed";
                test.info("Movie mid-roll at 15:00 - ad visible: " + movieMidRollVisible);
                test.info("Movie mid-roll at 15:00 - ad request: " + movieMidRollRequestResult);
                logger.info("Movie mid-roll at 15:00: ad visible={}, ad request={}", movieMidRollVisible, movieMidRollRequestResult);
                if (expectsAds) {
                    if (!movieMidRollVisible && !movieMidRollCaptureAvailable) {
                        pendingAdCheck = true;
                        test.warning("Movie mid-roll at 15:00 could not be verified; continuing to the next section");
                    } else {
                        Assert.assertTrue(movieMidRollVisible || movieMidRollRequested, "Movie mid-roll at 15:00: neither ad nor ad request detected");
                    }
                } else {
                    Assert.assertFalse(movieMidRollVisible || movieMidRollRequested, "Movie mid-roll at 15:00: unexpected ad detected");
                    if (!movieMidRollCaptureAvailable) {
                        pendingAdCheck = true;
                        test.warning("Movie mid-roll at 15:00: network evidence unavailable; absence of ads is unverified");
                    }
                }

            } else {
                pendingAdCheck = true;
                test.warning("Movie title is shorter than 15:00; mid-roll unverified, continuing");
            }
        }

        // ==================== LIVE TV ====================
        driver.get(snHomeUrl);
        hp.navigateToLiveTv();
        hp.selectRandomLiveChannel();
        ContentDetailPageLiveTV live = new ContentDetailPageLiveTV(driver);
        String channel = new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            String name = live.getChannelName();
            return name.isBlank() ? null : name;
        });
        test.info("Selected Live TV channel: " + channel);
        adsPage.startAdObservation();
        if (snsOnly) {
            if (!live.isSubscribeToWatchDisplayed() && !live.isSubscriptionPromptShown()) {
                live.clickPlayButton();
            }
            Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    live.isSubscribeToWatchDisplayed() || live.isSubscriptionPromptShown()),
                    "SNS-only account was not shown a Live TV subscription restriction");
            Assert.assertFalse(adsPage.waitForContentPlayback(5), "Restricted Live TV channel played");
            test.info("Expected Live TV access restriction verified");
        } else {
            live.clickPlayButton();
            adsPage.observeAds(observationSeconds);
            Assert.assertTrue(adsPage.waitForContentPlayback(60), "Live TV playback did not advance");
            test.info("Live TV playback verified independently of ads");
            adsPage.checkAds();
            boolean livePreRollVisible = adsPage.wasAdVisible();
            boolean livePreRollRequested = adsPage.wasAdRequested();
            boolean livePreRollCaptureAvailable = adsPage.isNetworkCaptureAvailable();
            String livePreRollRequestResult = livePreRollCaptureAvailable ? String.valueOf(livePreRollRequested) : "capture unavailable or no traffic observed";
            test.info("Live TV pre-roll - ad visible: " + livePreRollVisible);
            test.info("Live TV pre-roll - ad request: " + livePreRollRequestResult);
            logger.info("Live TV pre-roll: ad visible={}, ad request={}", livePreRollVisible, livePreRollRequestResult);
            if (expectsAds) {
                if (!livePreRollVisible && !livePreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("Live TV pre-roll could not be verified; continuing to the next section");
                } else {
                    Assert.assertTrue(livePreRollVisible || livePreRollRequested, "Live TV pre-roll: neither ad nor ad request detected");
                }
            } else {
                Assert.assertFalse(livePreRollVisible || livePreRollRequested, "Live TV pre-roll: unexpected ad detected");
                if (!livePreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("Live TV pre-roll: network evidence unavailable; absence of ads is unverified");
                }
            }
        }

        // ==================== TV Show ====================
        driver.get(snHomeUrl);
        hp.navigateToTvShows();
        hp.contentPlayBackCheck(-1, -1);
        ContentDetailPage show = new ContentDetailPage(driver);
        String showTitle = show.getContentName();
        Assert.assertFalse(showTitle.isBlank(), "TV Show content detail page was not reached");
        test.info("Selected TV Show title: " + showTitle);
        adsPage.startAdObservation();
        if (snsOnly) {
            if (!show.isSubscribeToWatchDisplayed()) {
                show.clickPlayButton();
            }
            Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    show.isSubscribeToWatchDisplayed() || restriction.isSubscriptionPromptShown()),
                    "SNS-only account was not shown a TV Show subscription restriction");
            Assert.assertFalse(adsPage.waitForContentPlayback(5), "Restricted TV Show content played");
            test.info("Expected TV Show access restriction verified");
        } else {
            show.clickPlayButton();
            adsPage.observeAds(observationSeconds);
            Assert.assertTrue(adsPage.waitForContentPlayback(60), "TV Show playback did not advance");
            test.info("TV Show playback verified independently of ads");
            adsPage.checkAds();
            boolean showPreRollVisible = adsPage.wasAdVisible();
            boolean showPreRollRequested = adsPage.wasAdRequested();
            boolean showPreRollCaptureAvailable = adsPage.isNetworkCaptureAvailable();
            String showPreRollRequestResult = showPreRollCaptureAvailable ? String.valueOf(showPreRollRequested) : "capture unavailable or no traffic observed";
            test.info("TV Show pre-roll - ad visible: " + showPreRollVisible);
            test.info("TV Show pre-roll - ad request: " + showPreRollRequestResult);
            logger.info("TV Show pre-roll: ad visible={}, ad request={}", showPreRollVisible, showPreRollRequestResult);
            if (expectsAds) {
                if (!showPreRollVisible && !showPreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("TV Show pre-roll could not be verified; continuing to the next section");
                } else {
                    Assert.assertTrue(showPreRollVisible || showPreRollRequested, "TV Show pre-roll: neither ad nor ad request detected");
                }
            } else {
                Assert.assertFalse(showPreRollVisible || showPreRollRequested, "TV Show pre-roll: unexpected ad detected");
                if (!showPreRollCaptureAvailable) {
                    pendingAdCheck = true;
                    test.warning("TV Show pre-roll: network evidence unavailable; absence of ads is unverified");
                }
            }

            if (adsPage.canSeekToMidRoll()) {
                adsPage.startAdObservation();
                adsPage.seekToMidRoll();
                adsPage.observeAds(observationSeconds);
                Assert.assertTrue(adsPage.waitForContentPlayback(60), "TV Show playback failed after 15:00 seek");
                adsPage.checkAds();
                boolean showMidRollVisible = adsPage.wasAdVisible();
                boolean showMidRollRequested = adsPage.wasAdRequested();
                boolean showMidRollCaptureAvailable = adsPage.isNetworkCaptureAvailable();
                String showMidRollRequestResult = showMidRollCaptureAvailable ? String.valueOf(showMidRollRequested) : "capture unavailable or no traffic observed";
                test.info("TV Show mid-roll at 15:00 - ad visible: " + showMidRollVisible);
                test.info("TV Show mid-roll at 15:00 - ad request: " + showMidRollRequestResult);
                logger.info("TV Show mid-roll at 15:00: ad visible={}, ad request={}", showMidRollVisible, showMidRollRequestResult);
                if (expectsAds) {
                    if (!showMidRollVisible && !showMidRollCaptureAvailable) {
                        pendingAdCheck = true;
                        test.warning("TV Show mid-roll at 15:00 could not be verified; continuing to the next section");
                    } else {
                        Assert.assertTrue(showMidRollVisible || showMidRollRequested, "TV Show mid-roll at 15:00: neither ad nor ad request detected");
                    }
                } else {
                    Assert.assertFalse(showMidRollVisible || showMidRollRequested, "TV Show mid-roll at 15:00: unexpected ad detected");
                    if (!showMidRollCaptureAvailable) {
                        pendingAdCheck = true;
                        test.warning("TV Show mid-roll at 15:00: network evidence unavailable; absence of ads is unverified");
                    }
                }

            } else {
                pendingAdCheck = true;
                test.warning("TV Show title is shorter than 15:00; mid-roll unverified, continuing");
            }
        }

        // ==================== SHORTS ====================
        driver.get(snHomeUrl);
        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
        shortsHome.shortsNavigation();
        adsPage.startAdObservation();
        String shortsTitle = shortsHome.contentPlayBackCheck(-1, -1);
        test.info("Selected Shorts title: " + shortsTitle);
        Assert.assertTrue(adsPage.waitForContentPlayback(60), "Shorts did not start playback");
        PlayerSunShorts player = new PlayerSunShorts(driver);
        if (limitedShorts && !player.isEpisodeAvailable(8)) {
            throw new SkipException("Selected Shorts title has no episode 8; seven-episode boundary cannot be verified");
        }
        player.fullScreen();
        Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> player.isFullScreen()),
                "Shorts player did not enter fullscreen");
        int current = player.getActiveEpisodeNumber();
        while (current > 1) {
            int expected = current - 1;
            Assert.assertTrue(player.isPreviousEpisodeEnabled(), "Previous episode unavailable");
            player.previousEpisode();
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
                adsPage.checkAds();
                return player.getActiveEpisodeNumber() == expected;
            });
            current = player.getActiveEpisodeNumber();
        }
        for (int episode = 1; episode <= episodesToWatch; episode++) {
            Assert.assertEquals(player.getActiveEpisodeNumber(), episode, "Unexpected active episode");
            Assert.assertTrue(adsPage.waitForContentPlayback(60), "Shorts playback did not advance");
            test.info("Playing Shorts episode: " + episode);
            adsPage.observeAds(10);
            if (!expectsAds) {
                test.info("Shorts ad visible: " + adsPage.wasAdVisible());
                test.info("Shorts ad request detected: " + adsPage.wasAdRequested());
                Assert.assertFalse(adsPage.wasAdVisible(), "Unexpected visible ad in annual-plan Shorts sample");
                Assert.assertFalse(adsPage.wasAdRequested(), "Unexpected ad request in annual-plan Shorts sample");
            }
            if (episode < episodesToWatch) {
                if (episode == 5 && expectsAds) {
                    adsPage.startAdObservation();
                }
                int expected = episode + 1;
                Assert.assertTrue(player.isNextEpisodeEnabled(), "Next episode unavailable before the playback sample completed");
                player.nextEpisode();
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
                    adsPage.checkAds();
                    return player.getActiveEpisodeNumber() == expected;
                });
                test.info("SNS episode changed: " + episode + " -> " + expected);
                logger.info("SNS episode changed: {} -> {}", episode, expected);
                if (episode == 5) {
                    adsPage.observeAds(observationSeconds);
                }
            }
        }
        adsPage.checkAds();
        boolean shortsVisible = adsPage.wasAdVisible();
        boolean shortsRequested = adsPage.wasAdRequested();
        boolean shortsCaptureAvailable = adsPage.isNetworkCaptureAvailable();
        String shortsRequestResult = shortsCaptureAvailable ? String.valueOf(shortsRequested) : "capture unavailable or no traffic observed";
        test.info("Shorts " + episodesToWatch + "-episode observation - ad visible: " + shortsVisible);
        test.info("Shorts " + episodesToWatch + "-episode observation - ad request: " + shortsRequestResult);
        logger.info("Shorts {}-episode observation: ad visible={}, ad request={}", episodesToWatch, shortsVisible, shortsRequestResult);
        if (expectsAds) {
            if (!shortsVisible && !shortsCaptureAvailable) {
                pendingAdCheck = true;
                test.warning("Shorts ad observation could not be verified");
            } else {
                Assert.assertTrue(shortsVisible || shortsRequested, "Shorts observation: neither ad nor ad request detected");
            }
        } else {
            Assert.assertFalse(shortsVisible || shortsRequested, "Shorts observation: unexpected ad detected");
            if (!shortsCaptureAvailable) {
                pendingAdCheck = true;
                test.warning("Shorts observation: network evidence unavailable; absence of ads is unverified");
            }
        }

        if (limitedShorts) {
            // Episode 7 may keep playing behind the paywall; verify episode 8 did not become active.
            player.moveFocusToPlayer();
            player.exitFullScreen();
            Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !player.isFullScreen()),
                    "Could not exit fullscreen to select paid episode 8");
            player.selectEpisode(8);
            Assert.assertTrue(new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    player.isSubscriptionPopupDisplayed()), "Episode 8 did not show Keep the Drama Going");
            Assert.assertEquals(player.getActiveEpisodeNumber(), 7,
                    "Paid episode 8 must not become the active playing episode");
            test.info("Shorts episodes 1-7 played; episode 8 showed the expected subscription popup");
            logger.info("Shorts seven-episode access boundary verified for {}", plan);
        }

        if (pendingAdCheck) {
            throw new SkipException("Full flow completed, but an ad check or 15:00 mid-roll remains unverified; see report");
        }
        test.pass(plan + " - Movie, Live TV, TV Show and Shorts expectations verified");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (test != null && result.getStatus() == ITestResult.SKIP) {
                test.skip("Check skipped; see TestNG result and section messages for the reason");
            } else if (test != null && !result.isSuccess()) {
                test.fail("Playback/ad flow failed; see TestNG result and section messages");
            }
        } finally {
            quitDriver();
            driver = null;
            networkUtils = null;
            adsPage = null;
            username = null;
            password = null;
        }
    }
}
