package pagesSunShorts;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Log;
import helpers.WaitUtils;

/** HomePageSunShorts - Handles platform switching, navigation and carousel selection. */
public class HomePageSunShorts extends WaitUtils {

    private static final Logger logger = Log.getLogger(HomePageSunShorts.class);

    public HomePageSunShorts(WebDriver driver) {
        super(driver);
    }

    // ==================== PLATFORM SELECTION ====================

    @FindBy(xpath = "//img[@alt='Sun NXT']")
    private WebElement switchShorts;

    @FindBy(xpath = "//span[text()='2-Minute Stories']")
    private WebElement sunShortsSelection;

    @FindBy(xpath = "//span[text()='Movies, TV Shows & Live Channels']")
    private WebElement sunnxtSelection;

    @FindBy(xpath = "//p[text()='Choose your experience']")
    private WebElement switchConfirmationBox;

    // ==================== NAVIGATION ====================

    @FindBy(xpath = "//span[text()='Home']")
    private WebElement homeSunShorts;

    @FindBy(xpath = "//span[text()='Search']")
    private WebElement searchSunShorts;

    @FindBy(xpath = "//span[text()='Watchlist']")
    private WebElement watchlistSunShorts;

    @FindBy(tagName = "h2")
    private List<WebElement> carouselListShorts;

    @FindBy(xpath = "//img[contains(@src,'sund-images.sunnxt.com/') and @alt]")
    private List<WebElement> contentPostersShorts;

    // Content grids shared by normal, ranked, mixed-size and six-card layouts.
    // Hero-thumbnail buttons are outside these grids.
    @FindBy(xpath = "//div[contains(concat(' ',normalize-space(@class),' '),' grid ') "
            + "and contains(@class,'overflow-visible')]"
            + "[div[contains(@class,'overflow-visible')]"
            + "[.//img[contains(@src,'sund-images.sunnxt.com/') "
            + "and contains(@class,'object-cover') and normalize-space(@alt)!='']]]")
    private List<WebElement> contentLayoutsShorts;

    // ==================== PLATFORM ACTIONS ====================

    public void shortsNavigation() {
        waitForElementToBeClickable(10, switchShorts);
        switchShorts.click();
        waitForElementToBeVisible(5, switchConfirmationBox);
        waitForElementToBeClickable(5, sunShortsSelection);
        sunShortsSelection.click();
        waitForElementToBeVisible(10, homeSunShorts);
        logger.info("Sun Shorts experience selected");
    }

    public void sunnxtNavigation() {
        waitForElementToBeClickable(10, switchShorts);
        switchShorts.click();
        waitForElementToBeVisible(5, switchConfirmationBox);
        waitForElementToBeClickable(5, sunnxtSelection);
        sunnxtSelection.click();
        waitForElementToBeVisible(10, homeSunShorts);
        logger.info("Sun NXT experience selected");
    }

    // ==================== NAVIGATION ACTIONS ====================

    public void navigateToHome() {
        waitForElementToBeClickable(10, homeSunShorts);
        homeSunShorts.click();
        logger.info("Navigated to Sun Shorts Home");
    }

    public void navigateToSearch() {
        waitForElementToBeClickable(10, searchSunShorts);
        searchSunShorts.click();
        logger.info("Navigated to Sun Shorts Search");
    }

    public void navigateToWatchlist() {
        waitForElementToBeClickable(10, watchlistSunShorts);
        watchlistSunShorts.click();
        logger.info("Navigated to Sun Shorts Watchlist");
    }

    // ==================== CAROUSEL ACTIONS ====================

    public int getCarouselCount() {
        waitForCarousels();
        return carouselListShorts.size();
    }

    /** Uses a zero-based index. Pass -1 to select a random carousel heading. */
    public void selectCarousel(int index) {
        waitForCarousels();
        int count = carouselListShorts.size();
        if (index == -1) {
            index = new Random().nextInt(count);
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Carousel index is outside the available range");
        }
        WebElement carousel = carouselListShorts.get(index);
        scrollIntoView(carousel);
        waitForElementToBeClickable(10, carousel);
        carousel.click();
        logger.info("Sun Shorts carousel selected: {}", index);
    }

    /**
     * Picks a content grid and then a card inside it. Indices are zero-based;
     * -1 selects randomly. Cards open the player directly, without clicking h2.
     */
    public String contentPlayBackCheck(int carouselIndex, int contentIndex) {
        if (carouselIndex != 0) {
            scrollPageWithLoad();
        }
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> !contentLayoutsShorts.isEmpty());

        int layoutCount = contentLayoutsShorts.size();
        if (carouselIndex == -1) {
            carouselIndex = new Random().nextInt(layoutCount);
        }
        if (carouselIndex < 0 || carouselIndex >= layoutCount) {
            throw new IllegalArgumentException("Shorts content grid index is outside the available range");
        }

        WebElement layout = contentLayoutsShorts.get(carouselIndex);
        scrollIntoView(layout);
        List<WebElement> cards = layout.findElements(By.xpath(
                "./div[contains(@class,'overflow-visible')]"
                + "[.//img[contains(@src,'sund-images.sunnxt.com/') "
                + "and contains(@class,'object-cover') and normalize-space(@alt)!='']]"));
        if (cards.isEmpty()) {
            throw new IllegalStateException("No content cards found in the selected Shorts grid");
        }
        if (contentIndex == -1) {
            contentIndex = new Random().nextInt(cards.size());
        }
        if (contentIndex < 0 || contentIndex >= cards.size()) {
            throw new IllegalArgumentException("Shorts content index is outside the available range");
        }

        WebElement card = cards.get(contentIndex);
        String title = card.findElement(By.xpath(
                ".//img[contains(@src,'sund-images.sunnxt.com/') "
                + "and contains(@class,'object-cover') and normalize-space(@alt)!='']"))
                .getAttribute("alt");
        scrollIntoView(card);
        List<WebElement> links = card.findElements(By.xpath("./a"));
        WebElement target = card;
        if (!links.isEmpty()) {
            target = links.get(0);
        }
        waitForElementToBeClickable(10, target);
        target.click();
        logger.info("Selected Sun Shorts grid {}, card {}: {}", carouselIndex, contentIndex, title);
        return title;
    }

    private void waitForCarousels() {
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> !carouselListShorts.isEmpty());
    }

    // ==================== VALIDATION CHECKS ====================

    /** Matches the selected poster title exactly, including language suffixes. */
    public boolean isContentDisplayed(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Content title must not be blank");
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        for (WebElement poster : contentPostersShorts) {
                            if (title.equals(poster.getAttribute("alt")) && poster.isDisplayed()) {
                                scrollIntoView(poster);
                                return true;
                            }
                        }
                        return false;
                    });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Checks the supplied Home navigation marker; it is not a unique platform marker. */
    public boolean isSunShortsPageLoaded() {
        return isElementVisible(10, homeSunShorts);
    }

    public boolean isSwitchConfirmationDisplayed() {
        return isElementVisible(5, switchConfirmationBox);
    }
}
