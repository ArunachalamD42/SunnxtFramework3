package pages;

import java.time.Duration;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import config.Log;
import helpers.WaitUtils;

/**
 * PlayerControlsPage - Video player interactions and validation.
 */
public class PlayerControlsPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(PlayerControlsPage.class);

    public PlayerControlsPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "player_html5_api")
    private WebElement player;

    @FindBy(xpath = "//video[@class='shaka-video']")
    private WebElement shakaPlayer;

    @FindBy(xpath = "//button[@title='Fullscreen']")
    private WebElement fullScreenButton;

    @FindBy(xpath = "//button[@title='Exit Fullscreen']")
    private WebElement exitFullScreenButton;

    @FindBy(xpath = "//button[@title='Play']")
    private WebElement playButton;

    @FindBy(xpath = "//button[@title='Pause']")
    private WebElement pauseButton;

    @FindBy(xpath = "//button[@title='Captions']")
    private WebElement captionsButton;

    @FindBy(xpath = "//span[text()='captions off']")
    private WebElement captionsOff;

    @FindBy(xpath = "//span[text()='English']")
    private WebElement captionsEnglish;

    @FindBy(xpath = "(//div[@class='vjs-menu'])[2]")
    private WebElement qualityMenu;

    @FindBy(xpath = "//li[text()='1080p']")
    private WebElement quality1080;

    @FindBy(xpath = "//li[text()='720p']")
    private WebElement quality720;

    @FindBy(xpath = "//li[text()='360p']")
    private WebElement quality360;

    @FindBy(xpath = "//li[text()='Auto']")
    private WebElement qualityAuto;

    @FindBy(xpath = "//h2[text()='Subscriptions']")
    private WebElement subscriptionText;

    // ==================== CONTROLS ====================

    public void play() {
        waitForElementToBeClickable(5, playButton);
        playButton.click();
        logger.info("Play clicked");
    }

    public void pause() {
        waitForElementToBeClickable(5, pauseButton);
        pauseButton.click();
        logger.info("Pause clicked");
    }

    public void fullScreen() {
        waitForElementToBeVisible(5, fullScreenButton);
        fullScreenButton.click();
        logger.info("Entered fullscreen");
    }

    public void exitFullScreen() {
        waitForElementToBeClickable(5, exitFullScreenButton);
        exitFullScreenButton.click();
        logger.info("Exited fullscreen");
    }

    public void moveFocusToPlayer() {
        try {
            Thread.sleep(5000);
            contextClick(player);
            logger.info("Focus moved to player");
        } catch (Exception e) {
            logger.error("Error moving focus to player", e);
        }
    }

    // ==================== QUALITY ====================

    public void changeQuality(String quality) {
        qualityMenu.click();
        implicitWait(3);

        switch (quality.toLowerCase()) {
            case "1080":
                quality1080.click();
                break;
            case "720":
                quality720.click();
                break;
            case "360":
                quality360.click();
                break;
            case "auto":
                qualityAuto.click();
                break;
            default:
                logger.warn("Invalid quality: {}", quality);
                return;
        }
        logger.info("Quality changed to: {}", quality);
    }

    // ==================== CAPTIONS ====================

    public void toggleCaptions(String enableOrDisable) {
        if (!isElementDisplayed(captionsButton)) {
            logger.warn("Captions button not visible");
            return;
        }
        captionsButton.click();

        if (enableOrDisable.equalsIgnoreCase("enable")) {
            waitForElementToBeClickable(3, captionsEnglish);
            captionsEnglish.click();
            logger.info("Captions enabled");
        } else if (enableOrDisable.equalsIgnoreCase("disable")) {
            waitForElementToBeClickable(3, captionsOff);
            captionsOff.click();
            logger.info("Captions disabled");
        }
    }

    // ==================== VOLUME & SEEK ====================

    public void setVolume(double level) {
        if (level < 0.0 || level > 1.0) {
            logger.warn("Volume must be 0.0-1.0. Got: {}", level);
            return;
        }
        js.executeScript("document.getElementById('player_html5_api').volume = arguments[0];", level);
        logger.info("Volume set to: {}", level);
    }

    public void seekTo(double seconds) {
        Object durationObj = js.executeScript("return document.getElementById('player_html5_api').duration");
        double totalDuration = (durationObj instanceof Number) ? ((Number) durationObj).doubleValue() : 0;

        if (seconds <= totalDuration) {
            js.executeScript("document.getElementById('player_html5_api').currentTime = arguments[0];", seconds);
            logger.info("Seeked to: {}s", seconds);
        } else {
            logger.warn("Seek {} exceeds duration {}. Skipping.", seconds, totalDuration);
        }
    }

    // ==================== VALIDATION ====================

    public boolean isVideoElementDisplayed() {
        try {
            if (isElementVisible(5, player)) {
                logger.info("HTML5 video player displayed");
                return true;
            } else if (isElementVisible(5, shakaPlayer)) {
                logger.info("Shaka video player displayed");
                return true;
            }
            logger.warn("No video player found");
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVideoPlaying() {
        try {
            Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(180))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            Boolean isPlaying = fluentWait.until(d -> {
                JavascriptExecutor jsExec = (JavascriptExecutor) d;
                Boolean normalPlaying = (Boolean) jsExec.executeScript(
                        "var p = document.getElementById('player_html5_api'); return p && !p.paused;");
                Boolean shakaPlaying = (Boolean) jsExec.executeScript(
                        "var s = document.querySelector('video.shaka-video'); return s && !s.paused;");
                return (normalPlaying != null && normalPlaying) || (shakaPlaying != null && shakaPlaying);
            });

            if (Boolean.TRUE.equals(isPlaying)) {
                logger.info("Video is playing");
                return true;
            }
            logger.warn("Video present but not playing");
            return false;
        } catch (Exception e) {
            logger.error("Error checking playback", e);
            return false;
        }
    }

    public boolean isSubscriptionShown() {
        try {
            return subscriptionText.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void validatePlayback() {
        if (isVideoElementDisplayed()) {
            if (isVideoPlaying()) {
                logger.info("Playback validation PASSED");
            } else {
                logger.warn("Playback validation FAILED: video not playing");
            }
        } else if (isSubscriptionShown()) {
            logger.info("Redirected to subscription page");
        } else {
            logger.error("Neither video nor subscription found");
        }
    }
}
