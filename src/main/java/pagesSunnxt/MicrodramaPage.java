package pagesSunnxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * MicrodramaPage - Handles the Microdrama section of SunNXT.
 * 
 * Microdrama is a separate module/application within SunNXT — not a top menu tab.
 * It has its own content listing, navigation, and player experience.
 * 
 * NOTE: Locators below are placeholder patterns based on common SunNXT class naming.
 * Update them after inspecting the actual Microdrama DOM elements.
 */
public class MicrodramaPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(MicrodramaPage.class);

    public MicrodramaPage(WebDriver driver) {
        super(driver);
    }

    // ==================== ENTRY POINT ====================
    // Microdrama entry could be a banner, card, or section link on home

    @FindBy(xpath = "//div[contains(@class,'microdrama')]//a | //a[contains(@href,'microdrama')]")
    private WebElement microdramaEntryPoint;

    @FindBy(xpath = "//*[contains(text(),'Microdrama') or contains(text(),'microdrama')]")
    private WebElement microdramaHeader;

    // ==================== CONTENT LISTING ====================

    @FindBy(xpath = "//div[contains(@class,'microdrama')]//div[contains(@class,'card')] | //div[contains(@class,'episode')]")
    private List<WebElement> contentCards;

    @FindBy(xpath = "//div[contains(@class,'microdrama')]//h1 | //div[contains(@class,'microdrama')]//h2")
    private List<WebElement> sectionHeaders;

    // ==================== EPISODE DETAILS ====================

    @FindBy(xpath = "//div[contains(@class,'episode-title')] | //span[contains(@class,'title')]")
    private WebElement episodeTitle;

    @FindBy(xpath = "//button[contains(text(),'Play')] | //div[contains(text(),'Play')]")
    private WebElement playButton;

    @FindBy(xpath = "//button[contains(text(),'Next')] | //div[contains(text(),'Next Episode')]")
    private WebElement nextEpisodeButton;

    @FindBy(xpath = "//button[contains(text(),'Previous')] | //div[contains(text(),'Previous')]")
    private WebElement previousEpisodeButton;

    // ==================== NAVIGATION ====================

    /**
     * Navigates to the Microdrama section from the main SunNXT app.
     */
    public void navigateToMicrodrama() {
        try {
            waitForElementToBeClickable(10, microdramaEntryPoint);
            microdramaEntryPoint.click();
            logger.info("Navigated to Microdrama section");
        } catch (Exception e) {
            logger.error("Failed to navigate to Microdrama. Entry point not found.", e);
        }
    }

    /**
     * Verifies Microdrama page/section has loaded.
     */
    public boolean isMicrodramaPageLoaded() {
        try {
            waitForElementToBeVisible(10, microdramaHeader);
            logger.info("Microdrama page loaded");
            return true;
        } catch (Exception e) {
            logger.warn("Microdrama page did not load");
            return false;
        }
    }

    // ==================== CONTENT BROWSING ====================

    public int getContentCount() {
        int count = contentCards.size();
        logger.info("Microdrama content cards: {}", count);
        return count;
    }

    public void selectContent(int index) {
        if (index >= 0 && index < contentCards.size()) {
            WebElement card = contentCards.get(index);
            scrollIntoView(card);
            card.click();
            logger.info("Selected Microdrama content at index: {}", index);
        } else {
            logger.warn("Invalid index: {}. Available: {}", index, contentCards.size());
        }
    }

    public void selectRandomContent() {
        if (contentCards.isEmpty()) {
            logger.warn("No Microdrama content found");
            return;
        }
        int randomIndex = new Random().nextInt(contentCards.size());
        selectContent(randomIndex);
    }

    public void scrollToLoadContent() {
        logger.info("Scrolling to load Microdrama content...");
        for (int i = 0; i < 5; i++) {
            scrollByPixel();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== EPISODE NAVIGATION ====================

    public void clickPlay() {
        try {
            waitForElementToBeClickable(10, playButton);
            playButton.click();
            logger.info("Microdrama play button clicked");
        } catch (Exception e) {
            logger.error("Play button not found or not clickable", e);
        }
    }

    public void goToNextEpisode() {
        try {
            waitForElementToBeClickable(5, nextEpisodeButton);
            nextEpisodeButton.click();
            logger.info("Navigated to next episode");
        } catch (Exception e) {
            logger.warn("Next episode button not available", e);
        }
    }

    public void goToPreviousEpisode() {
        try {
            waitForElementToBeClickable(5, previousEpisodeButton);
            previousEpisodeButton.click();
            logger.info("Navigated to previous episode");
        } catch (Exception e) {
            logger.warn("Previous episode button not available", e);
        }
    }

    public String getEpisodeTitle() {
        try {
            String title = episodeTitle.getText();
            logger.info("Current episode: {}", title);
            return title;
        } catch (Exception e) {
            logger.error("Could not get episode title", e);
            return "";
        }
    }

    public List<String> getSectionNames() {
        List<String> names = new ArrayList<>();
        for (WebElement section : sectionHeaders) {
            names.add(section.getText());
        }
        logger.info("Microdrama sections: {}", names);
        return names;
    }
}
