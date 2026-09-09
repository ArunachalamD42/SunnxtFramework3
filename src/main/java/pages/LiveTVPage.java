package pages;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * LiveTVPage - Handles Live TV section navigation and access validation.
 */
public class LiveTVPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(LiveTVPage.class);

    public LiveTVPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//span[text()='Live TV']")
    private WebElement liveTvTab;

    @FindBy(xpath = "//div[contains(@class,'channel')] | //div[contains(@class,'live')]//img")
    private List<WebElement> channelCards;

    @FindBy(xpath = "//div[text()='Play']")
    private WebElement playButton;

    @FindBy(xpath = "//video[@class='shaka-video']")
    private WebElement liveTVPlayer;

    @FindBy(xpath = "//h2[text()='Subscriptions']")
    private WebElement subscriptionPrompt;

    // ==================== NAVIGATION ====================

    /**
     * Checks if Live TV tab is visible in the top navigation.
     */
    public boolean isLiveTVSectionVisible() {
        try {
            return liveTvTab.isDisplayed();
        } catch (Exception e) {
            logger.info("Live TV tab not visible in navigation");
            return false;
        }
    }

    /**
     * Navigates to the Live TV section.
     */
    public void navigateToLiveTV() {
        try {
            waitForElementToBeClickable(10, liveTvTab);
            liveTvTab.click();
            logger.info("Navigated to Live TV section");
        } catch (Exception e) {
            logger.error("Failed to navigate to Live TV", e);
        }
    }

    /**
     * Selects the first available live channel.
     */
    public void selectFirstChannel() {
        try {
            implicitWait(5);
            if (!channelCards.isEmpty()) {
                WebElement firstChannel = channelCards.get(0);
                scrollIntoView(firstChannel);
                firstChannel.click();
                logger.info("First live channel selected");
            } else {
                logger.warn("No live channels found on the page");
            }
        } catch (Exception e) {
            logger.error("Error selecting first channel", e);
        }
    }

    /**
     * Checks if live channel is playing (shaka player active).
     */
    public boolean isLiveChannelPlaying() {
        try {
            waitForElementToBeVisible(30, liveTVPlayer);
            Boolean isPlaying = (Boolean) js.executeScript(
                    "var s = document.querySelector('video.shaka-video'); return s && !s.paused;");
            if (Boolean.TRUE.equals(isPlaying)) {
                logger.info("Live TV channel is playing");
                return true;
            }
            logger.warn("Live TV player found but not playing");
            return false;
        } catch (Exception e) {
            logger.warn("Live TV player not found or not playing", e);
            return false;
        }
    }

    /**
     * Checks if access denied / subscription prompt is shown.
     */
    public boolean isAccessDeniedShown() {
        try {
            return subscriptionPrompt.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if subscription prompt is shown (alias for clarity in tests).
     */
    public boolean isSubscriptionPromptShown() {
        return isAccessDeniedShown();
    }
}
