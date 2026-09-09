package pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * ProfilePage - Handles profile switching (Who is Watching screen).
 */
public class ProfilePage extends WaitUtils {

    private static final Logger logger = Log.getLogger(ProfilePage.class);

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//h1[text()='Who is Watching?']")
    private WebElement switchProfileTitle;

    @FindBy(xpath = "//button[text()='Select ']")
    private WebElement selectButton;

    @FindBy(xpath = "//div[@class='myprofilepopups_selected_profile__jArXe']")
    private WebElement primaryProfile;

    @FindBy(xpath = "(//div[@class='myprofilepopups_profile__0b1Ft'])[1]")
    private WebElement secondaryProfile1;

    @FindBy(xpath = "(//div[@class='myprofilepopups_profile__0b1Ft'])[2]")
    private WebElement secondaryProfile2;

    @FindBy(xpath = "(//div[@class='myprofilepopups_profile__0b1Ft'])[3]")
    private WebElement secondaryProfile3;

    public void selectPrimaryProfile() {
        waitForElementToBeClickable(5, primaryProfile);
        primaryProfile.click();
        selectButton.click();
        logger.info("Primary profile selected");
    }

    public void selectProfile(int profileNumber) {
        switch (profileNumber) {
            case 1:
                selectPrimaryProfile();
                break;
            case 2:
                secondaryProfile1.click();
                selectButton.click();
                logger.info("Secondary profile 1 selected");
                break;
            case 3:
                secondaryProfile2.click();
                selectButton.click();
                logger.info("Secondary profile 2 selected");
                break;
            case 4:
                secondaryProfile3.click();
                selectButton.click();
                logger.info("Secondary profile 3 selected");
                break;
            default:
                logger.warn("Invalid profile number: {}", profileNumber);
        }
    }
}
