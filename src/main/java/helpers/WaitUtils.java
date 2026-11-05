package helpers;

import java.time.Duration;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Log;

/**
 * WaitUtils - Base class for all Page Objects.
 * Provides wait, scroll, and interaction utilities.
 */
public class WaitUtils {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected Actions act;

    protected static final Logger logger = Log.getLogger(WaitUtils.class);

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    // ==================== WAIT METHODS ====================

    public void waitForElementToBeClickable(int seconds, WebElement element) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForElementToBeClickable(int seconds, By locator) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForElementToBeVisible(int seconds, WebElement element) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementToBeVisible(int seconds, By locator) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForTextToBePresent(int seconds, WebElement element, String expectedText) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(ExpectedConditions.textToBePresentInElement(element, expectedText));
    }

    public void waitForDropdownOptions(WebElement dropdownElement, int minimumOptions, int timeoutSeconds) {
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        wait.until(d -> {
            Select select = new Select(dropdownElement);
            return select.getOptions().size() >= minimumOptions;
        });
    }

    public void fluentWait(int timeoutSeconds, int pollingMillis) {
        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(pollingMillis))
                .ignoring(NoSuchElementException.class);
    }

    public void implicitWait(int seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    // ==================== ELEMENT STATE CHECKS ====================

    public boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | org.openqa.selenium.StaleElementReferenceException e) {
            return false;
        }
    }

    public boolean isElementVisible(int seconds, WebElement element) {
        try {
            waitForElementToBeVisible(seconds, element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== SCROLL METHODS ====================

    public void scrollPageWithLoad() {
        long previousHeight = 0;
        int bottomChecks = 0;
        long endTime = System.nanoTime() + java.time.Duration.ofSeconds(120).toNanos();

        while (System.nanoTime() < endTime) {
            js.executeScript("window.scrollBy(0,300)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Page scrolling was interrupted", e);
            }

            long pageHeight = ((Number) js.executeScript(
                    "return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);")).longValue();
            boolean atBottom = Boolean.TRUE.equals(js.executeScript(
                    "return Math.ceil(window.scrollY + window.innerHeight) >= "
                    + "Math.max(document.body.scrollHeight, document.documentElement.scrollHeight) - 2;"));

            if (atBottom && pageHeight == previousHeight) {
                bottomChecks++;
            } else {
                bottomChecks = 0;
            }
            previousHeight = pageHeight;

            if (bottomChecks == 3) {
                logger.info("Page scrolling completed");
                return;
            }
        }
        throw new org.openqa.selenium.TimeoutException("Page scrolling did not finish within 120 seconds");
    }

    public void scrollIntoView(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public void scrollByPixel() {
        js.executeScript("window.scrollBy(0,300);");
    }

    public void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }

    // ==================== ACTIONS METHODS ====================

    public void moveElement(WebElement element) {
        act = new Actions(driver);
        act.moveToElement(element).build().perform();
    }

    public void contextClick(WebElement element) {
        act = new Actions(driver);
        act.contextClick(element).build().perform();
    }
}
