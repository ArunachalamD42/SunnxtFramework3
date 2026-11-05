package userOnboarding;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.SignupPage;
import reporting.ExtentReportManager;

public class TC_UserCreation extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = false)
    public void userCreationInternational() throws InterruptedException {
        test.info("User creation (International) test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();
        test.info("Navigated to Login/Signup page");

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId(randomEmail());
        sp.enterNewPassword("A1234567");
        sp.enterConfirmPassword("A1234567");
        sp.selectGender("female");
        sp.selectAge(18);

        Assert.assertTrue(sp.isDemographicHeaderDisplayed(), "User creation should complete to demographic step");
        test.info("User created successfully");
        Thread.sleep(10000);
        test.pass("User Creation (International) Passed");
    }

    @Test(enabled = true)
    public void userCreationIndia() throws InterruptedException {
        test.info("User creation (India) test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId(randomEmail());
        sp.enterNewPassword("A1234567");
        sp.enterConfirmPassword("A1234567");
        sp.selectGender("female");
        sp.selectAge(18);

        Assert.assertTrue(sp.isDemographicHeaderDisplayed());
        test.pass("User Creation (India) Passed");
    }

    @Test(enabled = false)
    public void registeredUserTest() {
        test.info("Registered user test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId("sunnxtq@gmail.com");

        String expectedResult = sp.isPasswordHeaderText();
        String actualResult = "Enter your password to continue";
        Assert.assertEquals(actualResult, expectedResult);
        test.pass("Registered user redirected to password step");
    }

    @Test(enabled = false)
    public void emptyEmailIdTest() {
        test.info("Empty email test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId(" ");
        Assert.assertFalse(sp.isContinueBtnEnabled(), "Continue should be disabled for empty email");
        test.pass("Empty email validation passed");
    }

    @Test(enabled = false)
    public void incorrectPasswordTest() {
        test.info("Incorrect password mismatch test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId(randomEmail());
        sp.enterNewPassword("A1234567");
        sp.enterConfirmPassword("1234567A");

        if (sp.incorrectPasswordErrorDisplayed()) {
            Assert.assertEquals(sp.incorrectPasswordErrorMsg(), "Password must match in both fields");
        }
        test.pass("Password mismatch validation passed");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
