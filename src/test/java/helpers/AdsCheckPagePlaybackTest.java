package helpers;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.testng.Assert;
import org.testng.annotations.Test;

import pagesSunnxt.AdsCheckPage;

public class AdsCheckPagePlaybackTest {

    @Test
    public void capturesRequestThatArrivesWhileWaitingForContentToResume() throws InterruptedException {
        AtomicInteger playbackChecks = new AtomicInteger();
        AtomicInteger capturePolls = new AtomicInteger();
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class, HasCdp.class}, (proxy, method, args) -> {
                    if (method.getName().equals("executeScript")) return playbackChecks.incrementAndGet() >= 3;
                    throw new UnsupportedOperationException(method.getName());
                });
        NetworkUtils network = new NetworkUtils(driver) {
            @Override
            public List<String> getCapturedRequests() {
                capturePolls.incrementAndGet();
                if (playbackChecks.get() >= 2) {
                    return List.of("https://in-suntv.videoplaza.test/proxy/distributor/v2?tt=pa");
                }
                return List.of();
            }

            @Override
            public boolean isCaptureAvailable() {
                return true;
            }
        };
        AdsCheckPage page = new AdsCheckPage(driver, network);

        Assert.assertTrue(page.waitForContent(5));
        Assert.assertTrue(capturePolls.get() >= 3, "Keep draining network events while waiting for playback");
        Assert.assertTrue(page.isAdRequested(), "Include requests arriving after the ad first becomes visible");
    }
}
