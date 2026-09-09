package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v133.network.Network;
import org.openqa.selenium.devtools.v133.network.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetworkUtils - Captures network requests via Chrome DevTools Protocol.
 * Useful for verifying ad requests, API calls, and analytics during playback.
 */
public class NetworkUtils {

    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

    private final DevTools devTools;
    private final List<String> adRequests = new ArrayList<>();
    private boolean started = false;

    public NetworkUtils(WebDriver driver) {
        if (!(driver instanceof HasDevTools)) {
            throw new IllegalArgumentException("Driver does not support DevTools. Use ChromeDriver.");
        }
        this.devTools = ((HasDevTools) driver).getDevTools();
    }

    public void startCapture() {
        if (started) {
            log.warn("Network capture already started.");
            return;
        }

        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        Predicate<String> adPredicate = url -> {
            if (url == null) return false;
            String u = url.toLowerCase();
            return u.contains("suntv.videoplaza");
        };

        devTools.addListener(Network.requestWillBeSent(), event -> {
            Request req = event.getRequest();
            String url = req.getUrl();
            if (adPredicate.test(url)) {
                log.info("[NetworkUtils] Ad request detected: {}", url);
                synchronized (adRequests) {
                    adRequests.add(url);
                }
            }
        });

        started = true;
        log.info("Network capture started (DevTools session created).");
    }

    public boolean isAdRequested() {
        synchronized (adRequests) {
            return !adRequests.isEmpty();
        }
    }

    public List<String> getAdRequests() {
        synchronized (adRequests) {
            return new ArrayList<>(adRequests);
        }
    }

    public void clearCapturedRequests() {
        synchronized (adRequests) {
            adRequests.clear();
        }
    }
}
