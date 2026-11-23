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
 * Keeps Live TV channel metadata and uses the shared Watch Now action.
 */
public class ContentDetailPageLiveTV extends WaitUtils {

    private static final Logger logger = Log.getLogger(ContentDetailPageLiveTV.class);

    public ContentDetailPageLiveTV(WebDriver driver) {
        super(driver);
    }

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
        new ContentDetailPage(driver).clickPlayButton();
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
