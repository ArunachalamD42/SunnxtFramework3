package microdrama;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import pages.ContentLanguageSelectionPage;
import pages.HomePage;
import pages.LoginPage;
import pages.MicrodramaPage;
import config.ConfigReader;
import config.Log;
import reporting.ExtentReportManager;

/**
 * TC_MicrodramaBrowse - Tests Microdrama browsing and navigation.
 */
public class TC_MicrodramaBrowse extends TestBase {

    private static final Logger logger = Log.getLogger(TC_MicrodramaBrowse.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void navigateToMicrodramaTest() {
        logger.info("Starting Microdrama navigation test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));
        test.info("User logged in");

        waitForLoginSuccess();

        MicrodramaPage md = new MicrodramaPage(driver);
        md.navigateToMicrodrama();
        test.info("Navigated to Microdrama");

        Assert.assertTrue(md.isMicrodramaPageLoaded(), "Microdrama page should load");
        test.pass("Microdrama page loaded successfully");
    }

    @Test(enabled = true)
    public void browseMicrodramaContentTest() {
        logger.info("Starting Microdrama browse test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        lp.login(ConfigReader.get("test.username"), ConfigReader.get("test.password"));

        waitForLoginSuccess();

        MicrodramaPage md = new MicrodramaPage(driver);
        md.navigateToMicrodrama();
        md.scrollToLoadContent();

        int count = md.getContentCount();
        test.info("Content cards found: " + count);

        Assert.assertTrue(count > 0, "Microdrama should have content");
        test.pass("Microdrama browse successful");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
