package pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * HomePage - SunNXT home page interactions.
 * Handles navigation, profile menu, and carousel content selection.
 */
public class HomePage extends WaitUtils {

    private static final Logger logger = Log.getLogger(HomePage.class);

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // ==================== PROFILE MENU ====================

    @FindBy(xpath = "//div[@class='relative group inline-block pointer-events-auto']")
    private WebElement profileIcon;

    @FindBy(xpath = "//span[text()='Log In']")
    private WebElement loginButton;

    @FindBy(xpath = "//span[text()='Logout']")
    private WebElement logoutButton;

    @FindBy(xpath = "//span[text()='Subscriptions']")
    private WebElement subscriptionsMenu;

    @FindBy(xpath = "//span[text()='My Account']")
    private WebElement myAccountMenu;

    @FindBy(xpath = "//span[text()='Watchlist']")
    private WebElement watchlistMenu;

    // ==================== TOP NAVIGATION ====================

    @FindBy(xpath = "//span[text()='Home']")
    private WebElement homeTab;

    @FindBy(xpath = "//span[text()='Movies']")
    private WebElement moviesTab;

    @FindBy(xpath = "//span[text()='Live TV']")
    private WebElement liveTvTab;

    @FindBy(xpath = "//span[text()='TV Shows']")
    private WebElement tvShowsTab;

    @FindBy(xpath = "//span[text()='Music Videos']")
    private WebElement musicVideosTab;

    @FindBy(xpath = "//span[text()='Comedy']")
    private WebElement comedyTab;

    @FindBy(xpath = "//span[text()='Shorts']")
    private WebElement shortsTab;

    // ==================== CAROUSEL ====================

    @FindBy(tagName = "h2")
    private List<WebElement> carouselList;

    // ==================== PROFILE ACTIONS ====================

    public void hoverProfileIcon() {
        int attempts = 0;
        while (attempts < 5) {
            try {
                waitForElementToBeClickable(5, profileIcon);
                moveElement(profileIcon);
                break;
            } catch (StaleElementReferenceException e) {
                logger.warn("Stale element. Retrying hover...");
            }
            attempts++;
        }
    }

    public void navigateToLogin() {
        hoverProfileIcon();
        waitForElementToBeClickable(5, loginButton);
        loginButton.click();
        logger.info("Navigated to Login page");
    }

    public void logout() {
        hoverProfileIcon();
        waitForElementToBeClickable(5, logoutButton);
        logoutButton.click();
        logger.info("User logged out");
    }

    public void sideMenu(String menu) {
        hoverProfileIcon();
        if (menu.equalsIgnoreCase("subscriptions")) {
            waitForElementToBeClickable(3, subscriptionsMenu);
            subscriptionsMenu.click();
        } else if (menu.equalsIgnoreCase("my account")) {
            waitForElementToBeClickable(3, myAccountMenu);
            myAccountMenu.click();
        } else if (menu.equalsIgnoreCase("watchlist")) {
            waitForElementToBeClickable(3, watchlistMenu);
            watchlistMenu.click();
        } else if (menu.equalsIgnoreCase("logout")) {
            waitForElementToBeClickable(3, logoutButton);
            logoutButton.click();
        } else {
            logger.warn("Unknown side menu option: {}", menu);
        }
    }

    // ==================== TOP NAVIGATION ====================

    public void navigateToHome() {
        waitForElementToBeClickable(10, homeTab);
        homeTab.click();
        logger.info("Navigated to Home");
    }

    public void navigateToMovies() {
        waitForElementToBeClickable(10, moviesTab);
        moviesTab.click();
        logger.info("Navigated to Movies");
    }

    public void navigateToLiveTv() {
        waitForElementToBeClickable(10, liveTvTab);
        liveTvTab.click();
        logger.info("Navigated to Live TV");
    }

    public void navigateToTvShows() {
        waitForElementToBeClickable(10, tvShowsTab);
        tvShowsTab.click();
        logger.info("Navigated to TV Shows");
    }

    public void navigateToMusicVideos() {
        waitForElementToBeClickable(10, musicVideosTab);
        musicVideosTab.click();
        logger.info("Navigated to Music Videos");
    }

    public void navigateToComedy() {
        waitForElementToBeClickable(10, comedyTab);
        comedyTab.click();
        logger.info("Navigated to Comedy");
    }

    public void navigateToShorts() {
        waitForElementToBeClickable(10, shortsTab);
        shortsTab.click();
        logger.info("Navigated to Shorts");
    }

    public void pageRedirection(String tabName) {
        String capitalized = tabName.substring(0, 1).toUpperCase() + tabName.substring(1);
        WebElement tab = driver.findElement(By.xpath("//span[text()='" + capitalized + "']"));
        waitForElementToBeClickable(10, tab);
        tab.click();
        logger.info("Navigated to {}", capitalized);
    }

    // ==================== CAROUSEL CONTENT ====================

    /**
     * Scrolls, picks a carousel and content item, clicks it.
     * Pass -1 for random selection.
     */
    public void contentPlayBackCheck(int carouselIndex, int contentIndex) throws InterruptedException {
        scrollPageWithLoad();
        logger.info("Extracting carousels...");

        List<String> carouselNames = new ArrayList<>();
        for (WebElement carousel : carouselList) {
            carouselNames.add(carousel.getText());
        }

        if (carouselIndex == -1 && !carouselList.isEmpty()) {
            carouselIndex = new Random().nextInt(carouselList.size());
        }

        String targetName = carouselList.get(carouselIndex).getText();
        logger.info("Targeted carousel: {}", targetName);

        if (carouselNames.contains(targetName)) {
            WebElement targetCarousel = carouselList.get(carouselIndex);
            scrollIntoView(targetCarousel);
            targetCarousel.click();

            int maxDepth = 5;
            int clickDepth = 0;

            while (clickDepth < maxDepth) {
                List<WebElement> insideCarousel = driver.findElements(
                        By.className("viewmore_movie_images__2NctY"));

                for (int i = 0; i < 5; i++) {
                    scrollByPixel();
                    Thread.sleep(5000);
                    insideCarousel = driver.findElements(By.className("viewmore_movie_images__2NctY"));
                    if (insideCarousel.size() >= 30) break;
                }

                if (contentIndex == -1 && !insideCarousel.isEmpty()) {
                    contentIndex = new Random().nextInt(insideCarousel.size());
                }

                if (contentIndex >= insideCarousel.size()) {
                    logger.warn("No content at index: {}", contentIndex);
                    break;
                }

                try {
                    WebElement contentToClick = insideCarousel.get(contentIndex);
                    scrollIntoView(contentToClick);
                    Thread.sleep(3000);
                    contentToClick.click();
                    Thread.sleep(3000);

                    boolean morePages = !driver.findElements(
                            By.xpath("//img[@alt='Sticky Banner']")).isEmpty();
                    if (morePages) {
                        clickDepth++;
                        contentIndex = -1;
                    } else {
                        break;
                    }
                } catch (NoSuchElementException e) {
                    logger.error("Element not found clicking content", e);
                } catch (Exception e) {
                    logger.error("Exception during playback check", e);
                }
            }
        } else {
            logger.warn("Carousel not found: {}", targetName);
        }
    }
}
