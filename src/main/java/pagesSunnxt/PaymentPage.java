package pagesSunnxt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Log;
import helpers.WaitUtils;

import java.time.Duration;

/**
 * PaymentPage - Handles all payment methods (Card, UPI, Net Banking).
 */
public class PaymentPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(PaymentPage.class);

    public PaymentPage(WebDriver driver) {
        super(driver);
        waitForElementToBeVisible(15, payByUpiBtn);
        logger.info("Payment page loaded");
    }

    // ==================== PAYMENT METHOD BUTTONS ====================

    @FindBy(xpath = "(//div[text()='Pay by UPI'])[1]")
    private WebElement payByUpiBtn;

    @FindBy(xpath = "(//div[text()='Credit / Debit / ATM Card'])[1]")
    private WebElement payByCardBtn;

    @FindBy(xpath = "(//div[text()='Net Banking'])[1]")
    private WebElement payByNetBankingBtn;

    @FindBy(xpath = "//span[@class='text-base']")
    private List<WebElement> netBankingPartners;

    // ==================== BILLING ====================

    @FindBy(xpath = "(//span[text()='Choose State'])[1]")
    private WebElement billingState;

    @FindBy(xpath = "(//div[text()='Andaman and Nicobar Islands'])[1]")
    private WebElement andamanState;

    // ==================== CARD FIELDS ====================

    @FindBy(xpath = "(//input[@placeholder='Enter 16 digit Card Number'])[1]")
    private WebElement cardNumberField;

    @FindBy(xpath = "(//input[@placeholder='MM / YY'])[1]")
    private WebElement validTillField;

    @FindBy(xpath = "(//input[@placeholder='CVV'])[1]")
    private WebElement cvvField;

    @FindBy(xpath = "(//input[@placeholder='Name on Card'])[1]")
    private WebElement nameOnCardField;

    @FindBy(xpath = "(//button[text()='Continue'])[1]")
    private WebElement continueBtn;

    // ==================== UPI FIELDS ====================

    @FindBy(xpath = "(//input[@placeholder='upi-id@bankname'])[1]")
    private WebElement upiIdField;

    @FindBy(xpath = "(//button[text()='Pay'])[1]")
    private WebElement payBtn;

    @FindBy(xpath = "(//span[text()='Show QR Code'])[1]")
    private WebElement showQRCode;

    @FindBy(xpath = "(//p[text()='Enter a valid UPI ID'])[1]")
    private WebElement upiError;

    // ==================== COUPON ====================

    @FindBy(xpath = "(//input[@placeholder='Apply Coupon'])[1]")
    private WebElement couponField;

    @FindBy(xpath = "(//button[text()='Apply Code'])[1]")
    private WebElement applyCodeBtn;

    // ==================== SUCCESS ====================

    @FindBy(xpath = "//h1[text()='Your Payment is Successful!']")
    private WebElement paymentSuccessMsg;

    @FindBy(xpath = "//button[text()='Proceed']")
    private WebElement proceedBtn;

    // ==================== CARD PAYMENT ====================

    /**
     * Fills in card payment details.
     */
    public void payUsingCard() {
        waitForElementToBeClickable(3, payByCardBtn);
        billingState.click();
        andamanState.click();
        payByCardBtn.click();

        waitForElementToBeClickable(3, cardNumberField);
        cardNumberField.sendKeys("5555555555554444");
        validTillField.sendKeys("0355");
        cvvField.sendKeys("123");
        nameOnCardField.sendKeys("Arun");
        logger.info("Card details entered");
    }

    // ==================== NET BANKING ====================

    /**
     * Selects Net Banking and picks a random banking partner.
     */
    public RazorpayPage payUsingNetBanking() {
        waitForElementToBeClickable(5, payByNetBankingBtn);
        payByNetBankingBtn.click();
        logger.info("Net Banking selected");

        waitForElementToBeVisible(5, netBankingPartners.get(0));

        List<String> partnersCollection = new ArrayList<>();
        for (WebElement partner : netBankingPartners) {
            String partnerName = (String) js.executeScript("return arguments[0].textContent;", partner);
            partnersCollection.add(partnerName.trim());
        }

        int partnerIndex = 0;
        if (!partnersCollection.isEmpty()) {
            partnerIndex = new Random().nextInt(partnersCollection.size());
        }

        String selectedPartner = partnersCollection.get(partnerIndex);
        logger.info("Selecting Net Banking partner: {}", selectedPartner);

        if (!selectedPartner.isEmpty()) {
            String xpath = "//span[normalize-space()='" + selectedPartner + "']";
            WebElement targetPartner = driver.findElement(By.xpath(xpath));
            scrollIntoView(targetPartner);
            waitForElementToBeClickable(10, targetPartner);
            targetPartner.click();
        }

        return new RazorpayPage(driver);
    }

    // ==================== COMPLETE PAYMENT ====================

    /**
     * Clicks Continue and switches to Razorpay popup window.
     */
    public RazorpayPage completePayment(String originalWindowHandle) {
        waitForElementToBeClickable(5, continueBtn);
        continueBtn.click();
        logger.info("Continue clicked - switching to payment window");

        switchToNewWindow(originalWindowHandle, 20);
        return new RazorpayPage(driver);
    }

    // ==================== WINDOW SWITCHING ====================

    public String switchToNewWindow(String originalWindow, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window found"));

        driver.switchTo().window(newWindow);
        logger.info("Switched to new window: {}", driver.getTitle());
        return newWindow;
    }

    public void switchToWindow(String windowHandle) {
        driver.switchTo().window(windowHandle);
        logger.info("Switched back to window: {}", windowHandle);
    }

    // ==================== POST-PAYMENT ====================

    public boolean isPaymentSuccessfulMsgShown() {
        waitForElementToBeVisible(5, paymentSuccessMsg);
        return paymentSuccessMsg.isDisplayed();
    }

    public void clickProceedBtn() {
        waitForElementToBeClickable(5, proceedBtn);
        proceedBtn.click();
        logger.info("Proceed button clicked after payment");
    }
}
