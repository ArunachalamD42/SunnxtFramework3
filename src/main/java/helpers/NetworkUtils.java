package helpers;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.net.URLDecoder;
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
    private Path debugLogFile;
    private final Map<String, String> debugAdRequests = new HashMap<>();

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

    public void enableDebugLog(String name) {
        try {
            Path folder = Path.of("logs", "network");
            Files.createDirectories(folder);
            debugLogFile = Files.createTempFile(folder, name.replaceAll("[^a-zA-Z0-9_-]", "_") + "-", ".log");
            logger.info("Network debug file: {}", debugLogFile.toAbsolutePath());
        } catch (IOException e) {
            logger.warn("Could not create the network debug file: {}", e.getClass().getSimpleName());
        }
    }

    public Path getDebugLogFile() {
        return debugLogFile;
    }

    private String getDebugAdUrl(URI url) {
        String address = url.getScheme() + "://" + url.getHost();
        if (url.getPort() != -1) address += ":" + url.getPort();
        address += url.getRawPath();
        if (url.getRawQuery() != null) {
            List<String> allowed = List.of("rt", "pf", "cv", "t", "s", "vbw", "bp", "xpb", "st", "rnd");
            StringBuilder query = new StringBuilder();
            for (String part : url.getRawQuery().split("&")) {
                String[] pair = part.split("=", 2);
                if (query.length() > 0) query.append("&");
                query.append(pair[0]).append("=");
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                if (allowed.contains(key) && pair.length == 2) {
                    query.append(pair[1]);
                } else {
                    query.append("[redacted]");
                }
            }
            address += "?" + query;
        }
        return address;
    }

    private void writeDebugEvent(LogEntry entry, Map<?, ?> message) {
        if (debugLogFile == null) return;
        String method = (String) message.get("method");
        if (!List.of("Network.requestWillBeSent", "Network.responseReceived", "Network.loadingFinished",
                "Network.loadingFailed").contains(method)) return;
        Map<?, ?> params = (Map<?, ?>) message.get("params");
        if (params == null) return;
        String requestId = (String) params.get("requestId");
        Map<?, ?> details = (Map<?, ?>) params.get("request");
        if (details == null) details = (Map<?, ?>) params.get("response");
        if (details != null && details.get("url") instanceof String) {
            try {
                URI url = URI.create((String) details.get("url"));
                if (url.getHost() == null || !url.getHost().contains("suntv.videoplaza.")
                        || url.getPath() == null || !url.getPath().startsWith("/proxy/distributor")) return;
                debugAdRequests.put(requestId, getDebugAdUrl(url));
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        String address = debugAdRequests.get(requestId);
        if (address == null) return;
        String line = Instant.ofEpochMilli(entry.getTimestamp()) + " | " + method + " | requestId=" + requestId;
        if (details != null) {
            if (details.containsKey("method")) line += " | method=" + details.get("method");
            if (details.containsKey("status")) line += " | status=" + details.get("status");
        }
        if (params.containsKey("errorText")) line += " | error=" + params.get("errorText");
        line += " | " + address + System.lineSeparator();
        try {
            Files.writeString(debugLogFile, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.warn("Network debug file write failed: {}", e.getClass().getSimpleName());
            debugLogFile = null;
        }
        if ("Network.loadingFinished".equals(method) || "Network.loadingFailed".equals(method)) {
            debugAdRequests.remove(requestId);
        }
    }

    /** Poll regularly during playback; log reads drain ChromeDriver's event buffer. */
    private void readRequests() {
        if (!started) return;
        try {
            for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE)) {
                Map<?, ?> envelope = new Json().toType(entry.getMessage(), Map.class);
                Map<?, ?> message = (Map<?, ?>) envelope.get("message");
                if (message != null) writeDebugEvent(entry, message);
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
