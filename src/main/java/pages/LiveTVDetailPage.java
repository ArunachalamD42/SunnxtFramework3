package pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * LiveTVDetailPage - Live TV channel detail/info page shown before live playback.
 *
 * <p>This page is the Live TV counterpart to {@link ContentDetailPage} (which is
 * VOD-only). It owns the Live TV specific "Play" button and channel metadata so
 * that VOD and Live TV playback flows stay decoupled.</p>
 */
public class LiveTVDetailPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(LiveTVDetailPage.class);

    public LiveTVDetailPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//div[text()='Play']")
    private WebElement playButtonLiveTV;

    @FindBy(xpath = "//span[@class='text-2xl']")
    private WebElement channelTitle;

    @FindBy(xpath = "//h2[text()='Subscriptions']")
    private WebElement subscriptionPrompt;

    /**
     * Clicks the Live TV "Play" button on the channel detail page.
     */
    public void clickPlayButton() {
        logger.info("Attempting to click Live TV play button...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (isElementDisplayed(playButtonLiveTV)) {
            playButtonLiveTV.click();
            logger.info("Live TV play button clicked");
        } else {
            logger.warn("No Live TV play button found on channel detail page");
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
