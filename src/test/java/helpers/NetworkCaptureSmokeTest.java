package helpers;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpServer;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pagesSunnxt.AdsPage;

/** Opt-in headless Chrome test against a temporary local server. No SunNXT account. */
public class NetworkCaptureSmokeTest {

    @Test
    public void capturesNetworkAndHiddenPulseCreativeInChrome() throws Exception {
        if (!Boolean.getBoolean("runNetworkSmoke")) {
            throw new SkipException("Opt-in: set runNetworkSmoke=true and network.smoke.driver");
        }
        String driverPath = System.getProperty("network.smoke.driver");
        Assert.assertTrue(driverPath != null && new File(driverPath).isFile(), "Supply a local ChromeDriver executable");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            boolean page = exchange.getRequestURI().getPath().equals("/");
            String body = page ? "<!doctype html><html><body>Network smoke fixture</body></html>" : "fixture";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", page ? "text/html" : "application/octet-stream");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        ChromeDriver driver = null;
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(driverPath)).build();
        server.start();
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-first-run", "--disable-background-networking");
            LoggingPreferences logs = new LoggingPreferences();
            logs.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", logs);
            options.setExperimentalOption("perfLoggingPrefs", Map.of("enableNetwork", true, "enablePage", false));
            driver = new ChromeDriver(service, options);
            NetworkUtils network = new NetworkUtils(driver);
            network.startCapture();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            driver.get(base + "/");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !network.getCapturedRequests().isEmpty());
            Assert.assertTrue(network.isCaptureAvailable());
            Assert.assertFalse(network.isAdRequested(), "Ordinary page requests must not be classified as ads");

            AdsPage ads = new AdsPage(driver, network);
            ads.startAdObservation();
            driver.executeScript("var v=document.createElement('video');v.id='pulseAdPlayerHTML5_smoke';"
                    + "v.style.display='none';v.preload='auto';v.src=arguments[0];document.body.appendChild(v);v.load();",
                    base + "/ad-creative.mp4");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                ads.checkAds();
                return ads.wasAdRequested();
            });
            Assert.assertFalse(ads.wasAdVisible(), "Hidden creative must not be reported as a visible ad");
            Assert.assertTrue(ads.isNetworkCaptureAvailable());
            Assert.assertTrue(network.getAdRequests().contains(base + "/ad-creative.mp4"));

            ads.startAdObservation();
            driver.executeAsyncScript("var done=arguments[arguments.length-1];fetch('/content-segment').then(done).catch(done);");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                ads.checkAds();
                return !network.getCapturedRequests().isEmpty();
            });
            Assert.assertFalse(ads.wasAdRequested(), "Previous observation's creative request must not leak into this one");
            Assert.assertTrue(ads.isNetworkCaptureAvailable());
            System.out.println("Local capture smoke PASSED with Chrome " + driver.getCapabilities().getBrowserVersion());
        } finally {
            try {
                if (driver != null) driver.quit();
            } finally {
                service.stop();
                server.stop(0);
            }
        }
    }
}
