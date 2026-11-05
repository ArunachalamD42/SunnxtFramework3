package pagesSunnxt;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * ContentDetailPageLiveTV - Live TV channel detail/info page shown before live playback.
 *
 * <p>This page is the Live TV counterpart to {@link ContentDetailPage} (which is
 * VOD-only). It owns the Live TV specific "Play" button and channel metadata so
 * that VOD and Live TV playback flows stay decoupled.</p>
 */
public class ContentDetailPageLiveTV extends WaitUtils {

    private static final Logger logger = Log.getLogger(ContentDetailPageLiveTV.class);

    public ContentDetailPageLiveTV(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//button[contains(normalize-space(.),'Watch Now')]")
    private WebElement playButtonLiveTV;

    @FindBy(xpath = "//p[@class='text-lg font-semibold whitespace-nowrap']")
    private WebElement channelTitle;

    @FindBy(xpath = "//h2[text()='Subscriptions']")
    private WebElement subscriptionPrompt;

    @FindBy(xpath = "//span[text()='Subscribe to Watch']")
    private WebElement subscribeToWatchButton;

    public boolean isSubscribeToWatchDisplayed() {
        return isElementDisplayed(subscribeToWatchButton);
    }

    /**
     * Clicks the Live TV "Play" button on the channel detail page.
     */
    public void clickPlayButton() {
        logger.info("Attempting to click Live TV Watch Now button...");
        try {
            // Wait for the channel detail page to render the Watch Now button
            waitForElementToBeClickable(15, playButtonLiveTV);
            playButtonLiveTV.click();
            logger.info("Live TV Watch Now button clicked");
        } catch (Exception e) {
            logger.warn("No Live TV Watch Now button found on channel detail page", e);
        }
    }

    /**
     * Returns the currently displayed live channel name.
     */
    public String getChannelName() {
        try {
            String name = channelTitle.getText();
            logger.info("Targeted Live Channel: {}", name);
            return name;
        } catch (NoSuchElementException | org.openqa.selenium.StaleElementReferenceException e) {
            logger.error("Failed to get live channel title", e);
            return "";
        }
    }

    /**
     * Checks if the subscription prompt is shown instead of the Play button
     * (i.e. the plan is not entitled to this live channel).
     */
    public boolean isSubscriptionPromptShown() {
        try {
            return subscriptionPrompt.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
