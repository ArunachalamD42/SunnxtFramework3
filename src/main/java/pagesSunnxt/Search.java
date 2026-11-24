package pagesSunnxt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.ConfigReader;
import config.Log;
import helpers.WaitUtils;

public class Search extends WaitUtils {

    private static final Logger logger = Log.getLogger(HomePage.class);

	public Search(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

@FindBy (xpath = "//img[@alt='search']")
private WebElement searchIconAtHomePage;

@FindBy (xpath = "//input[@placeholder='Search Movies, Shows, Actors or channels']")
private WebElement searchBarInput;

@FindBy (xpath = "//h2[normalize-space()='Trending Search']")
private WebElement trendingSearchHeader;

@FindBy (xpath = "//div[contains(@class,'trending_search_content')]//div[contains(@class,'trending_images')]")
private List<WebElement> trendingSearchItems;

@FindBy (xpath = "//button[@class='search_content-module__3X0YkG__background_img']")
private WebElement searchElement;

@FindBy(xpath = "//h1")
private List<WebElement> searchTitleHeader;

@FindBy (xpath = "//button[normalize-space()='Languages']")
private WebElement filterLanguage;

@FindBy(xpath = "//h4[normalize-space()='No Results Found']")
private WebElement noResultFound;

// ==================== OPEN SEARCH PAGE ====================

public void clickSearchIcon() {
    waitForElementToBeClickable(30, searchIconAtHomePage);
    searchIconAtHomePage.click();
    logger.info("Clicked the search icon");
}

public void clickSearchMenu() {
    waitForElementToBeClickable(30, searchElement);
    searchElement.click();
    logger.info("Clicked the search menu");
}

/** True when the search bar is visible, which means we are on the search page. */
public boolean isSearchPageDisplayed() {
    return isElementVisible(15, searchBarInput);
}

public boolean isNoResultFoundDisplayed() {
    return isElementVisible(15, noResultFound);
}

/** Clicks the search icon and returns true if the search page opened. */
public boolean openSearchPage() {
    clickSearchIcon();
    return isSearchPageDisplayed();
}

// ==================== SEARCH BAR ====================

public void clearSearchBar() {
    waitForElementToBeClickable(10, searchBarInput);
    searchBarInput.click();
    searchBarInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
}

public void enterSearchText(String text) {
    clearSearchBar();
    searchBarInput.sendKeys(text);
    logger.info("Entered search text: {}", text);
}

public String getSearchBarText() {
    return searchBarInput.getAttribute("value");
}

/** Types each input and returns the inputs that were NOT shown correctly in the search bar. */
public List<String> getInputsNotDisplayedInSearchBar(List<String> inputs) {
    List<String> notDisplayed = new ArrayList<>();
    for (int i = 0; i < inputs.size(); i++) {
        String input = inputs.get(i);
        enterSearchText(input);
        String shown = getSearchBarText();
        logger.info("Typed '{}', search bar shows '{}'", input, shown);
        if (!input.equals(shown)) {
            notDisplayed.add(input);
        }
    }
    return notDisplayed;
}

// ==================== SEARCH RESULTS ====================

/** Text of every result carousel header (searchTitleHeader) that is on the page right now. */
public List<String> getResultHeaders() {
    List<String> headers = new ArrayList<>();
    try {
        for (WebElement header : searchTitleHeader) {
            String text = header.getText().trim();
            if (!text.isEmpty()) {
                headers.add(text);
            }
        }
    } catch (StaleElementReferenceException e) {
        logger.info("Results were refreshing, will read the headers again");
    }
    return headers;
}

/** Checks every 0.5 second and returns the headers as soon as at least one is shown. */
public List<String> waitForResultHeaders(int seconds) {
    List<String> headers = new ArrayList<>();
    for (int check = 1; check <= seconds * 2; check++) {
        headers = getResultHeaders();
        if (!headers.isEmpty()) {
            return headers;
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
    return headers;
}

/** Scrolls the whole results page (WaitUtils.scrollPageWithLoad) so every carousel loads, then returns all headers. */
public List<String> getAllResultHeaders() {
	
    waitForResultHeaders(10);
    scrollPageWithLoad(searchTitleHeader);
    List<String> headers = getResultHeaders();
    scrollToTop();
    logger.info("All result carousels: {}", headers);
    return headers;
}

/** Clears the bar, types the text, waits for the results and returns the carousel headers. */
public List<String> searchAndGetHeaders(String text) {
    clearSearchBar();
    isElementVisible(3, trendingSearchHeader);   // page goes back to the trending view
    enterSearchText(text);
    // results replace the trending view
	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.invisibilityOf(trendingSearchHeader));
    List<String> headers = waitForResultHeaders(10);
    logger.info("Search '{}' -> {} carousel(s): {}", text, headers.size(), headers);
    return headers;
}

/** Searches every input and returns the inputs that showed NO result carousel. */
public List<String> getInputsWithNoResults(List<String> inputs) {
    List<String> noResults = new ArrayList<>();
    implicitWait(0);   // faster checks while looping
    try {
        for (int i = 0; i < inputs.size(); i++) {
            if (searchAndGetHeaders(inputs.get(i)).isEmpty()) {
                noResults.add(inputs.get(i));
            }
        }
    } finally {
        implicitWait(ConfigReader.getInt("implicit.wait", 10));
    }
    return noResults;
}

/**
 * For inputs like @ or A* : searches each one and returns the inputs where the typed text
 * was not kept in the search bar or the search page was no longer displayed.
 */
public List<String> getUnstableInputs(List<String> inputs) {
    List<String> unstable = new ArrayList<>();
    implicitWait(0);
    try {
        for (int i = 0; i < inputs.size(); i++) {
            String input = inputs.get(i);
            searchAndGetHeaders(input);
            boolean textKept = input.equals(getSearchBarText());
            boolean pageShown = isSearchPageDisplayed();
            if (!textKept || !pageShown) {
                logger.info("Input '{}' -> text kept = {}, search page shown = {}", input, textKept, pageShown);
                unstable.add(input);
            }
        }
    } finally {
        implicitWait(ConfigReader.getInt("implicit.wait", 10));
    }
    return unstable;
}

// ==================== TRENDING SEARCH / FILTER ====================

public boolean isTrendingSearchHeaderDisplayed() {
    return isElementVisible(10, trendingSearchHeader);
}

public int getTrendingSearchItemsCount() {
    new WebDriverWait(driver, Duration.ofSeconds(60)).until(d -> !trendingSearchItems.isEmpty());
    return trendingSearchItems.size();
}

public boolean isLanguageFilterDisplayed() {
	
    return isElementVisible(10, filterLanguage);
}

}
