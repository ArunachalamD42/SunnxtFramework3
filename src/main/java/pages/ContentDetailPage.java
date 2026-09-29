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
        org.openqa.selenium.By titleLocator = org.openqa.selenium.By.xpath("//span[@class='text-2xl']");
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                waitForElementToBeVisible(5, titleLocator);
                String name = driver.findElement(titleLocator).getText();
                logger.info("Targeted Content: {}", name);
                return name;
            } catch (NoSuchElementException | org.openqa.selenium.StaleElementReferenceException e) {
                logger.warn("Content title not ready (attempt {}/3), retrying...", attempt);
            } catch (Exception e) {
                logger.warn("Content title lookup failed (attempt {}/3): {}", attempt, e.getMessage());
            }
        }
        logger.error("Failed to get content title after retries");
        return "";
    }
}
