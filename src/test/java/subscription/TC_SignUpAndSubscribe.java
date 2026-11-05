package subscription;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.PaymentPage;
import pagesSunnxt.RazorpayPage;
import pagesSunnxt.SignupPage;
import pagesSunnxt.SubscriptionPage;
import reporting.ExtentReportManager;

/**
 * TC_SignUpAndSubscribe - Tests new user signup followed by subscription via Card.
 */
public class TC_SignUpAndSubscribe extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void signUpAndSubscribePremium() {
        test.info("SignUp + Subscribe (Premium, Card) test starts...");

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

        pp.payUsingCard();
        test.info("Card details entered");

        String originalWindow = driver.getWindowHandle();
        RazorpayPage rp = pp.completePayment(originalWindow);
        rp.handleSuccessPayment();
        test.info("Payment completed via Razorpay");

        pp.switchToWindow(originalWindow);
        if (pp.isPaymentSuccessfulMsgShown()) {
            pp.clickProceedBtn();
            test.info("Proceed clicked after payment success");
        }

        test.pass("SignUp + Subscribe (Premium) passed");
    }

    @Test(enabled = false)
    public void signUpAndSubscribeLite() {
        test.info("SignUp + Subscribe (Lite, Card) test starts...");

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

        SubscriptionPage subPage = new SubscriptionPage(driver);
        PaymentPage pp = subPage.navigateToPaymentPage("lite");

        pp.payUsingCard();

        String originalWindow = driver.getWindowHandle();
        RazorpayPage rp = pp.completePayment(originalWindow);
        rp.handleSuccessPayment();

        pp.switchToWindow(originalWindow);
        if (pp.isPaymentSuccessfulMsgShown()) {
            pp.clickProceedBtn();
        }

        test.pass("SignUp + Subscribe (Lite) passed");
    }

    @Test(enabled = false)
    public void signUpSubscribeAndUpgrade() {
        test.info("SignUp + Subscribe + Upgrade test starts...");

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

        SubscriptionPage subPage = new SubscriptionPage(driver);
        PaymentPage pp = subPage.navigateToPaymentPage("lite");

        pp.payUsingCard();

        String originalWindow = driver.getWindowHandle();
        RazorpayPage rp = pp.completePayment(originalWindow);
        rp.handleSuccessPayment();

        pp.switchToWindow(originalWindow);
        if (pp.isPaymentSuccessfulMsgShown()) {
            pp.clickProceedBtn();
        }
        test.info("Initial subscription completed");

        // Upgrade
        hp.sideMenu("subscriptions");
        PaymentPage pp2 = subPage.upgradeAndUpcoming("saver");
        pp2.payUsingCard();

        String originalWindow2 = driver.getWindowHandle();
        RazorpayPage rp2 = pp2.completePayment(originalWindow2);
        rp2.handleSuccessPayment();

        pp2.switchToWindow(originalWindow2);
        if (pp2.isPaymentSuccessfulMsgShown()) {
            pp2.clickProceedBtn();
        }

        test.pass("Upgrade subscription passed");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
