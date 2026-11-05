package pagesSunShorts;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import config.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import helpers.WaitUtils;

public class PlayerSunShorts extends WaitUtils {

    private static final Logger logger = Log.getLogger(PlayerSunShorts.class);

	public PlayerSunShorts(WebDriver driver) {
		super(driver);
	}

	@FindBy (xpath = "//button[text()='play_arrow']")
	private WebElement playBtnShorts;

	@FindBy (xpath = "//button[text()='pause']")
	private WebElement pauseBtnShorts;

	@FindBy (xpath = "//button[text()='volume_up']")
	private WebElement muteShorts;

	@FindBy (xpath = "//button[text()='volume_off']")
	private WebElement unMuteShorts;

	@FindBy(xpath = "//button[@class='.shaka-vod-resolution']")
	private WebElement resolutionSettings;

	@FindBy(xpath = "//span[text()='Choose resolution']")
	private WebElement resolutionHeader;

	@FindBy(xpath = "//span[text()='Auto']")
	private WebElement autoReslutionShorts;

	@FindBy(xpath = "//span[text()='1080p ']")
	private WebElement fullHdShorts;

	@FindBy (xpath = "//span[text()='720p ']")
	private WebElement hdShorts;

	@FindBy (xpath = "//span[text()='480p ']")
	private WebElement sdShorts;

	@FindBy (xpath = "//span[text()='360p ']")
	private WebElement lowqualityShorts;

	@FindBy (xpath = "//button[text()='fullscreen']")
	private WebElement fullscreenShorts;

	@FindBy (xpath = "//button[text()='fullscreen_exit']")
	private WebElement fullscreenExitShorts;

	@FindBy(xpath = "//button[img[@alt='Previous']]")
	private WebElement previousEpisode;

    @FindBy(xpath = "//*[starts-with(@class, 'font-bold text-center')]")
    private List<WebElement> episodeNumbers;

    @FindBy(xpath = "//h2[normalize-space(.)='Keep the Drama Going']")
    private WebElement subscriptionPopup;

	@FindBy(xpath = "//button[img[@alt='Next']]")
	private WebElement nextEpisode;

	@FindBy (xpath = "//img[@alt='Rate']")
	private WebElement rateContent;


	@FindBy (xpath = "//img[@alt='Watchlist']")
	private WebElement watchlist;

	@FindBy (xpath = "//img[@alt='Share']")
	private WebElement shareShorts;

	@FindBy (xpath = "//img[@alt='Love it']")
	private WebElement rateAsLove;

	@FindBy (xpath = "//img[@alt='Like it']")
	private WebElement rateAsLike;

	@FindBy (xpath = "//img[@alt='Not for me']")
	private WebElement rateAsNotForMe;

	@FindBy (xpath = "//div[@class='shaka-content-title']")
	private WebElement contenTitleShorts;
	
    // ==================== PLAYBACK CONTROLS ====================

    public boolean isSubscriptionPopupDisplayed() {
        return isElementDisplayed(subscriptionPopup);
    }

    public boolean isEpisodeAvailable(int number) {
        for (WebElement episode : episodeNumbers) {
            if (episode.getText().trim().equals(String.valueOf(number))) {
                return true;
            }
        }
        return false;
    }

    public void selectEpisode(int number) {
        for (WebElement episode : episodeNumbers) {
            if (episode.getText().trim().equals(String.valueOf(number))) {
                scrollIntoView(episode);
                waitForElementToBeClickable(10, episode);
                episode.click();
                logger.info("Shorts episode {} selected", number);
                return;
            }
        }
        throw new org.openqa.selenium.NoSuchElementException("Shorts episode " + number + " was not found");
    }

    public int getActiveEpisodeNumber() {
        for (WebElement episode : episodeNumbers) {
            String colour = episode.getCssValue("color").replace(" ", "");
            if (colour.equals("rgb(255,221,26)") || colour.equals("rgba(255,221,26,1)")) {
                return Integer.parseInt(episode.getText().trim());
            }
        }
        throw new org.openqa.selenium.NoSuchElementException("No highlighted SNS episode found");
    }

    public void moveFocusToPlayer() {
        act = new org.openqa.selenium.interactions.Actions(driver);
        act.moveToElement(getVisibleVideo()).moveByOffset(1, 1).perform();
    }

    public void play() {
        waitForElementToBeClickable(10, playBtnShorts);
        playBtnShorts.click();
        logger.info("Sun Shorts Play clicked");
    }

    public void pause() {
        waitForElementToBeClickable(10, pauseBtnShorts);
        pauseBtnShorts.click();
        logger.info("Sun Shorts Pause clicked");
    }

    public void mute() {
        waitForElementToBeClickable(5, muteShorts);
        muteShorts.click();
        logger.info("Sun Shorts muted");
    }

    public void unMute() {
        waitForElementToBeClickable(5, unMuteShorts);
        unMuteShorts.click();
        logger.info("Sun Shorts unmuted");
    }

    public void fullScreen() {
        waitForElementToBeClickable(5, fullscreenShorts);
        fullscreenShorts.click();
        logger.info("Sun Shorts fullscreen opened");
    }

    public void exitFullScreen() {
        waitForElementToBeClickable(5, fullscreenExitShorts);
        fullscreenExitShorts.click();
        logger.info("Sun Shorts fullscreen closed");
    }

    // ==================== RESOLUTION ====================

    public void openResolutionSettings() {
        waitForElementToBeClickable(5, resolutionSettings);
        resolutionSettings.click();
        waitForElementToBeVisible(5, resolutionHeader);
        logger.info("Sun Shorts resolution settings opened");
    }

    public void changeResolution(String quality) {
        if (quality == null) {
            throw new IllegalArgumentException("Resolution must not be null");
        }
        WebElement option;
        if (quality.equalsIgnoreCase("auto")) {
            option = autoReslutionShorts;
        } else if (quality.equals("1080p") || quality.equals("1080")) {
            option = fullHdShorts;
        } else if (quality.equals("720p") || quality.equals("720")) {
            option = hdShorts;
        } else if (quality.equals("480p") || quality.equals("480")) {
            option = sdShorts;
        } else if (quality.equals("360p") || quality.equals("360")) {
            option = lowqualityShorts;
        } else {
            throw new IllegalArgumentException("Use Auto, 1080p, 720p, 480p or 360p");
        }
        openResolutionSettings();
        waitForElementToBeClickable(5, option);
        option.click();
        logger.info("Sun Shorts resolution selected: {}", quality);
    }

    // ==================== EPISODE NAVIGATION ====================

    public String getContentTitle() {
        waitForElementToBeVisible(10, contenTitleShorts);
        return contenTitleShorts.getText().trim();
    }

    public void nextEpisode() {
        moveFocusToPlayer();
        waitForElementToBeClickable(10, nextEpisode);
        nextEpisode.click();
        logger.info("Next episode button clicked");
    }

    public void previousEpisode() {
        moveFocusToPlayer();
        waitForElementToBeClickable(10, previousEpisode);
        previousEpisode.click();
        logger.info("Previous episode button clicked");
    }
    // ==================== RATING, WATCHLIST AND SHARE ====================

    public void rateContent(String rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating must not be null");
        }
        WebElement option;
        if (rating.equalsIgnoreCase("love")) {
            option = rateAsLove;
        } else if (rating.equalsIgnoreCase("like")) {
            option = rateAsLike;
        } else if (rating.equalsIgnoreCase("not for me")) {
            option = rateAsNotForMe;
        } else {
            throw new IllegalArgumentException("Use love, like or not for me");
        }
        if (!isElementDisplayed(option)) {
            waitForElementToBeClickable(5, rateContent);
            rateContent.click();
        }
        waitForElementToBeClickable(5, option);
        option.click();
        logger.info("Sun Shorts rating selected: {}", rating);
    }

    /** The Rate icon keeps its alt text; its image source identifies the selection. */
    public String getSelectedRating() {
        waitForElementToBeVisible(5, rateContent);
        String source = rateContent.getDomAttribute("src");
        if (source == null) {
            return "";
        }
        source = source.replace("%20", " ");
        if (source.endsWith("/Love it.svg")) {
            return "love";
        } else if (source.endsWith("/Like it.svg")) {
            return "like";
        } else if (source.endsWith("/Not for me.svg")) {
            return "not for me";
        }
        return "";
    }

    /** Toggles watchlist membership; does not assume the current saved state. */
    public void clickWatchlist() {
        waitForElementToBeClickable(5, watchlist);
        watchlist.click();
        logger.info("Sun Shorts Watchlist clicked");
    }

    public void clickShare() {
        waitForElementToBeClickable(5, shareShorts);
        shareShorts.click();
        logger.info("Sun Shorts Share clicked");
    }

    // ==================== VALIDATION CHECKS ====================

    public boolean isPaused() {
        return Boolean.TRUE.equals(js.executeScript("return arguments[0].paused;", getVisibleVideo()));
    }

    public boolean isMuted() {
        return Boolean.TRUE.equals(js.executeScript("return arguments[0].muted;", getVisibleVideo()));
    }

    public boolean isFullScreen() {
        return Boolean.TRUE.equals(js.executeScript("return document.fullscreenElement != null;"));
    }

    private WebElement getVisibleVideo() {
        return new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> (WebElement) js.executeScript(
                "var videos=document.getElementsByTagName('video');"
                + "for(var i=0;i<videos.length;i++){var r=videos[i].getBoundingClientRect();"
                + "if(r.width>0 && r.height>0) return videos[i];} return null;"));
    }

    /** Checks a visible HTML5 video and waits for its playback clock to advance. */
    public boolean waitForPlaybackProgress(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Playback timeout must be positive");
        }
        try {
            WebDriverWait playbackWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement video = playbackWait.until(d -> (WebElement) js.executeScript(
                    "var videos = document.getElementsByTagName('video');"
                    + "for (var i=0; i<videos.length; i++) {"
                    + "var v=videos[i]; var r=v.getBoundingClientRect();"
                    + "if (r.width>0 && r.height>0 && v.readyState>=2 && !v.paused && !v.ended) return v;"
                    + "} return null;"));
            Number initialTime = (Number) js.executeScript("return arguments[0].currentTime;", video);
            playbackWait.until(d -> Boolean.TRUE.equals(js.executeScript(
                    "var v=arguments[0]; var r=v.getBoundingClientRect();"
                    + "return v.isConnected && r.width>0 && r.height>0"
                    + " && !v.paused && !v.ended && v.readyState>=2"
                    + " && v.currentTime>arguments[1]+2;", video, initialTime.doubleValue())));
            logger.info("Sun Shorts video playback advanced");
            return true;
        } catch (TimeoutException e) {
            logger.warn("Sun Shorts video did not advance within the playback observation window");
            return false;
        }
    }

    /** UI state only. Actual playback progress needs a supplied video locator. */
    public boolean isPauseButtonDisplayed() {
        return isElementVisible(10, pauseBtnShorts);
    }

    public boolean isPlayButtonDisplayed() {
        return isElementVisible(10, playBtnShorts);
    }

    public boolean isNextEpisodeDisplayed() {
        moveFocusToPlayer();
        return isElementVisible(5, nextEpisode);
    }

    public boolean isPreviousEpisodeDisplayed() {
        moveFocusToPlayer();
        return isElementVisible(5, previousEpisode);
    }

    public boolean isNextEpisodeEnabled() {
        return isNextEpisodeDisplayed() && nextEpisode.isEnabled();
    }

    public boolean isPreviousEpisodeEnabled() {
        return isPreviousEpisodeDisplayed() && previousEpisode.isEnabled();
    }

    public boolean isResolutionHeaderDisplayed() {
        return isElementVisible(5, resolutionHeader);
    }
}
