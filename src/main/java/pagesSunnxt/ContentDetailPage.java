package pagesSunnxt;

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

    @FindBy(xpath = "//span[contains(normalize-space(.),'Watch Now')] | //button[contains(normalize-space(.),'Watch Now')]")
    private WebElement playButton;

    @FindBy (xpath="//img[@alt='watchlist']")
private WebElement watchlistButton;

@FindBy (xpath="//img[@alt='share']")
private WebElement shareButton;

@FindBy(xpath="//button[contains(normalize-space(.),'Subscribe to Watch')] | //span[normalize-space(.)='Subscribe to Watch']")
private WebElement subscribeToWatchButton;


    @FindBy(xpath = "//span[@class='text-2xl']")
    private WebElement contentTitle;

    public boolean isSubscribeToWatchDisplayed() {
        return isElementDisplayed(subscribeToWatchButton);
    }

    public String getSubscribeToWatchText() {
        waitForElementToBeVisible(30, subscribeToWatchButton);
        String subscribeToWatchText = subscribeToWatchButton.getText().trim();
        logger.info("Subscribe CTA displayed: {}", subscribeToWatchText);
        return subscribeToWatchText;
    }

    public void clickSubscribeToWatchButton() {
        waitForElementToBeClickable(30, subscribeToWatchButton);
        subscribeToWatchButton.click();
        logger.info("Subscribe to Watch button clicked");
    }

    public void clickWatchlistButton() {
        waitForElementToBeClickable(30, watchlistButton);
        watchlistButton.click();
        logger.info("Watchlist button clicked");
    }

    /**
     * Clicks Watch Now for Movies, TV Shows, and Live TV.
     */
    public void clickPlayButton() {
        logger.info("Attempting to click Watch Now button...");
        waitForElementToBeClickable(30, playButton);
        playButton.click();
        logger.info("Watch Now button clicked");
    }
    
    public boolean isWatchNowEnabled() {
    	waitForElementToBeClickable(30, playButton);
    	return playButton.isEnabled();
    }

    public String getWatchNowText() {
        waitForElementToBeVisible(30, playButton);
        String watchNowText = playButton.getText().trim();
        logger.info("Watch Now CTA displayed: {}", watchNowText);
        return watchNowText;
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
