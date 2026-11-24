package frameworkTests.helpers;

import helpers.WaitUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WaitUtilsScrollTest {

    @Test
    public void waitsForDelayedCarouselsEvenWhenPageHeightIsStable() {
        AtomicInteger scrolls = new AtomicInteger();
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                    if (method.getName().equals("executeScript")) {
                        String script = (String) args[0];
                        if (script.startsWith("window.scrollBy")) {
                            scrolls.incrementAndGet();
                            return null;
                        }
                        if (script.startsWith("return Math.max")) return 1000L;
                        if (script.startsWith("return Math.ceil")) return true;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        List<WebElement> carousels = new java.util.AbstractList<>() {
            public WebElement get(int index) {
                throw new UnsupportedOperationException("Only the number of rows is observed");
            }

            public int size() {
                if (scrolls.get() < 3) return 5;
                if (scrolls.get() < 6) return 10;
                return 15;
            }
        };

        new WaitUtils(driver).scrollPageWithLoad(carousels);

        Assert.assertTrue(scrolls.get() >= 11,
                "A stable page height must not finish scrolling while new carousel rows are arriving");
        Assert.assertEquals(carousels.size(), 15);
    }
}
