package helpers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import config.Log;

/** Captures ChromeDriver network events without version-specific DevTools imports. */
public class NetworkUtils {

    private static final Logger logger = Log.getLogger(NetworkUtils.class);
    private final WebDriver driver;
    private final List<String> requests = new ArrayList<>();
    private final Set<String> adMediaUrls = new HashSet<>();
    private boolean started;
    private String captureFailure = "Capture has not started";

    public NetworkUtils(WebDriver driver) {
        if (!(driver instanceof HasCdp)) {
            throw new IllegalArgumentException("Network capture requires a Chromium driver");
        }
        this.driver = driver;
    }

    public void startCapture() {
        if (started) return;
        try {
            if (!driver.manage().logs().getAvailableLogTypes().contains(LogType.PERFORMANCE)) {
                throw new IllegalStateException("Enable performance logging in ChromeOptions before creating the driver");
            }
            ((HasCdp) driver).executeCdpCommand("Network.enable", Map.of());
            driver.manage().logs().get(LogType.PERFORMANCE);
            requests.clear();
            started = true;
            captureFailure = "";
            logger.info("Network capture started using ChromeDriver performance logs");
        } catch (RuntimeException e) {
            started = false;
            captureFailure = "Network capture could not start: " + e.getClass().getSimpleName();
            throw e;
        }
    }

    /** Poll regularly during playback; log reads drain ChromeDriver's event buffer. */
    private void readRequests() {
        if (!started) return;
        try {
            for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE)) {
                Map<?, ?> envelope = new Json().toType(entry.getMessage(), Map.class);
                Map<?, ?> message = (Map<?, ?>) envelope.get("message");
                if (message == null || !"Network.requestWillBeSent".equals(message.get("method"))) continue;
                Map<?, ?> params = (Map<?, ?>) message.get("params");
                Map<?, ?> request = (Map<?, ?>) params.get("request");
                String url = (String) request.get("url");
                if (url != null) requests.add(url);
            }
        } catch (RuntimeException e) {
            started = false;
            captureFailure = "Network log read failed: " + e.getClass().getSimpleName();
            logger.warn("{}; ad absence cannot be confirmed", captureFailure);
        }
    }

    /** Register only a source read from the supplied Pulse ad-video element. */
    public void registerAdMediaUrl(String url) {
        if (url != null && !url.isBlank()) adMediaUrls.add(url);
    }

    private boolean isAdUrl(String url) {
        if (adMediaUrls.contains(url)) return true;
        try {
            String host = URI.create(url).getHost();
            // Preserve the existing Pulse/Videoplaza server family, matching the host only.
            return host != null && host.toLowerCase(Locale.ROOT).startsWith("suntv.videoplaza.");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isAdRequested() {
        return !getAdRequests().isEmpty();
    }

    public List<String> getAdRequests() {
        readRequests();
        List<String> ads = new ArrayList<>();
        for (String url : requests) {
            if (isAdUrl(url)) ads.add(url);
        }
        return ads;
    }

    public List<String> getCapturedRequests() {
        readRequests();
        return new ArrayList<>(requests);
    }

    public boolean isCaptureAvailable() {
        readRequests();
        return started;
    }

    public String getCaptureFailure() {
        return captureFailure;
    }

    /** Drain older events before starting a new pre-roll/mid-roll/episode observation. */
    public void clearCapturedRequests() {
        readRequests();
        requests.clear();
        adMediaUrls.clear();
    }
}
