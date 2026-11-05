package pagesSunnxt;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import helpers.NetworkUtils;
import helpers.WaitUtils;

/** Ad observation and content playback checks shared by SN and Shorts. */
public class AdsPage extends WaitUtils {

    private final NetworkUtils network;
    private boolean adVisible;
    private boolean adRequested;

    public AdsPage(WebDriver driver, NetworkUtils network) {
        super(driver);
        this.network = network;
    }

    @FindBy(xpath = "//video[starts-with(@id,'pulseAdPlayerHTML5_')]")
    private List<WebElement> adVideos;

    @FindBy(xpath = "//video[not(starts-with(@id,'pulseAdPlayerHTML5_'))]")
    private List<WebElement> contentVideos;

    // ==================== AD OBSERVATION ====================

    public void startAdObservation() {
        adVisible = false;
        adRequested = false;
        if (network != null) network.clearCapturedRequests();
    }

    public void checkAds() {
        for (WebElement video : adVideos) {
            try {
                if (network != null) network.registerAdMediaUrl(video.getDomProperty("src"));
                if (video.isDisplayed() && Boolean.TRUE.equals(js.executeScript(
                        "return !arguments[0].paused && !arguments[0].ended && arguments[0].readyState>=2;", video))) {
                    adVisible = true;
                }
            } catch (StaleElementReferenceException e) {
                logger.debug("Ad video changed while checking visibility");
            }
        }
        if (network != null && network.isAdRequested()) {
            adRequested = true;
        }
    }

    public void observeAds(int seconds) throws InterruptedException {
        long end = System.nanoTime() + Duration.ofSeconds(seconds).toNanos();
        do {
            checkAds();
            Thread.sleep(250);
        } while (System.nanoTime() < end);
    }

    public boolean wasAdVisible() {
        return adVisible;
    }

    public boolean wasAdRequested() {
        return adRequested;
    }

    public boolean isNetworkCaptureAvailable() {
        return network != null && !network.getCapturedRequests().isEmpty() && network.isCaptureAvailable();
    }

    // ==================== CONTENT PLAYBACK ====================

    private WebElement getContentVideo() {
        for (WebElement video : contentVideos) {
            if (video.isDisplayed() && Boolean.TRUE.equals(
                    js.executeScript("return arguments[0].readyState>=2;", video))) {
                return video;
            }
        }
        return null;
    }

    /** Excludes Pulse ad videos, so an ad alone cannot pass content playback. */
    public boolean waitForContentPlayback(int seconds) throws InterruptedException {
        long end = System.nanoTime() + Duration.ofSeconds(seconds).toNanos();
        WebElement previousVideo = null;
        double initialTime = -1;
        while (System.nanoTime() < end) {
            checkAds();
            try {
                WebElement video = getContentVideo();
                if (video != null) {
                    double time = ((Number) js.executeScript("return arguments[0].currentTime;", video)).doubleValue();
                    if (!video.equals(previousVideo) || time < initialTime) {
                        previousVideo = video;
                        initialTime = time;
                    }
                    if (time > initialTime + 2 && Boolean.TRUE.equals(js.executeScript(
                            "return !arguments[0].paused && !arguments[0].ended;", video))) {
                        logger.info("Content playback advanced independently of the ad video");
                        return true;
                    }
                }
            } catch (StaleElementReferenceException e) {
                previousVideo = null;
                logger.debug("Content video changed while checking playback");
            }
            Thread.sleep(250);
        }
        return false;
    }

    public boolean canSeekToMidRoll() {
        WebElement video = getContentVideo();
        if (video == null) return false;
        double duration = ((Number) js.executeScript("return arguments[0].duration;", video)).doubleValue();
        return Double.isFinite(duration) && duration > 900;
    }

    public void seekToMidRoll() {
        WebElement video = getContentVideo();
        js.executeScript("arguments[0].currentTime=900;", video);
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            checkAds();
            return Boolean.TRUE.equals(js.executeScript(
                    "return !arguments[0].seeking && arguments[0].currentTime>=899;", video));
        });
        logger.info("Content seek completed at 15:00");
    }
}
