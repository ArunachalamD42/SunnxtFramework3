package pagesSunnxt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import config.Log;
import helpers.WaitUtils;

/**
 * PlayerEntitlementPage - Inspects the player for entitlement validation.
 *
 * <p>Distinct from {@link PlayerControlsPage}: that page drives playback controls
 * (play/pause/fullscreen/captions) and validates basic playback for the content
 * playback tests. This page instead inspects what a plan is entitled to -
 * available video qualities and audio tracks - plus paywall detection, for the
 * entitlement validation suite.</p>
 */
public class PlayerEntitlementPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(PlayerEntitlementPage.class);

    public PlayerEntitlementPage(WebDriver driver) {
        super(driver);
    }

    // ==================== PLAYER ELEMENTS ====================

    @FindBy(id = "player_html5_api")
    private WebElement player;

    @FindBy(xpath = "//video[@class='shaka-video']")
    private WebElement shakaPlayer;

    @FindBy(xpath = "//h2[text()='Subscriptions']")
    private WebElement subscriptionPrompt;

    // ==================== QUALITY MENU ====================

    @FindBy(xpath = "(//div[@class='vjs-menu'])[2]")
    private WebElement qualityMenuButton;

    @FindBy(xpath = "//li[contains(@class,'vjs-menu-item')]")
    private List<WebElement> qualityMenuItems;

    // ==================== AUDIO MENU ====================

    @FindBy(xpath = "//button[@title='Audio Track']")
    private WebElement audioMenuButton;

    @FindBy(xpath = "//div[contains(@class,'audio')]//li")
    private List<WebElement> audioMenuItems;

    // ==================== PLAYBACK VALIDATION ====================

    /**
     * Waits for video playback to start using FluentWait (180s timeout, 500ms polling).
     */
    public boolean waitForPlaybackStart(int timeoutSeconds) {
        try {
            Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
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
                logger.info("Video playback started");
                return true;
            }
            logger.warn("Video present but not playing");
            return false;
        } catch (Exception e) {
            logger.error("Timeout waiting for playback to start", e);
            return false;
        }
    }

    public boolean isVideoPlaying() {
        return waitForPlaybackStart(180);
    }

    /**
     * Checks if subscription prompt/paywall is shown instead of content.
     */
    public boolean isSubscriptionPromptShown() {
        try {
            return subscriptionPrompt.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== QUALITY MENU INSPECTION ====================

    /**
     * Opens the quality/settings menu on the player.
     */
    public void openQualityMenu() {
        try {
            waitForElementToBeClickable(5, qualityMenuButton);
            qualityMenuButton.click();
            implicitWait(2);
            logger.info("Quality menu opened");
        } catch (Exception e) {
            logger.warn("Could not open quality menu", e);
        }
    }

    /**
     * Returns list of available quality options (e.g., ["4K UHD", "1080p", "720p", "360p", "Auto"]).
     */
    public List<String> getAvailableQualities() {
        List<String> qualities = new ArrayList<>();
        try {
            openQualityMenu();
            for (WebElement item : qualityMenuItems) {
                String text = item.getText().trim();
                if (!text.isEmpty()) {
                    qualities.add(text);
                }
            }
            logger.info("Available qualities: {}", qualities);
        } catch (Exception e) {
            logger.warn("Could not read quality menu items", e);
        }
        return qualities;
    }

    /**
     * Returns the highest resolution available in the quality menu.
     */
    public String getMaxQuality() {
        List<String> qualities = getAvailableQualities();
        if (qualities.isEmpty()) {
            logger.warn("No quality options found");
            return "";
        }

        // Priority order: 4K > 1080p > 720p > 360p
        for (String q : qualities) {
            if (q.contains("4K") || q.contains("2160")) return "4K UHD";
        }
        for (String q : qualities) {
            if (q.contains("1080")) return "Full HD";
        }
        for (String q : qualities) {
            if (q.contains("720")) return "720p";
        }
        return qualities.get(0);
    }

    // ==================== AUDIO MENU INSPECTION ====================

    /**
     * Opens the audio track selector menu.
     */
    public void openAudioMenu() {
        try {
            waitForElementToBeClickable(5, audioMenuButton);
            audioMenuButton.click();
            implicitWait(2);
            logger.info("Audio menu opened");
        } catch (Exception e) {
            logger.warn("Could not open audio menu", e);
        }
    }

    /**
     * Returns list of available audio tracks (e.g., ["Dolby Atmos", "DD+5.1", "Stereo 2.0"]).
     */
    public List<String> getAvailableAudioTracks() {
        List<String> tracks = new ArrayList<>();
        try {
            openAudioMenu();
            for (WebElement item : audioMenuItems) {
                String text = item.getText().trim();
                if (!text.isEmpty()) {
                    tracks.add(text);
                }
            }
            logger.info("Available audio tracks: {}", tracks);
        } catch (Exception e) {
            logger.warn("Could not read audio menu items", e);
        }
        return tracks;
    }

    /**
     * Checks if a specific audio track is available.
     */
    public boolean hasAudioTrack(String trackName) {
        List<String> tracks = getAvailableAudioTracks();
        for (String track : tracks) {
            if (track.toLowerCase().contains(trackName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves focus to player (hover to show controls).
     */
    public void moveFocusToPlayer() {
        try {
            Thread.sleep(5000);
            contextClick(player);
            logger.info("Focus moved to player");
        } catch (Exception e) {
            logger.error("Error moving focus to player", e);
        }
    }
}
