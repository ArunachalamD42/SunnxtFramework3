package pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * RazorpayPage - Handles the Razorpay payment gateway popup/window.
 */
public class RazorpayPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(RazorpayPage.class);

    public RazorpayPage(WebDriver driver) {
        super(driver);
        waitForElementToBeVisible(15, razorpayHeader);
        logger.info("Razorpay page loaded");
    }

    @FindBy(id = "submit-action")
    private WebElement secureAndContinueBtn;

    @FindBy(xpath = "//div[text()='Keep card saved for future payments']")
    private WebElement razorpayHeader;

    @FindBy(xpath = "//button[text()='Success']")
    private WebElement successButton;

    @FindBy(xpath = "//button[text()='Failure']")
    private WebElement failureButton;

    /**
     * Completes the payment successfully via the Razorpay test gateway.
     */
    public void handleSuccessPayment() {
        waitForElementToBeClickable(10, secureAndContinueBtn);
        secureAndContinueBtn.click();
        logger.info("Secure & Continue clicked");

        waitForElementToBeClickable(10, successButton);
        successButton.click();
        logger.info("Payment success confirmed");
    }

    /**
     * Simulates a failed payment via the Razorpay test gateway.
     */
    public void handleFailurePayment() {
        waitForElementToBeClickable(10, secureAndContinueBtn);
        secureAndContinueBtn.click();
        logger.info("Secure & Continue clicked");

        waitForElementToBeClickable(10, failureButton);
        failureButton.click();
        logger.info("Payment failure simulated");
    }
}
