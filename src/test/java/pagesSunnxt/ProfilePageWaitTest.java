package pagesSunnxt;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Simulates a heading arriving before the profile tile; no browser or credentials. */
public class ProfilePageWaitTest {

    @Test
    public void waitsForDelayedProfileBeforeCheckingItsLock() {
        AtomicInteger attempts = new AtomicInteger();
        WebElement tile = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("isDisplayed")) return true;
                    if (method.getName().equals("findElements")) return List.of();
                    throw new UnsupportedOperationException(method.getName());
                });
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findElement")) {
                        if (attempts.incrementAndGet() < 3) {
                            throw new NoSuchElementException("Profile tile is still loading");
                        }
                        return tile;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        ProfilePage page = new ProfilePage(driver);
        Assert.assertFalse(page.isProfileLocked(1));
        Assert.assertEquals(attempts.get(), 3, "Profile lookup should wait through transient absence");
    }
}
