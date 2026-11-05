package pagesSunnxt;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * SubscriptionPage - Handles subscription plan selection and navigation to payment.
 */
public class SubscriptionPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(SubscriptionPage.class);

    private By proceedButtonLocator = By.xpath("(//div[@class='flex flex-col items-center justify-end'])[1]");

    public SubscriptionPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//div[@class='relative group inline-block']")
    private WebElement subscriptionBtn;

    @FindBy(xpath = "(//div[@class='flex flex-col items-center justify-end'])[1]")
    private WebElement premiumPlan;

    @FindBy(xpath = "(//div[@class='flex flex-col items-center justify-end'])[2]")
    private WebElement saverPlan;

    @FindBy(xpath = "(//div[@class='flex flex-col items-center justify-end'])[3]")
    private WebElement litePlan;

    @FindBy(xpath = "//button[text()='Proceed']")
    private WebElement proceedBtn;

    @FindBy(xpath = "//p[text()='Active Plan - ']")
    private WebElement activePlan;

    @FindBy(xpath = "//button[text()='Manage Auto-Renewal']")
    private WebElement manageAutoRenewal;

    @FindBy(xpath = "//button[text()='Keep Subscription']")
    private WebElement keepSubscription;

    @FindBy(xpath = "//button[text()='Confirm Cancellation']")
    private WebElement cancelSubscription;

    /**
     * Selects a subscription plan and navigates to PaymentPage.
     */
    public PaymentPage navigateToPaymentPage(String plan) {
        waitForElementToBeClickable(5, subscriptionBtn);
        subscriptionBtn.click();
        logger.info("Subscription button clicked");

        if (plan.equalsIgnoreCase("premium")) {
            waitForElementToBeClickable(5, premiumPlan);
            premiumPlan.click();
            logger.info("Premium plan selected");
        } else if (plan.equalsIgnoreCase("saver")) {
            waitForElementToBeClickable(10, saverPlan);
            saverPlan.click();
            logger.info("Saver plan selected");
        } else if (plan.equalsIgnoreCase("lite")) {
            waitForElementToBeClickable(5, litePlan);
            litePlan.click();
            logger.info("Lite plan selected");
        } else {
            logger.warn("Plan '{}' not listed", plan);
        }

        waitForElementToBeClickable(10, proceedButtonLocator);
        proceedBtn.click();
        logger.info("Proceed button clicked");

        return new PaymentPage(driver);
    }

    /**
     * Upgrades or selects an upcoming plan (used when user already has an active plan).
     */
    public PaymentPage upgradeAndUpcoming(String plan) {
        if (plan.equalsIgnoreCase("premium")) {
            waitForElementToBeClickable(5, premiumPlan);
            premiumPlan.click();
        } else if (plan.equalsIgnoreCase("saver")) {
            waitForElementToBeClickable(5, saverPlan);
            saverPlan.click();
        } else if (plan.equalsIgnoreCase("lite")) {
            waitForElementToBeClickable(5, litePlan);
            litePlan.click();
        } else {
            logger.warn("Plan '{}' not listed", plan);
        }

        waitForElementToBeClickable(10, proceedBtn);
        proceedBtn.click();
        logger.info("Upgrade proceed clicked for plan: {}", plan);

        return new PaymentPage(driver);
    }
}
