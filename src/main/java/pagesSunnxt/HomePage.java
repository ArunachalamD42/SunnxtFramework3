package pagesSunnxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;
import java.util.concurrent.ThreadLocalRandom;
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

    // ==================== CAROUSEL ELEMENTS ====================

    @FindBy(xpath = "//h2")
    private List<WebElement> carouselTitles;

    @FindBy(xpath = "//div[contains(@class,'movie_images')]")
    private List<WebElement> contentTiles;

    @FindBy(xpath = "//img[@alt='poster']")
    private List<WebElement> viewAllBanners;

    // ==================== LIVE TV ELEMENTS ====================

    @FindBy(xpath = "(//div[contains(@class,'overflow-x-auto') "
            + "and contains(@class,'hide-scrollbar')])[1]"
            + "//a[starts-with(@href,'/live/')]")
    private List<WebElement> liveChannelTiles;

    @FindBy(xpath = "//div[contains(@class,'absolute') "
            + "and contains(@class,'right-0') "
            + "and contains(@class,'cursor-pointer')]")
    private List<WebElement> liveChannelNextArrows;

    @FindBy(xpath = "//button[normalize-space(.)='Tamil' "
            + "or normalize-space(.)='Telugu' "
            + "or normalize-space(.)='Malayalam' "
            + "or normalize-space(.)='Kannada' "
            + "or normalize-space(.)='Hindi' "
            + "or normalize-space(.)='Bengali' "
            + "or normalize-space(.)='Marathi' "
            + "or normalize-space(.)='English']")
    private List<WebElement> liveLanguageTabs;

    // ==================== WAIT CHECKS ====================

    private void waitForFirstElement(List<WebElement> elements) {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    if (elements.isEmpty()) {
                        return false;
                    }
                    return elements.get(0).isDisplayed();
                });
    }

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
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                waitForElementToBeVisible(10, profileIcon);
                hoverProfileIcon();
                waitForElementToBeClickable(5, loginButton);
                loginButton.click();
                logger.info("Log In menu clicked. Waiting for the login form");
                break;
            } catch (TimeoutException | StaleElementReferenceException e) {
                if (attempt == 3) {
                    logger.error("Login navigation failed after 3 attempts", e);
                    throw e;
                }
                logger.warn("Log In menu unavailable. Retrying hover, attempt {} of 3", attempt + 1);
            }
        }
        waitForElementToBeClickable(30, By.name("email"));
        logger.info("Navigated to Login page - username field ready");
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
     * Picks the first content tile of the first carousel and opens it.
     * No scrolling or random selection - used by control tests that just need
     * any playable title quickly.
     */
    public void selectFirstContent() throws InterruptedException {
        logger.info("Selecting first content from first carousel...");

        // Let the section settle so carousel titles don't go stale mid-interaction
        waitForFirstElement(carouselTitles);
        Thread.sleep(2000);

        // PageFactory locates the current carousel when the list is accessed
        WebElement firstCarousel = carouselTitles.get(0);
        scrollIntoView(firstCarousel);
        firstCarousel.click();

        // Wait for the poster tiles of the opened carousel to render
        waitForFirstElement(contentTiles);
        Thread.sleep(2000);

        // Click the first tile (re-find fresh right before clicking)
        WebElement firstTile = contentTiles.get(0);
        scrollIntoView(firstTile);
        firstTile.click();
        logger.info("First content selected");
        Thread.sleep(3000);
    }

    /**
     * Picks the first Live TV channel tile and opens it (no language change).
     * Live TV channels are direct anchor tiles (href starting with "/live/"),
     * unlike VOD which needs a carousel -> tile drill-down.
     */
    public void selectFirstLiveChannel() throws InterruptedException {
        logger.info("Selecting first live channel...");
        selectLiveChannel(0);
    }

    /**
     * Picks a random language tab, reveals all its channels (via the next arrow),
     * then opens a random channel from that language row.
     */
    public void selectRandomLiveChannel() throws InterruptedException {
        logger.info("Selecting a random language and live channel...");
        selectRandomLanguage();

        waitForFirstElement(liveChannelTiles);
        Thread.sleep(2000);

        // If a right arrow is shown, more channels exist off-screen; click it to reveal them all
        revealAllLiveChannels();

        int count = liveChannelTiles.size();
        int index = new Random().nextInt(count);
        logger.info("Total channels in selected language: {}", count);
        selectLiveChannel(index);
    }

    /**
     * Clicks the "next" arrow repeatedly to scroll the channel row until no more
     * channels load (arrow gone or count stops growing).
     */
    private void revealAllLiveChannels() throws InterruptedException {
        int previousCount = -1;
        int guard = 0; // safety cap to avoid an infinite loop
        while (guard < 20) {
            int currentCount = liveChannelTiles.size();
            List<WebElement> arrows = liveChannelNextArrows;

            // Stop when the arrow is gone or no new channels appeared after the last scroll
            if (arrows.isEmpty() || currentCount == previousCount) {
                break;
            }
            previousCount = currentCount;

            WebElement arrow = arrows.get(0);
            scrollIntoView(arrow);
            try {
                arrow.click();
            } catch (org.openqa.selenium.ElementNotInteractableException e) {
                js.executeScript("arguments[0].click();", arrow);
            }
            Thread.sleep(1500);
            guard++;
        }
        logger.info("Revealed live channels: {}", liveChannelTiles.size());
    }

    /**
     * Clicks a random Live TV language tab and waits for its channels to load.
     */
    private int previousLanguageIndex = -1;

    
    public void selectRandomLanguage() throws InterruptedException {

        waitForFirstElement(liveLanguageTabs);

        List<WebElement> languages = liveLanguageTabs;
        int langCount = languages.size();

        if (langCount == 0) {
            logger.warn("No language tabs found on Live TV page");
            return;
        }

        if (langCount == 1) {
            logger.warn("Only one language available. Cannot avoid repetition.");
            return;
        }

        int langIndex;

        do {
            langIndex = ThreadLocalRandom.current().nextInt(langCount);
        } while (langIndex == previousLanguageIndex);

        WebElement language = languages.get(langIndex);
        String langName = language.getText();

        scrollIntoView(language);
        language.click();

        previousLanguageIndex = langIndex;

        logger.info("Live TV language index: {}", langIndex);
        logger.info("Live TV language selected: {}", langName);

        Thread.sleep(3000);
    }

    /**
     * Opens the Live TV channel tile at the given 0-based index.
     * Channel tiles are anchors linking to a /live/ path.
     */
    private void selectLiveChannel(int index) throws InterruptedException {
        waitForFirstElement(liveChannelTiles);
        Thread.sleep(2000);

        // Re-find fresh to avoid a stale reference, then click the chosen tile
        WebElement channel = liveChannelTiles.get(index);
        scrollIntoView(channel);
        Thread.sleep(1000);
        try {
            channel.click();
        } catch (org.openqa.selenium.ElementNotInteractableException e) {
            // Overlay intercept or non-interactable anchor; fall back to a JS click
            // (ElementClickInterceptedException is a subclass, so this covers both)
            logger.warn("Channel not directly clickable; using JS click");
            js.executeScript("arguments[0].click();", channel);
        }
        logger.info("Live channel [{}] selected", index);
        Thread.sleep(3000);
    }

    /**
     * Scrolls, picks a carousel and content item, clicks it.
     * Pass -1 for random selection.
     */
    public void contentPlayBackCheck(int carouselIndex, int contentIndex) throws InterruptedException {
        // Scroll the whole page so lazy-loaded carousel rows are rendered
        scrollPageWithLoad();
        
        logger.info("Extracting carousels...");

        // Collect the visible carousel titles (used to confirm the target exists)
        List<String> carouselNames = new ArrayList<>();
        for (WebElement carousel : carouselTitles) {
            carouselNames.add(carousel.getText());
        }

        if (carouselNames.isEmpty()) {
            throw new NoSuchElementException("No carousel titles found after scrolling");
        }

        // If caller passed -1, pick a random carousel row
        if (carouselIndex == -1 && !carouselTitles.isEmpty()) {
            carouselIndex = new Random().nextInt(carouselTitles.size());
        }

        // Guard: if the requested index is beyond what loaded, fall back to a random valid one
        if (carouselIndex >= carouselTitles.size()) {
            logger.warn("Requested carousel index {} >= available {}. Picking random.",
                    carouselIndex, carouselTitles.size());
            carouselIndex = new Random().nextInt(carouselTitles.size());
        }

        // Resolve the chosen carousel's title for logging/validation
        String targetName = carouselTitles.get(carouselIndex).getText();
        logger.info("Targeted carousel: {}", targetName);

        // Proceed only if the chosen carousel title is actually present
        if (carouselNames.contains(targetName)) {
            // Bring the target carousel into view and open it
            WebElement targetCarousel = carouselTitles.get(carouselIndex);
            scrollIntoView(targetCarousel);
            targetCarousel.click();

            // Limit how many nested "View All" pages we drill into
            int maxDepth = 5;
            int clickDepth = 0;

            while (clickDepth < maxDepth) {
                // Grab the poster tiles currently rendered in the opened view
                List<WebElement> insideCarousel = new ArrayList<>(contentTiles);

                // Scroll a few times to let more tiles lazy-load (up to ~30)
                for (int i = 0; i < 5; i++) {
                    scrollByPixel();
                    Thread.sleep(5000);
                    insideCarousel = new ArrayList<>(contentTiles);
                    if (insideCarousel.size() >= 30) {
                    	break;
                    }
                }

                logger.info("Content tiles loaded in {}: {}", targetName, insideCarousel.size());
                if (insideCarousel.isEmpty()) {
                    throw new NoSuchElementException("No content tiles loaded after opening carousel: " + targetName);
                }

                // If caller passed -1, pick a random tile from what loaded
                if (contentIndex == -1 && !insideCarousel.isEmpty()) {
                    contentIndex = new Random().nextInt(insideCarousel.size());
                }

                // Guard against an index beyond the available tiles
                if (contentIndex >= insideCarousel.size()) {
                    logger.warn("No content at index: {}", contentIndex);
                    break;
                }

                try {
                    // Select the target tile, scroll it into view, and click it
                    WebElement contentToClick = insideCarousel.get(contentIndex);
                    scrollIntoView(contentToClick);
                    Thread.sleep(3000);
                    contentToClick.click();
                    Thread.sleep(3000);

                    // Detect whether the click opened another "View All" listing (Sticky Banner present)
                    boolean morePages = !viewAllBanners.isEmpty();
                    if (morePages) {
                        // Went one level deeper: increment depth and re-randomize the next pick
                        clickDepth++;
                        contentIndex = -1;
                        ContentDetailPage cd=new ContentDetailPage(driver);
                        try {
							if(cd.isWatchNowEnabled()) {
								break;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    } else {
                        // Reached the content detail page: stop drilling
                        break;
                    }
                } catch (NoSuchElementException e) {
                    // Tile disappeared/relocated between find and click
                    logger.error("Element not found clicking content", e);
                } catch (Exception e) {
                    // Any other failure during the drill-down loop
                    logger.error("Exception during playback check", e);
                }
            }
        } else {
            // Chosen carousel title was not found on the page
            logger.warn("Carousel not found: {}", targetName);
        }
    }
}
