package pagesSunnxt;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * LoginPage - Handles the NEW 2-step login flow (from Framework 1 pattern).
 * Step 1: Enter email/mobile -> Click Continue
 * Step 2: Enter password -> Click Log In
 */
public class LoginPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(LoginPage.class);

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ==================== STEP 1: EMAIL/MOBILE ====================

    @FindBy(tagName = "h3")
    private WebElement loginHeader;

    @FindBy(name = "email")
    private WebElement emailField;

    @FindBy(xpath = "//button[text()='Continue']")
    private WebElement continueButton;

    // ==================== STEP 2: PASSWORD ====================

    @FindBy(xpath = "//span[text()='Enter your password to continue']")
    private WebElement passwordHeader;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//button[text()='Log In']")
    private WebElement loginButton;

    @FindBy(xpath = "//a[text()='Forgot Password?']")
    private WebElement forgotPasswordLink;

    // ==================== VALIDATION MESSAGES ====================

    @FindBy(xpath = "//a[text()='Please Enter a valid 10-digit Mobile Number']")
    private WebElement invalidMobileError;

    @FindBy(xpath = "//a[text()='Please enter a valid Email ID']")
    private WebElement invalidEmailError;

    @FindBy(xpath = "//span[text()=\"Incorrect password. Try again or use 'Forgot Password'\"]")
    private WebElement incorrectPasswordError;

    // ==================== DEVICE LIMIT ====================

    @FindBy(xpath = "//h2[text()='Device Limit Reached']")
    private WebElement deviceLimitHeader;

    @FindBy(xpath = "//button[text()='Add Device']")
    private WebElement addDeviceButton;

    // ==================== LOGIN ACTIONS ====================

    public void enterLoginId(String loginId) {
        waitForElementToBeClickable(5, emailField);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        emailField.sendKeys(loginId);
        logger.info("Login ID entered: {}", loginId);
    }

    public void clickContinueButton() {
        if (isContinueButtonEnabled()) {
        	waitForElementToBeClickable(20, continueButton);
            continueButton.click();
            logger.info("Continue button clicked");
        } else {
            logger.warn("Continue button is not enabled");
        }
    }

    public void waitForPasswordScreen() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (isPasswordFieldVisible()) {
                logger.info("Password screen displayed");
                return;
            }

            if (attempt < 3) {
                try {
                    if (continueButton.isDisplayed() && continueButton.isEnabled()) {
                        logger.warn("Password screen not displayed. Retrying Continue, attempt {} of 3", attempt + 1);
                        clickContinueButton();
                    } else {
                        logger.info("Continue is unavailable. Waiting for the password screen, attempt {} of 3", attempt + 1);
                    }
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    logger.info("Login screen changed. Waiting for the password field");
                }
            }
        }
        throw new TimeoutException("Password screen did not appear after Continue; login transition failed");
    }

    public void enterPassword(String password) {
        waitForElementToBeVisible(15, passwordField);
        //waitForElementToBeClickable(15, passwordField);
        passwordField.sendKeys(password);
        logger.info("Password entered");
    }

    public void clickLoginButton() {
        waitForElementToBeClickable(5, loginButton);
        loginButton.click();
        logger.info("Login button clicked");
    }

    /**
     * Complete 2-step login in one call.
     * Continue is retried if the password step does not appear (flaky transition).
     */
    public void login(String loginId, String password) {
        enterLoginId(loginId);
        clickContinueButton();

        // The mobile -> password transition is occasionally flaky; retry Continue once
        // if the password field has not appeared.
        if (!isElementVisible(10, passwordField)) {
            logger.warn("Password step not shown after Continue; retrying Continue...");
            clickContinueButton();
        }

        enterPassword(password);
        clickLoginButton();
        handleDeviceLimit();
        logger.info("Login flow completed for user: {}", loginId);
    }

    // ==================== DEVICE LIMIT ====================

    public void handleDeviceLimit() {
        if (isElementVisible(30, deviceLimitHeader)) {
            addDeviceButton.click();
            logger.info("Device limit popup handled - Add Device clicked");
        }
    }

    // ==================== VALIDATION CHECKS ====================

    public boolean isContinueButtonEnabled() {
        return continueButton.isEnabled();
    }

    public boolean isLoginButtonEnabled() {
        waitForElementToBeVisible(5, loginButton);
        return loginButton.isEnabled();
    }

    public boolean isEmailErrorVisible() {
        return isElementVisible(5, invalidEmailError);
    }

    public boolean isMobileErrorVisible() {
        return isElementVisible(5, invalidMobileError);
    }

    public boolean isPasswordErrorVisible() {
        return isElementVisible(5, incorrectPasswordError);
    }

    public boolean isDeviceLimitReachedDisplayed() {
        return isElementVisible(30, deviceLimitHeader);
    }

    /**
     * Returns true if the password field (step 2) is visible.
     */
    public boolean isPasswordFieldVisible() {
        return isElementVisible(15, passwordField);
    }

    /**
     * Returns true if login succeeded, detected via the post-login profile icon.
     */
    public boolean isLoggedIn() {
        try {
            waitForElementToBeVisible(60, org.openqa.selenium.By.xpath(
                    "//h1[text()='Choose your profile']"));
            logger.info("Login successful - Choose your profile page displayed");
            return true;
        } catch (Exception e) {
            logger.warn("Login not confirmed - Choose your profile page not shown");
            return false;
        }
    }

    public void clickForgotPassword() {
        forgotPasswordLink.click();
        logger.info("Navigated to Forgot Password page");
    }
}
