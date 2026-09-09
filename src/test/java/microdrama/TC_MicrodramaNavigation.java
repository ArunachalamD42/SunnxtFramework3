package microdrama;

import java.lang.reflect.Method;
import java.util.List;

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
 * TC_MicrodramaNavigation - Tests Microdrama internal sections and content selection.
 */
public class TC_MicrodramaNavigation extends TestBase {

    private static final Logger logger = Log.getLogger(TC_MicrodramaNavigation.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = true)
    public void microdramaSectionsTest() {
        logger.info("Starting Microdrama sections test...");

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

        Assert.assertTrue(md.isMicrodramaPageLoaded(), "Microdrama should load");

        md.scrollToLoadContent();
        List<String> sections = md.getSectionNames();
        test.info("Sections: " + sections);

        Assert.assertFalse(sections.isEmpty(), "Should have sections");
        test.pass("Sections verified: " + sections.size() + " found");
    }

    @Test(enabled = true)
    public void selectSpecificContentTest() {
        logger.info("Starting specific content test...");

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
        Assert.assertTrue(count > 0, "Content should exist");

        md.selectContent(0);
        String title = md.getEpisodeTitle();
        test.info("Selected: " + title);

        Assert.assertFalse(title.isEmpty(), "Title should not be empty");
        test.pass("Specific content selection passed");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
