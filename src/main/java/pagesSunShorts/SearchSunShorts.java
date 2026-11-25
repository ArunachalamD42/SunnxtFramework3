package pagesSunShorts;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Log;
import helpers.WaitUtils;

public class SearchSunShorts extends WaitUtils {
	  private static final Logger logger = Log.getLogger(SearchSunShorts.class);

    public SearchSunShorts(WebDriver driver) {
        super(driver);
    }

    // ==================== SEARCH BAR ====================

    @FindBy(xpath = "//input[@placeholder='Search your favourite content']")
    private WebElement searchShorts;

    // ==================== TRENDING ====================

    @FindBy(xpath = "//h2[normalize-space()='Trending on Sun NXT Shorts']")
    private WebElement trendingOnSunNXTShorts;

    // Trending content posters
    @FindBy(xpath = "//div[contains(@class,'grid')]//img[@alt]")
    private List<WebElement> contentPosters;

    // ==================== TOP AIRING ====================

    @FindBy(xpath = "//h2[normalize-space()='Top Airing']")
    private WebElement topAiring;

    @FindBy(xpath = "//button[normalize-space()='Today']")
    private WebElement todayButton;

    @FindBy(xpath = "//button[normalize-space()='Week']")
    private WebElement weekButton;

    @FindBy(xpath = "//button[normalize-space()='Month']")
    private WebElement monthButton;

    @FindBy(xpath = "//div[contains(@class,'grid')]//img[@alt]")
    private List<WebElement> contentPostersTopAiring;

    // ==================== SEARCH RESULTS ====================

    @FindBy(xpath = "//h2[normalize-space()='Top Result']")
    private WebElement topResult;

    @FindBy(xpath = "//h4[normalize-space()='No results found']")
    private WebElement noResultsFoundShorts;
    
    @FindBy(xpath = "//div[contains(@class,'grid-cols-2')]//img[@alt]")
    private List<WebElement> searchResultcontentPosters;

    // ==================== SEARCH PAGE ====================

    public boolean isSearchPageDisplayed() {
        return isElementVisible(15, searchShorts);
    }

    // ==================== SEARCH BAR METHODS ====================

    public void clearSearchBar() {
        waitForElementToBeClickable(10, searchShorts);

        searchShorts.click();

        searchShorts.sendKeys(
                Keys.chord(Keys.CONTROL, "a"),
                Keys.DELETE
        );
    }

    public void enterSearchText(String text) {
        clearSearchBar();
        searchShorts.sendKeys(text);
        logger.info("Entered search text: {}", text);
    }

    public String getSearchBarText() {
        return searchShorts.getAttribute("value");
    }

    // ==================== TRENDING METHODS ====================

    public boolean isTrendingOnShortsDisplayed() {
        return isElementVisible(10, trendingOnSunNXTShorts);
    }

    public int getTrendingContentCount() {
    	waitForElementToBeVisible(60, monthButton);
    	new WebDriverWait(driver, Duration.ofSeconds(60)).until(d -> !contentPosters.isEmpty());
        return contentPosters.size();
    }

    // ==================== TOP AIRING METHODS ====================

    public boolean isTopAiringDisplayed() {
        return isElementVisible(10, topAiring);
    }

    public int getTopAiringContentCount() {
        return contentPostersTopAiring.size();
    }

    public void clickToday() {
        waitForElementToBeClickable(10, todayButton);
        todayButton.click();
    }

    public void clickWeek() {
        waitForElementToBeClickable(10, weekButton);
        weekButton.click();
    }

    public void clickMonth() {
        waitForElementToBeClickable(10, monthButton);
        monthButton.click();
    }
    
    // ==================== SEARCH RESULTS ====================
    public boolean isSearchHeaderDisplayedOnShorts() {
        return isElementVisible(10, topResult);
    }

    public int getSearchContentCount() {
    	//waitForElementToBeVisible(60, topResult);
    	new WebDriverWait(driver, Duration.ofSeconds(60)).until(d -> !searchResultcontentPosters.isEmpty());
        return searchResultcontentPosters.size();
    }
    
    
    // ==================== NO RESULTS ====================

    public boolean isNoResultsFoundDisplayed() {
        return isElementVisible(10, noResultsFoundShorts);
    }
}