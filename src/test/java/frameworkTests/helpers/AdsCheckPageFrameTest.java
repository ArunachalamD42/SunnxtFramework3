package frameworkTests.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pagesSunnxt.AdsCheckPage;

public class AdsCheckPageFrameTest {

    @Test
    public void retriesAReplacedFrameDuringResetAndReturnsToMainPage() throws Exception {
        FrameStub stub = new FrameStub(1);
        Assert.assertFalse(scan(stub, true));
        Assert.assertEquals(stub.switches.get(), 2);
        Assert.assertEquals(stub.resets.get(), 1);
        Assert.assertEquals(stub.depth.get(), 0);
    }

    @Test
    public void retriesAReplacedFrameBeforeDetectingItsAdRequest() throws Exception {
        FrameStub stub = new FrameStub(1);
        Assert.assertTrue(scan(stub, false));
        Assert.assertEquals(stub.switches.get(), 2);
        Assert.assertEquals(stub.depth.get(), 0);
    }

    @Test
    public void persistentFrameReplacementFailsInsteadOfPassingTheCheck() throws Exception {
        FrameStub stub = new FrameStub(10);
        try {
            scan(stub, true);
            Assert.fail("A request reset that never completes must fail");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
            Assert.assertTrue(e.getCause().getMessage().contains("after 3 attempts"));
        }
        Assert.assertEquals(stub.switches.get(), 3);
        Assert.assertEquals(stub.depth.get(), 0);
        Assert.assertEquals(stub.resets.get(), 0);
    }

    private boolean scan(FrameStub stub, boolean reset) throws Exception {
        AdsCheckPage page = new AdsCheckPage(stub.driver);
        var method = AdsCheckPage.class.getDeclaredMethod("checkFrameAdRequests", boolean.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(page, reset);
    }

    private static class FrameStub {
        final AtomicInteger switches = new AtomicInteger();
        final AtomicInteger resets = new AtomicInteger();
        final AtomicInteger depth = new AtomicInteger();
        final WebDriver driver;

        FrameStub(int failures) {
            AtomicReference<WebDriver> currentDriver = new AtomicReference<>();
            WebElement frame = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    });
            WebDriver.TargetLocator target = (WebDriver.TargetLocator) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[] {WebDriver.TargetLocator.class}, (proxy, method, args) -> {
                        if (method.getName().equals("defaultContent")) {
                            depth.set(0);
                            return currentDriver.get();
                        }
                        if (method.getName().equals("parentFrame")) {
                            depth.decrementAndGet();
                            return currentDriver.get();
                        }
                        if (method.getName().equals("frame")) {
                            if (switches.incrementAndGet() <= failures) {
                                throw new StaleElementReferenceException("Ad iframe was replaced");
                            }
                            depth.incrementAndGet();
                            return currentDriver.get();
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                        if (method.getName().equals("switchTo")) return target;
                        if (method.getName().equals("findElements")) return depth.get() == 0 ? List.of(frame) : List.of();
                        if (method.getName().equals("executeScript")) {
                            if (((String) args[0]).contains("clearResourceTimings")) {
                                resets.incrementAndGet();
                                return null;
                            }
                            return true;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
            currentDriver.set(driver);
        }
    }
}
