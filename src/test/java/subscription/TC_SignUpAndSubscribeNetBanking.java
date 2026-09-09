package subscription;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.PaymentPage;
import pages.RazorpayPage;
import pages.SignupPage;
import pages.SubscriptionPage;
import reporting.ExtentReportManager;

/**
 * TC_SignUpAndSubscribeNetBanking - Tests signup + subscription via Net Banking.
 */
public class TC_SignUpAndSubscribeNetBanking extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void signUpAndSubscribeViaNetBanking() {
        test.info("SignUp + Subscribe (Net Banking) test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        SignupPage sp = new SignupPage(driver);
        sp.enterLoginId(randomEmail());
        sp.enterNewPassword("A1234567");
        sp.enterConfirmPassword("A1234567");
        sp.selectGender("female");
        sp.selectAge(18);
        test.info("User created");

        SubscriptionPage subPage = new SubscriptionPage(driver);
        PaymentPage pp = subPage.navigateToPaymentPage("premium");
        test.info("Premium plan selected");

        pp.payUsingNetBanking();
        test.info("Net Banking partner selected");

        String originalWindow = driver.getWindowHandle();
        RazorpayPage rp = new RazorpayPage(driver);
        rp.handleSuccessPayment();
        test.info("Payment completed via Razorpay");

        pp.switchToWindow(originalWindow);
        if (pp.isPaymentSuccessfulMsgShown()) {
            pp.clickProceedBtn();
            test.info("Proceed clicked after payment success");
        }

        test.pass("SignUp + Subscribe (Net Banking) passed");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
