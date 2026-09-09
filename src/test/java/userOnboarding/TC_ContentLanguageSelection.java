package userOnboarding;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import reporting.ExtentReportManager;

/**
 * TC_ContentLanguageSelection - Validates the dual-platform (SN + SNS) language
 * selection onboarding flow.
 *
 * Flow: Select SN languages -> Proceed -> Select SNS languages -> Proceed
 * SNS pre-selects SNS-supported languages (Tamil, Telugu, Malayalam, Kannada)
 * based on the SN selection. Unsupported SNS languages show "Coming Soon".
 */
public class TC_ContentLanguageSelection extends TestBase {

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void completeDualPlatformOnboarding() {
        test.info("Full dual-platform onboarding: SN -> SNS");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        test.info("SN languages selected");

        cls.proceedToSNSScreen();
        test.info("Proceeded to SNS screen");

        cls.selectSNSLanguages("tamil", "telugu");
        test.info("SNS languages selected");

        cls.clickProceed();
        test.pass("Dual-platform onboarding completed successfully");
    }

    @Test(enabled = true)
    public void snsPreSelectedWhenSupportedLanguageChosenInSN() {
        test.info("SN includes SNS-supported languages -> should pre-select on SNS");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();

        Assert.assertTrue(cls.isSNSLanguagePreSelected("tamil"),
                "Tamil should be pre-selected on SNS");
        Assert.assertTrue(cls.isSNSLanguagePreSelected("telugu"),
                "Telugu should be pre-selected on SNS");

        cls.clickProceed();
        test.pass("SNS pre-selection based on SN selection verified");
    }

    @Test(enabled = true)
    public void snsNotPreSelectedWhenNoSupportedLanguageInSN() {
        test.info("SN has no SNS-supported language -> nothing pre-selected on SNS");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("hindi", "english");
        cls.proceedToSNSScreen();

        Assert.assertFalse(cls.isSNSLanguagePreSelected("tamil"),
                "No language should be pre-selected on SNS when SN has no supported language");

        // User must manually select an SNS language to proceed
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.pass("Manual SNS selection required and completed when no pre-selection");
    }

    @Test(enabled = true)
    public void unsupportedSNSLanguagesShowComingSoon() {
        test.info("Unsupported SNS languages should show Coming Soon and be disabled");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();

        Assert.assertTrue(cls.isSNSLanguageComingSoon("hindi"), "Hindi should show Coming Soon on SNS");
        Assert.assertTrue(cls.isSNSLanguageComingSoon("bengali"), "Bengali should show Coming Soon on SNS");
        Assert.assertTrue(cls.isSNSLanguageComingSoon("marathi"), "Marathi should show Coming Soon on SNS");
        Assert.assertTrue(cls.isSNSLanguageComingSoon("english"), "English should show Coming Soon on SNS");

        test.pass("Unsupported SNS languages correctly shown as Coming Soon");
    }

    @Test(enabled = true)
    public void supportedSNSLanguagesAreNotComingSoon() {
        test.info("Supported SNS languages should be selectable (not Coming Soon)");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();

        Assert.assertFalse(cls.isSNSLanguageComingSoon("tamil"), "Tamil should be selectable on SNS");
        Assert.assertFalse(cls.isSNSLanguageComingSoon("telugu"), "Telugu should be selectable on SNS");
        Assert.assertFalse(cls.isSNSLanguageComingSoon("malayalam"), "Malayalam should be selectable on SNS");
        Assert.assertFalse(cls.isSNSLanguageComingSoon("kannada"), "Kannada should be selectable on SNS");

        test.pass("Supported SNS languages are selectable");
    }

    @Test(enabled = true)
    public void selectAllSNLanguagesReflectsSupportedOnSNS() {
        test.info("Select all SN languages -> only 4 supported reflect on SNS");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu", "malayalam", "kannada", "hindi", "bengali", "marathi", "english");
        cls.proceedToSNSScreen();

        Assert.assertTrue(cls.isSNSLanguagePreSelected("tamil"), "Tamil should reflect on SNS");
        Assert.assertTrue(cls.isSNSLanguagePreSelected("telugu"), "Telugu should reflect on SNS");
        Assert.assertTrue(cls.isSNSLanguagePreSelected("malayalam"), "Malayalam should reflect on SNS");
        Assert.assertTrue(cls.isSNSLanguagePreSelected("kannada"), "Kannada should reflect on SNS");

        cls.clickProceed();
        test.pass("Only SNS-supported languages reflected from full SN selection");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
