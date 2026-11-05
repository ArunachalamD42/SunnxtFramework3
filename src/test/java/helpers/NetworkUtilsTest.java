package helpers;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NetworkUtilsTest {

    @Test
    public void noTrafficIsNotProofOfAnAdFreeObservation() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        pagesSunnxt.AdsPage page = new pagesSunnxt.AdsPage(stub.driver, network);
        Assert.assertFalse(page.isNetworkCaptureAvailable());
        stub.request("https://example.test/content");
        Assert.assertTrue(page.isNetworkCaptureAvailable());
        stub.failReads = true;
        Assert.assertFalse(page.isNetworkCaptureAvailable());
    }

    @Test
    public void capturesPulseRequestsAndIgnoresOrdinaryMediaAndUrlQueryMatches() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.request("https://suntv.videoplaza.tv/proxy/distributor");
        stub.request("https://media.example.test/movie.mp4");
        stub.request("https://example.test/api?next=suntv.videoplaza.tv");
        Assert.assertEquals(network.getCapturedRequests().size(), 3);
        Assert.assertEquals(network.getAdRequests(), List.of("https://suntv.videoplaza.tv/proxy/distributor"));
        Assert.assertTrue(network.isCaptureAvailable());
    }

    @Test
    public void recognizesOnlyTheExactCreativeIdentifiedByThePulseVideo() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.request("https://cdn.example.test/creative.mp4");
        stub.request("https://cdn.example.test/movie.mp4");
        Assert.assertFalse(network.isAdRequested());
        // DOM identification can arrive after the request event, within the same window.
        network.registerAdMediaUrl("https://cdn.example.test/creative.mp4");
        Assert.assertEquals(network.getAdRequests(), List.of("https://cdn.example.test/creative.mp4"));
    }

    @Test
    public void observationResetDoesNotCountOldOrBufferedAdRequests() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.request("https://suntv.videoplaza.tv/old-ad");
        network.clearCapturedRequests();
        Assert.assertFalse(network.isAdRequested());
        Assert.assertTrue(network.getCapturedRequests().isEmpty());
        stub.request("https://suntv.videoplaza.tv/new-ad");
        Assert.assertEquals(network.getAdRequests(), List.of("https://suntv.videoplaza.tv/new-ad"));
    }

    @Test
    public void logReadFailureInvalidatesCaptureEvenAfterEarlierSuccess() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.request("https://example.test/content");
        Assert.assertEquals(network.getCapturedRequests().size(), 1);
        stub.failReads = true;
        Assert.assertFalse(network.isCaptureAvailable());
        Assert.assertTrue(network.getCaptureFailure().contains("read failed"));
    }

    @Test
    public void malformedNetworkEventInvalidatesCapture() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.pending.add(new LogEntry(Level.INFO, 0, "not-json"));
        Assert.assertFalse(network.isCaptureAvailable());
    }

    @Test
    public void unavailablePerformanceLogCannotStartCapture() {
        NetworkStub stub = new NetworkStub();
        stub.logTypes = Set.of();
        NetworkUtils network = new NetworkUtils(stub.driver);
        Assert.expectThrows(IllegalStateException.class, network::startCapture);
        Assert.assertFalse(network.isCaptureAvailable());
    }

    @Test
    public void unrelatedPerformanceEventsAreNotRequests() {
        NetworkStub stub = new NetworkStub();
        NetworkUtils network = new NetworkUtils(stub.driver);
        network.startCapture();
        stub.pending.add(new LogEntry(Level.INFO, 0,
                new Json().toJson(Map.of("message", Map.of("method", "Network.loadingFinished")))));
        Assert.assertTrue(network.getCapturedRequests().isEmpty());
        Assert.assertTrue(network.isCaptureAvailable());
    }

    private static class NetworkStub implements Logs {
        final List<LogEntry> pending = new ArrayList<>();
        Set<String> logTypes = Set.of(LogType.PERFORMANCE);
        boolean failReads;
        final WebDriver driver;

        NetworkStub() {
            WebDriver.Options options = (WebDriver.Options) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[] {WebDriver.Options.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("logs")) return this;
                        throw new UnsupportedOperationException(method.getName());
                    });
            driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {WebDriver.class, HasCdp.class, org.openqa.selenium.JavascriptExecutor.class}, (proxy, method, args) -> {
                        if (method.getName().equals("manage")) return options;
                        if (method.getName().equals("executeCdpCommand")) return Map.of();
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        void request(String url) {
            Map<String, Object> event = Map.of("message", Map.of("method", "Network.requestWillBeSent",
                    "params", Map.of("request", Map.of("url", url))));
            pending.add(new LogEntry(Level.INFO, 0, new Json().toJson(event)));
        }

        @Override
        public LogEntries get(String type) {
            if (failReads) throw new WebDriverException("Simulated log read failure");
            LogEntries result = new LogEntries(new ArrayList<>(pending));
            pending.clear();
            return result;
        }

        @Override
        public Set<String> getAvailableLogTypes() {
            return logTypes;
        }
    }
}
