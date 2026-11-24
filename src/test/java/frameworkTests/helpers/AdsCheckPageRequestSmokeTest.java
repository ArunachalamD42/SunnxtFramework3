package frameworkTests.helpers;

import helpers.NetworkUtils;

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
import pagesSunnxt.AdsCheckPage;

/** Local request fixture only; no SunNXT account or CDP capture. */
public class AdsCheckPageRequestSmokeTest {

    @Test
    public void detectsFetchAndXhrAndResetsBetweenAdBreaks() throws Exception {
        if (!Boolean.getBoolean("runAdsRequestSmoke")) {
            throw new SkipException("Opt-in: runAdsRequestSmoke=true and ads.smoke.driver");
        }
        String driverPath = System.getProperty("ads.smoke.driver");
        Assert.assertTrue(driverPath != null && new File(driverPath).isFile(), "Supply a local ChromeDriver");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String html = "<!doctype html><html><body>Local ad request fixture</body></html>";
            if (exchange.getRequestURI().getPath().equals("/frame")) {
                html = "<!doctype html><script>fetch('http://in-suntv.videoplaza.test:"
                        + server.getAddress().getPort() + "/proxy/distributor/v2?break=frame').catch(function() {});</script>";
            }
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        ChromeDriver driver = null;
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(driverPath))
                .withTimeout(Duration.ofSeconds(120))
                .withLogFile(new File("target/ads-request-smoke-driver.log")).build();
        server.start();
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-first-run", "--disable-background-networking",
                    "--no-proxy-server", "--host-resolver-rules=MAP in-suntv.videoplaza.test 127.0.0.1");
            LoggingPreferences logs = new LoggingPreferences();
            logs.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", logs);
            options.setExperimentalOption("perfLoggingPrefs", Map.of("enableNetwork", true, "enablePage", false));
            driver = new ChromeDriver(service, options);
            driver.get("http://in-suntv.videoplaza.test:" + server.getAddress().getPort() + "/");
            AdsCheckPage page = new AdsCheckPage(driver);
            page.clearAdRequests();
            Assert.assertFalse(page.isAdRequested());

            driver.executeScript("window.fetchDone=false; fetch('/ordinary').then(function() {window.fetchDone=true;});");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    Boolean.TRUE.equals(((ChromeDriver) d).executeScript("return window.fetchDone;")));
            Assert.assertFalse(page.isAdRequested(), "Ordinary traffic is not an ad request");

            driver.executeScript("window.fetchDone=false; fetch('/proxy/distributor/v2?break=pre')"
                    + ".then(function() {window.fetchDone=true;});");
            Assert.assertTrue(page.isAdRequested(), "Fetch request must be remembered");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    Boolean.TRUE.equals(((ChromeDriver) d).executeScript("return window.fetchDone;")));
            driver.executeScript("performance.clearResourceTimings();");
            Assert.assertTrue(page.isAdRequested(), "Clearing browser timing entries must not lose the request");

            page.clearAdRequests();
            Assert.assertFalse(page.isAdRequested(), "Pre-roll must not count as mid-roll");
            driver.executeScript("var xhr=new XMLHttpRequest(); xhr.open('GET','/proxy/distributor/v2?break=mid'); xhr.send();");
            Assert.assertTrue(page.isAdRequested(), "A fresh mid-roll XHR must be detected");

            NetworkUtils network = new NetworkUtils(driver);
            network.startCapture();
            AdsCheckPage browserPage = new AdsCheckPage(driver, network);
            browserPage.clearAdRequests();
            driver.executeScript("window.frameLoaded=false; var frame=document.createElement('iframe');"
                    + "frame.onload=function(){window.frameLoaded=true;}; frame.src=arguments[0]; document.body.appendChild(frame);",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/frame");
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    Boolean.TRUE.equals(((ChromeDriver) d).executeScript("return window.frameLoaded;")));
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .withMessage("Browser capture must detect the child-frame ad request")
                        .until(d -> browserPage.isAdRequested());
            } catch (org.openqa.selenium.TimeoutException e) {
                driver.switchTo().frame(0);
                System.out.println("Local frame diagnostics: " + driver.executeScript(
                        "return {url:location.href, resources:performance.getEntriesByType('resource').map(function(e){return e.name;}), body:document.documentElement.outerHTML};"));
                driver.switchTo().defaultContent();
                System.out.println("Local browser diagnostics: " + driver.manage().logs().get(LogType.BROWSER).getAll());
                throw e;
            }

            browserPage.clearAdRequests();
            driver.executeScript("window.fetchDone=false; fetch('/ordinary?after=reset')"
                    + ".then(function(){window.fetchDone=true;});");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    Boolean.TRUE.equals(((ChromeDriver) d).executeScript("return window.fetchDone;")));
            Assert.assertFalse(browserPage.isAdRequested(), "Reset must exclude the previous ad break");

            // A controlled range input models a player that reacts only to genuine UI events.
            driver.executeScript("document.body.innerHTML='<style>input[type=range]{appearance:none;padding:0;border:0;}"
                    + "input[type=range]::-webkit-slider-thumb{appearance:none;width:0;height:4px;}"
                    + "</style><video style=\"width:600px;height:200px\"></video>'"
                    + "+'<input type=\"range\" aria-label=\"Seek\" min=\"0\" max=\"3600\" step=\"any\" value=\"10\" style=\"width:600px\">';"
                    + "var video=document.querySelector('video'); window.fixturePosition=10; window.uiSeekEvents=0; window.uiSeekTrusted=false;"
                    + "window.seekClicks=0; window.seekDragged=false;"
                    + "document.querySelector('input').addEventListener('click',function(){window.seekClicks++;});"
                    + "document.querySelector('input').addEventListener('pointermove',function(event){if(event.buttons) window.seekDragged=true;});"
                    + "Object.defineProperty(video,'readyState',{get:function(){return 4;}});"
                    + "Object.defineProperty(video,'currentTime',{get:function(){return window.fixturePosition;},set:function(value){window.fixturePosition=value;}});"
                    + "Object.defineProperty(video,'seeking',{get:function(){return false;}});"
                    + "document.querySelector('input').addEventListener('change',function(event){"
                    + "window.uiSeekEvents++; window.uiSeekTrusted=event.isTrusted; video.currentTime=Number(this.value);"
                    + "fetch('/proxy/distributor/v2?break=mouse').catch(function(){});});");
            for (int target : new int[] {900, 1800}) {
                page.clearAdRequests();
                driver.executeScript("window.seekClicks=0; window.seekDragged=false;");
                page.seekTo(target);
                Assert.assertEquals(((Number) driver.executeScript("return window.seekClicks;")).intValue(), 1,
                        "Seeking must use exactly one click");
                Assert.assertFalse(Boolean.TRUE.equals(driver.executeScript("return window.seekDragged;")),
                        "Seeking must not drag the timeline");
                Assert.assertTrue(Boolean.TRUE.equals(driver.executeScript("return window.uiSeekTrusted;")),
                        "Seeking must generate a trusted browser UI event");
                Assert.assertTrue(Math.abs(((Number) driver.executeScript("return window.fixturePosition;")).doubleValue() - target) <= 12);
                Assert.assertTrue(page.isAdRequested(), "Mouse seeking must invoke the fixture's player event handler");
            }
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
