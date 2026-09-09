package pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * SignupPage - Handles the new user registration flow.
 * Step 1: Enter email → Continue
 * Step 2: Enter new password + confirm password → Create Account
 * Step 3: Select gender + age (demographic) → Save
 */
public class SignupPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(SignupPage.class);

    public SignupPage(WebDriver driver) {
        super(driver);
    }

    // ==================== STEP 1: EMAIL ====================

    @FindBy(tagName = "h3")
    private WebElement loginHeader;

    @FindBy(name = "email")
    private WebElement emailField;

    @FindBy(xpath = "//button[text()='Continue']")
    private WebElement continueBtn;

    // ==================== STEP 2: PASSWORD ====================

    @FindBy(name = "newPassword")
    private WebElement newPasswordField;

    @FindBy(name = "confirmPassword")
    private WebElement confirmPasswordField;

    @FindBy(xpath = "//button[text()='Create Account']")
    private WebElement createAccountBtn;

    @FindBy(xpath = "//span[text()='Password must match in both fields']")
    private WebElement incorrectPasswordError;

    @FindBy(xpath = "//span[text()='Enter your password to continue']")
    private WebElement passwordHeader;

    @FindBy(xpath = "//label[text()='Data Protection Notice']")
    private WebElement gdprCheckbox;

    // ==================== STEP 3: DEMOGRAPHIC ====================

    @FindBy(xpath = "//h3[text()='Almost done!']")
    private WebElement demographicHeader;

    @FindBy(xpath = "//button[text()='Save']")
    private WebElement saveBtn;

    @FindBy(xpath = "//div[@name='gender']")
    private WebElement genderDropdown;

    @FindBy(xpath = "//div[text()='Male']")
    private WebElement maleOption;

    @FindBy(xpath = "//div[text()='Female']")
    private WebElement femaleOption;

    @FindBy(xpath = "//div[text()='Others']")
    private WebElement othersOption;

    @FindBy(xpath = "//div[@name='age']")
    private WebElement ageDropdown;

    @FindBy(xpath = "//div[text()='18 - 24 Years']")
    private WebElement age18to24;

    @FindBy(xpath = "//div[text()='25 - 34 Years']")
    private WebElement age25to34;

    @FindBy(xpath = "//div[text()='35 - 44 Years']")
    private WebElement age35to44;

    @FindBy(xpath = "//div[text()='45 - 60 Years']")
    private WebElement age45to60;

    @FindBy(xpath = "//div[text()='60+ Years']")
    private WebElement age60AndAbove;

    // ==================== ACTIONS ====================

    /**
     * Enters email/login ID and clicks Continue.
     */
    public void enterLoginId(String loginId) {
        waitForElementToBeClickable(5, emailField);
        implicitWait(5);
        emailField.sendKeys(loginId);
        logger.info("Login ID entered: {}", loginId);

        if (isContinueBtnEnabled()) {
            waitForElementToBeClickable(5, emailField);
            continueBtn.click();
            logger.info("Continue button clicked");
        }
    }

    /**
     * Enters the new password.
     */
    public void enterNewPassword(String newPassword) {
        waitForElementToBeClickable(5, newPasswordField);
        if (!newPassword.isEmpty()) {
            newPasswordField.sendKeys(newPassword);
            logger.info("New password entered");
        } else {
            waitForElementToBeVisible(5, newPasswordField);
            logger.info("Skipping: empty password scenario");
        }
    }

    /**
     * Enters confirm password, handles GDPR checkbox, and clicks Create Account.
     */
    public void enterConfirmPassword(String confirmPassword) {
        waitForElementToBeClickable(5, confirmPasswordField);
        if (!confirmPassword.isEmpty()) {
            confirmPasswordField.sendKeys(confirmPassword);
            logger.info("Confirm password entered");
        } else {
            waitForElementToBeVisible(5, newPasswordField);
            logger.info("Skipping: empty confirm password scenario");
        }

        // Handle GDPR checkbox if present
        try {
            if (gdprCheckbox.isDisplayed()) {
                waitForElementToBeClickable(5, gdprCheckbox);
                gdprCheckbox.click();
                logger.info("GDPR checkbox clicked");
            }
        } catch (NoSuchElementException e) {
            logger.info("GDPR checkbox not present");
        }

        createAccountBtn.click();
        logger.info("Create Account button clicked");
    }

    /**
     * Selects gender from the demographic form.
     */
    public void selectGender(String gender) {
        waitForElementToBeClickable(20, genderDropdown);
        if (!saveBtn.isEnabled()) {
            if (gender.equalsIgnoreCase("male")) {
                genderDropdown.click();
                maleOption.click();
                logger.info("Gender selected: Male");
            } else if (gender.equalsIgnoreCase("female")) {
                genderDropdown.click();
                femaleOption.click();
                logger.info("Gender selected: Female");
            } else if (gender.equalsIgnoreCase("others")) {
                genderDropdown.click();
                othersOption.click();
                logger.info("Gender selected: Others");
            } else {
                logger.warn("Gender '{}' not listed", gender);
            }
        } else {
            logger.info("Save button already enabled - gender may be pre-selected");
        }
    }

    /**
     * Selects age range and clicks Save.
     */
    public void selectAge(int age) {
        waitForElementToBeClickable(5, ageDropdown);
        ageDropdown.click();

        if (age >= 18 && age <= 24) {
            age18to24.click();
        } else if (age >= 25 && age <= 34) {
            age25to34.click();
        } else if (age >= 35 && age <= 44) {
            age35to44.click();
        } else if (age >= 45 && age < 60) {
            age45to60.click();
        } else if (age >= 60 && age <= 100) {
            age60AndAbove.click();
        } else {
            throw new IllegalStateException("Age " + age + " is not in valid range (18-100)");
        }
        logger.info("Age range selected for age: {}", age);

        saveBtn.click();
        logger.info("Save button clicked");
    }

    /**
     * Clicks the GDPR checkbox explicitly.
     */
    public void clickGdpr() {
        waitForElementToBeClickable(5, gdprCheckbox);
        gdprCheckbox.click();
        logger.info("GDPR checkbox clicked");
    }

    // ==================== VALIDATION CHECKS ====================

    public boolean isContinueBtnEnabled() {
        return continueBtn.isEnabled();
    }

    public boolean isCreateAccountBtnEnabled() {
        waitForElementToBeVisible(5, createAccountBtn);
        return createAccountBtn.isEnabled();
    }

    public boolean isCreateAccountBtnDisplayed() {
        return createAccountBtn.isDisplayed();
    }

    public boolean isDemographicHeaderDisplayed() {
        return demographicHeader.isDisplayed();
    }

    public boolean isSaveBtnEnabled() {
        waitForElementToBeVisible(5, saveBtn);
        return saveBtn.isEnabled();
    }

    public boolean isPasswordHeaderTextDisplayed() {
        return passwordHeader.isDisplayed();
    }

    public String isPasswordHeaderText() {
        return passwordHeader.getText();
    }

    public boolean incorrectPasswordErrorDisplayed() {
        return incorrectPasswordError.isDisplayed();
    }

    public String incorrectPasswordErrorMsg() {
        return incorrectPasswordError.getText();
    }
}
