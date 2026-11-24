package frameworkTests.pagesSunnxt;

import pagesSunnxt.HomePage;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomePageNavigationTest {

    @Test
    public void acceptsTvShowsWhenDestinationIsAlreadyOpen() {
        WebElement tab = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isDisplayed") || method.getName().equals("isEnabled")) return true;
                    if (method.getName().equals("click")) return null;
                    throw new UnsupportedOperationException(method.getName());
                });
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findElement")) return tab;
                    if (method.getName().equals("executeScript") || method.getName().equals("getCurrentUrl")) {
                        return "https://example.test/shows";
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        new HomePage(driver).navigateToTvShows();
    }

    @Test
    public void waitsForTvShowsDestinationBeforeReturning() {
        AtomicBoolean clicked = new AtomicBoolean();
        AtomicInteger navigationChecks = new AtomicInteger();
        WebElement tab = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isDisplayed") || method.getName().equals("isEnabled")) return true;
                    if (method.getName().equals("click")) {
                        clicked.set(true);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findElement")) return tab;
                    if (method.getName().equals("executeScript")) return "https://example.test/shows";
                    if (method.getName().equals("getCurrentUrl")) {
                        if (!clicked.get() || navigationChecks.incrementAndGet() < 3) {
                            return "https://example.test/movies";
                        }
                        return "https://example.test/shows";
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        new HomePage(driver).navigateToTvShows();

        Assert.assertTrue(clicked.get());
        Assert.assertTrue(navigationChecks.get() >= 3, "Do not proceed while the previous catalogue is still open");
        Assert.assertEquals(driver.getCurrentUrl(), "https://example.test/shows");
    }
}
