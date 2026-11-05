package contentPlayback;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import pagesSunShorts.HomePageSunShorts;
import pagesSunShorts.PlayerSunShorts;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentDetailPageLiveTV;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.PlayerControlsPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

public class TC_Complete_playback extends TestBase {

	
	@BeforeMethod
    public void setupTest(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }
	
	@Test
	public void completePlayback() throws InterruptedException {
		logger.info("Starting Movies playback test...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
        hp.navigateToLogin();

        LoginPage lp = new LoginPage(driver);
        String username = ConfigReader.get("test.username");
        String password = ConfigReader.get("test.password");
        lp.enterLoginId(username);
        lp.clickContinueButton();
        if (!lp.isPasswordFieldVisible()) {
            lp.clickContinueButton();
        }
        lp.enterPassword(password);
        lp.clickLoginButton();
        lp.handleDeviceLimit();
        test.info("User logged in");

        ProfilePage profile = new ProfilePage(driver);
        test.info("Total profiles: " + profile.getProfileCount());
        profile.selectPrimaryProfile();
        test.info("Primary profile selected");

        hp.navigateToMovies();
        test.info("Navigated to Movies");

        hp.contentPlayBackCheck(-1, -1);
        test.info("Random content selected from Movies");

        ContentDetailPage cdp = new ContentDetailPage(driver);
        String contentName = cdp.getContentName();
        test.info("Playing: " + contentName);
        cdp.clickPlayButton();

        
	}
}
