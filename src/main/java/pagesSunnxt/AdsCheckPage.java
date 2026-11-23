package pagesSunnxt;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.interactions.Actions;

import config.Log;
import helpers.WaitUtils;
import helpers.NetworkUtils;

/**
 * AdsCheckPage - Simple ad checks for the video player.
 * An ad is a <video> whose id starts with pulseAdPlayerHTML5_
 * The real content is any other <video>.
 */
public class AdsCheckPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(AdsCheckPage.class);
    private NetworkUtils network;
    private boolean browserAdRequested;

    @FindBy(xpath = "//input[@type='range' and @aria-label='Seek']")
    private WebElement seekBar;

    public AdsCheckPage(WebDriver driver) {
        super(driver);
    }

    public AdsCheckPage(WebDriver driver, NetworkUtils network) {
        super(driver);
        this.network = network;
    }

    // ==================== JAVASCRIPT HELPERS ====================

    // Finds the visible content video (not the ad video) and keeps it in "video"
    private static final String FIND_CONTENT_VIDEO =
            "var videos = document.querySelectorAll(\"video:not([id^='pulseAdPlayerHTML5_'])\");"
            + "var video = null;"
            + "for (var i = 0; i < videos.length; i++) {"
            + "  var r = videos[i].getBoundingClientRect();"
            + "  if (r.width > 0 && r.height > 0 && videos[i].readyState >= 2) { video = videos[i]; break; }"
            + "}";

    // ==================== AD CHECK ====================

    /** True when an ad video is visible and playing right now. */
    public boolean isAdPlaying() {
        Object result = js.executeScript(
                "var ads = document.querySelectorAll(\"video[id^='pulseAdPlayerHTML5_']\");"
                + "for (var i = 0; i < ads.length; i++) {"
                + "  var r = ads[i].getBoundingClientRect();"
                + "  if (r.width > 0 && r.height > 0 && !ads[i].paused && !ads[i].ended && ads[i].readyState >= 2) return true;"
                + "}"
                + "return false;");
        return Boolean.TRUE.equals(result);
    }

    /** Checks once every second. Returns true as soon as an ad is seen. */
    public boolean waitForAd(int seconds) throws InterruptedException {
        for (int second = 1; second <= seconds; second++) {
            if (network != null) {
                checkBrowserAdRequests();
            }
            if (isAdPlaying()) {
                logger.info("Ad found after {} second(s)", second);
                return true;
            }
            Thread.sleep(1000);
        }
        logger.info("No ad found in {} seconds", seconds);
        return false;
    }

    // ==================== AD REQUEST CHECK ====================

    // Ad request example:
    // https://in-suntv.videoplaza.tv/proxy/distributor/v2?rt=vp_3.0&pf=html5&...
    private static final String AD_REQUEST_HOST = "suntv.videoplaza.";
    private static final String AD_REQUEST_PATH = "/proxy/distributor";

    /** Forget the old requests. Call this just BEFORE the action that should trigger the ad (play, seek, next). */
    public void clearAdRequests() {
        if (network != null) {
            checkFrameAdRequests(true);
            network.clearCapturedRequests();
            browserAdRequested = false;
            if (!network.isCaptureAvailable()) {
                throw new IllegalStateException(network.getCaptureFailure());
            }
            logger.info("Browser ad-request window reset");
            return;
        }
        js.executeScript(
                "if (window.sunNxtAdObserver) window.sunNxtAdObserver.disconnect();"
                + "performance.setResourceTimingBufferSize(1000); performance.clearResourceTimings();"
                + "window.sunNxtAdRequestStart = performance.now();"
                + "window.sunNxtAdRequested = false;"
                + "var host = arguments[0], path = arguments[1];"
                + "window.sunNxtRecordAdRequest = function(value) {"
                + "  try {"
                + "    var url = new URL(value, document.baseURI);"
                + "    if (url.hostname.indexOf(host) !== -1 && url.pathname.indexOf(path) !== -1) {"
                + "      window.sunNxtAdRequested = true;"
                + "    }"
                + "  } catch (e) {}"
                + "};"
                + "if (!window.sunNxtAdHooksInstalled) {"
                + "  if (window.fetch) {"
                + "    var originalFetch = window.fetch;"
                + "    window.fetch = function(input) {"
                + "      var result = originalFetch.apply(this, arguments);"
                + "      window.sunNxtRecordAdRequest(typeof input === 'string' ? input : input.url || input.href);"
                + "      return result;"
                + "    };"
                + "  }"
                + "  var originalOpen = XMLHttpRequest.prototype.open;"
                + "  var originalSend = XMLHttpRequest.prototype.send;"
                + "  XMLHttpRequest.prototype.open = function(method, url) {"
                + "    var result = originalOpen.apply(this, arguments);"
                + "    this.sunNxtAdUrl = url;"
                + "    return result;"
                + "  };"
                + "  XMLHttpRequest.prototype.send = function() {"
                + "    var result = originalSend.apply(this, arguments);"
                + "    window.sunNxtRecordAdRequest(this.sunNxtAdUrl);"
                + "    return result;"
                + "  };"
                + "  window.sunNxtAdHooksInstalled = true;"
                + "}"
                + "window.sunNxtAdObserver = new PerformanceObserver(function(list) {"
                + "  var entries = list.getEntries();"
                + "  for (var i = 0; i < entries.length; i++) {"
                + "    if (entries[i].startTime < window.sunNxtAdRequestStart) continue;"
                + "    try {"
                + "      var url = new URL(entries[i].name);"
                + "      if (url.hostname.indexOf(host) !== -1 && url.pathname.indexOf(path) !== -1) {"
                + "        window.sunNxtAdRequested = true;"
                + "      }"
                + "    } catch (e) {}"
                + "  }"
                + "});"
                + "window.sunNxtAdObserver.observe({entryTypes: ['resource']});",
                AD_REQUEST_HOST, AD_REQUEST_PATH);
    }

    /** True when the browser has made an ad request since the last clearAdRequests(). */
    public boolean isAdRequested() {
        if (network != null) {
            checkBrowserAdRequests();
            if (!browserAdRequested) {
                browserAdRequested = checkFrameAdRequests(false);
            }
            if (!browserAdRequested && network.getCapturedRequests().isEmpty()) {
                throw new IllegalStateException("No browser network traffic captured; ad absence cannot be verified");
            }
            logger.info("Browser ad request found: {}", browserAdRequested);
            return browserAdRequested;
        }
        Object result = js.executeScript(
                "if (window.sunNxtAdRequested === true) return true;"
                + "var entries = performance.getEntriesByType('resource');"
                + "for (var i = 0; i < entries.length; i++) {"
                + "  if (entries[i].startTime < window.sunNxtAdRequestStart) continue;"
                + "  try {"
                + "    var url = new URL(entries[i].name);"
                + "    if (url.hostname.indexOf(arguments[0]) !== -1 && url.pathname.indexOf(arguments[1]) !== -1) return true;"
                + "  } catch (e) {}"
                + "}"
                + "return false;", AD_REQUEST_HOST, AD_REQUEST_PATH);
        boolean requested = Boolean.TRUE.equals(result);
        logger.info("Ad request found: {}", requested);
        if (!requested) {
            Object entries = js.executeScript("return performance.getEntriesByType('resource').length;");
            Object observing = js.executeScript("return !!window.sunNxtAdObserver;");
            Object hooks = js.executeScript("return window.sunNxtAdHooksInstalled === true;");
            logger.info("Ad request diagnostics: resource entries = {}, observation window present = {}, request hooks installed = {}",
                    entries, observing, hooks);
        }
        return requested;
    }

    private void checkBrowserAdRequests() {
        List<String> requests = network.getCapturedRequests();
        if (!network.isCaptureAvailable()) {
            throw new IllegalStateException(network.getCaptureFailure());
        }
        if (browserAdRequested) return;
        for (String request : requests) {
            try {
                URI url = URI.create(request);
                if (url.getHost() != null && url.getPath() != null
                        && url.getHost().contains(AD_REQUEST_HOST) && url.getPath().contains(AD_REQUEST_PATH)) {
                    browserAdRequested = true;
                    logger.info("Browser ad request detected: {}{}", url.getHost(), url.getPath());
                    return;
                }
            } catch (IllegalArgumentException e) {
                // Ignore entries that are not valid request URLs.
            }
        }
    }

    private boolean checkFrameAdRequests(boolean reset) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                driver.switchTo().defaultContent();
                return checkChildFrames(reset);
            } catch (StaleElementReferenceException | NoSuchFrameException e) {
                if (attempt == 3) {
                    throw new IllegalStateException("Ad frames kept changing during request "
                            + (reset ? "reset" : "inspection") + " after 3 attempts", e);
                }
                logger.info("Ad frame changed. Re-reading frames, attempt {} of 3", attempt + 1);
            } finally {
                driver.switchTo().defaultContent();
            }
        }
        throw new IllegalStateException("Ad frame inspection did not complete");
    }

    private boolean checkChildFrames(boolean reset) {
        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (WebElement frame : frames) {
            driver.switchTo().frame(frame);
            try {
                if (reset) {
                    js.executeScript("performance.setResourceTimingBufferSize(1000); performance.clearResourceTimings();"
                            + "window.sunNxtFrameAdStart = performance.now();");
                } else {
                    Object result = js.executeScript(
                            "var entries = performance.getEntriesByType('resource');"
                            + "for (var i = 0; i < entries.length; i++) {"
                            + "  if (entries[i].startTime < window.sunNxtFrameAdStart) continue;"
                            + "  try {"
                            + "    var url = new URL(entries[i].name);"
                            + "    if (url.hostname.indexOf(arguments[0]) !== -1 && url.pathname.indexOf(arguments[1]) !== -1) return true;"
                            + "  } catch (e) {}"
                            + "} return false;", AD_REQUEST_HOST, AD_REQUEST_PATH);
                    if (Boolean.TRUE.equals(result)) {
                        logger.info("Ad request detected in a child frame");
                        return true;
                    }
                }
                if (checkChildFrames(reset)) {
                    return true;
                }
            } finally {
                driver.switchTo().parentFrame();
            }
        }
        return false;
    }

    // ==================== CONTENT CHECK ====================

    /** Waits until the real content (not the ad) is playing. */
    public boolean waitForContent(int seconds) throws InterruptedException {
        for (int second = 1; second <= seconds; second++) {
            if (network != null) {
                checkBrowserAdRequests();
            }
            Object result = js.executeScript(FIND_CONTENT_VIDEO
                    + "return video != null && !video.paused && !video.ended && video.currentTime > 2;");
            if (Boolean.TRUE.equals(result)) {
                logger.info("Content is playing");
                return true;
            }
            Thread.sleep(1000);
        }
        logger.warn("Content did not start playing in {} seconds", seconds);
        return false;
    }

    /** Content length in seconds. Returns 0 if it cannot be read (or for Live TV). */
    public double getContentDuration() {
        Object result = js.executeScript(FIND_CONTENT_VIDEO + "return video ? video.duration : 0;");
        if (result instanceof Number) {
            double duration = ((Number) result).doubleValue();
            if (!Double.isNaN(duration) && !Double.isInfinite(duration)) {
                return duration;
            }
        }
        return 0;
    }

    public void startSeekDebug() {
        js.executeScript("""
                window.sunNxtSeekEvents = [];
                window.sunNxtSeekPhase = 'automated';
                if (!window.sunNxtSeekDebugInstalled) {
                    window.sunNxtSeekDebugInstalled = true;
                    ['pointerdown','pointerup','mousedown','mouseup','click','input','change','seeking','seeked'].forEach(function(name) {
                        document.addEventListener(name, function(event) {
                            var target = event.target;
                            var video = document.querySelector("video:not([id^='pulseAdPlayerHTML5_'])");
                            var seek = document.querySelector("input[type='range'][aria-label='Seek']");
                            if (!video || window.sunNxtSeekEvents.length >= 300) return;
                            window.sunNxtSeekEvents.push({time:new Date().toISOString(),phase:window.sunNxtSeekPhase,
                                event:name,trusted:event.isTrusted,target:target.tagName,classes:String(target.className),
                                x:event.clientX,y:event.clientY,button:event.button,
                                seekValue:seek ? seek.value : null,currentTime:video.currentTime,
                                paused:video.paused,seeking:video.seeking,readyState:video.readyState});
                        }, true);
                    });
                }
                """);
        logger.info("Seek event recording started: automated phase");
    }

    public void startManualSeekDebug() {
        js.executeScript("window.sunNxtSeekPhase = 'manual';");
        logger.info("Manual seek comparison window open");
    }

    public void saveSeekDebug() throws java.io.IOException {
        String events = (String) js.executeScript("return JSON.stringify(window.sunNxtSeekEvents || [], null, 2);");
        java.nio.file.Path folder = java.nio.file.Path.of("logs", "network");
        java.nio.file.Files.createDirectories(folder);
        java.nio.file.Path file = java.nio.file.Files.createTempFile(folder, "seek-events-", ".json");
        java.nio.file.Files.writeString(file, events);
        logger.info("Seek event debug file: {}", file.toAbsolutePath());
    }

    public void savePlayerDebug() throws java.io.IOException {
        java.nio.file.Path folder = java.nio.file.Path.of("logs", "network");
        java.nio.file.Files.createDirectories(folder);
        java.nio.file.Path screenshot = java.nio.file.Files.createTempFile(folder, "player-state-", ".png");
        byte[] bytes = ((org.openqa.selenium.TakesScreenshot) driver)
                .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
        java.nio.file.Files.write(screenshot, bytes);
        logger.info("Player debug screenshot: {}", screenshot.toAbsolutePath());
        saveSeekDebug();
    }

    /** Seeks with one mouse click on the player's timeline. */
    public void seekTo(int seconds) throws InterruptedException {
        Object content = js.executeScript(FIND_CONTENT_VIDEO + "return video;");
        if (!(content instanceof WebElement)) {
            throw new IllegalStateException("No ready content video found before seeking");
        }
        WebElement video = (WebElement) content;
        Object before = js.executeScript("return arguments[0].currentTime;", video);
        logger.info("Mouse seeking content from {} to {} seconds", before, seconds);
        Actions mouse = new Actions(driver);
        mouse.moveToElement(video).perform();
        waitForElementToBeClickable(10, seekBar);
        double minimum = Double.parseDouble(seekBar.getDomAttribute("min"));
        double maximum = Double.parseDouble(seekBar.getDomAttribute("max"));
        int width = seekBar.getRect().getWidth();
        if (width < 2 || maximum <= minimum || seconds < minimum || seconds >= maximum) {
            throw new IllegalArgumentException("Seek position is outside the player's available range");
        }
        double secondsPerPixel = (maximum - minimum) / width;
        double tolerance = Math.max(5, secondsPerPixel * 2);
        int offset = (int) Math.ceil((seconds - minimum) / (maximum - minimum) * width - width / 2.0);
        offset = Math.max(-width / 2 + 1, Math.min(width / 2 - 1, offset));
        mouse.moveToElement(video).moveToElement(seekBar, offset, 0)
                .pause(java.time.Duration.ofMillis(250)).click().perform();
        double selected = Double.parseDouble(seekBar.getDomProperty("value"));
        if (Math.abs(selected - seconds) > tolerance) {
            throw new IllegalStateException("Mouse seek could not select the requested position; selected " + selected);
        }
        logger.info("Single mouse click on seek bar: requested = {} seconds, selected = {} seconds", seconds, selected);
        for (int second = 1; second <= 15; second++) {
            if (isAdPlaying()) {
                logger.info("Ad playback started after the mouse seek");
                return;
            }
            if (network != null) {
                checkBrowserAdRequests();
                if (browserAdRequested) {
                    logger.info("Fresh ad request started after the mouse seek; checking playback after ad observation");
                    return;
                }
            }
            Object position = js.executeScript(
                    "return !arguments[0].seeking ? arguments[0].currentTime : null;", video);
            if (position instanceof Number && Math.abs(((Number) position).doubleValue() - seconds) <= tolerance) {
                logger.info("Seek completed: requested = {} seconds, actual position = {} seconds", seconds, position);
                return;
            }
            Thread.sleep(1000);
        }
        throw new org.openqa.selenium.TimeoutException("Content did not reach " + seconds + " seconds after seeking");
    }
}
