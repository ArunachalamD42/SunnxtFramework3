package pages;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * ContentDetailPage - Content detail/info page before playback.
 */
public class ContentDetailPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(ContentDetailPage.class);

    public ContentDetailPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//span[text()='Watch Now']")
    private WebElement playButton;

    @FindBy (xpath="//img[@alt='watchlist']")
private WebElement watchlistButton;

@FindBy (xpath="//img[@alt='share']")
private WebElement shareButton;

@FindBy(xpath="//span[text()='Subscribe to Watch']")
private WebElement subscribeToWatchButton;


    @FindBy(xpath = "//span[@class='text-2xl']")
    private WebElement contentTitle;

    /**
     * Clicks the VOD "Watch Now" play button on the content detail page.
     * Live TV playback is handled separately by {@link LiveTVDetailPage}.
     */
    public void clickPlayButton() {
        logger.info("Attempting to click Watch Now button...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (isElementDisplayed(playButton)) {
            playButton.click();
            logger.info("Watch Now button clicked");
        } else {
            logger.warn("No Watch Now button found on VOD detail page");
        }
    }

    public String getContentName() {
        try {
            String name = contentTitle.getText();
            logger.info("Targeted Content: {}", name);
            return name;
        } catch (NoSuchElementException | org.openqa.selenium.StaleElementReferenceException e) {
            logger.error("Failed to get content title", e);
            return "";
        }
    }
}
