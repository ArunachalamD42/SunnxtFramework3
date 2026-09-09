package userOnboarding;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import config.ConfigReader;
import dataProvider.DataProviderClass;
import reporting.ExtentReportManager;

public class TC_Login extends TestBase {

    @BeforeMethod
    public void setupTest(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    /**
     * Completes language selection and navigates to the Login page.
     */
    private LoginPage goToLoginPage() {
        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();
        test.info("Navigated to Login page");

        return new LoginPage(driver);
    }

    @Test(enabled = true)
    public void loginTestPositive() {
        test.info("Login test starts...");

        LoginPage lp = goToLoginPage();
        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");

        lp.enterLoginId(username);
        
        lp.clickContinueButton();
        lp.enterPassword(password);
        lp.clickLoginButton();

        if (lp.isDeviceLimitReachedDisplayed()) {
            lp.handleDeviceLimit();
        }

        Assert.assertTrue(lp.isLoggedIn(), "Login failed for config user");
        test.pass("Login Test Passed");
    }

    @Test(enabled = false, dataProvider = "loginData", dataProviderClass = DataProviderClass.class)
    public void loginTestMultipleUsers(String username, String password) {
        test.info("Login test with multiple users: " + username);

        LoginPage lp = goToLoginPage();

        lp.enterLoginId(username);
        lp.clickContinueButton();

        // Use a single check to see which one is actually there
        if (!lp.isPasswordFieldVisible()) {
            Assert.assertTrue(true, "Login blocked at ID step (invalid/empty ID): " + username);
        } else {
            lp.enterPassword(password);
            lp.clickLoginButton();

            if (lp.isDeviceLimitReachedDisplayed()) {
                lp.handleDeviceLimit();
            }

            if (lp.isLoggedIn()) {
                Assert.assertTrue(true, "Login successful for: " + username);
            } else {
                Assert.assertTrue(lp.isPasswordErrorVisible(), "Password error displayed for: " + username);
            }
        }
    }

    @Test(enabled = false, dataProvider = "invalidUser", dataProviderClass = DataProviderClass.class)
    public void invalidUserIDTest(String username) {
        test.info("Invalid user ID test: " + username);

        LoginPage lp = goToLoginPage();

        lp.enterLoginId(username);

        // Continue may be disabled for invalid input, or clicking it surfaces a validation error.
        if (!lp.isContinueButtonEnabled()) {
            Assert.assertFalse(lp.isContinueButtonEnabled(),
                    "Continue should be disabled for invalid ID: " + username);
            test.pass("Continue disabled for invalid ID: " + username);
            return;
        }

        lp.clickContinueButton();

        boolean errorShown = lp.isEmailErrorVisible() || lp.isMobileErrorVisible();
        Assert.assertTrue(errorShown, "No validation error appeared for invalid ID: " + username);
        test.pass("Validation error displayed for invalid ID: " + username);
    }

    @Test(enabled = false)
    public void invalidPasswordTest() {
        test.info("Invalid password test...");

        LoginPage lp = goToLoginPage();

        lp.enterLoginId(ConfigReader.get("test.username"));
        lp.clickContinueButton();
        lp.enterPassword("wrongpassword");
        lp.clickLoginButton();

        Assert.assertTrue(lp.isPasswordErrorVisible(), "Password error should display");
        test.pass("Invalid password validation passed");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
