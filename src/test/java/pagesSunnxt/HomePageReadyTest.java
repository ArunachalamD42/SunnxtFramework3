package pagesSunnxt;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomePageReadyTest {

    @Test
    public void waitsForHomeContentEvenWhenNavigationIsAlreadyVisible() {
        AtomicInteger contentChecks = new AtomicInteger();
        WebElement visible = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isDisplayed") || method.getName().equals("isEnabled")) return true;
                    throw new UnsupportedOperationException(method.getName());
                });
        WebElement profileHeading = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isDisplayed")) return false;
                    throw new UnsupportedOperationException(method.getName());
                });
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findElement")) {
                        if (args[0].toString().contains("Choose your profile")) return profileHeading;
                        return visible;
                    }
                    if (method.getName().equals("findElements")) {
                        if (contentChecks.incrementAndGet() < 3) return List.of();
                        return List.of(visible);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        new HomePage(driver).waitForHomePageLoaded();

        Assert.assertTrue(contentChecks.get() >= 3, "Wait for content, not only the navigation bar");
    }
}
