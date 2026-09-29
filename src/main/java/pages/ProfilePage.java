package pages;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * ProfilePage - Handles the "Choose your profile" screen shown after login.
 *
 * <p>Each profile tile is located via {@code (//div[@class='flex flex-col'])[index]}
 * (1-based). Clicking a tile selects that profile directly (no confirm button).
 * Locked profiles carry a {@code //img[@alt='lock']} icon and are skipped for now.
 * A Kids profile is identified by its avatar image {@code .../profile/kids.jpg}.</p>
 */
public class ProfilePage extends WaitUtils {

    private static final Logger logger = Log.getLogger(ProfilePage.class);

    private static final String PROFILE_TILE = "(//div[@class='flex flex-col'])[%d]";
    private static final String KIDS_IMAGE_SRC = "profile/kids.jpg";

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//h1[contains(text(),'Choose your profile') or contains(text(),'Who is Watching')]")
    private WebElement chooseProfileTitle;

    @FindBy(xpath = "//div[@class='flex flex-col']")
    private List<WebElement> profileTiles;

    // ==================== PAGE STATE ====================

    /**
     * Returns true if the "Choose your profile" page is displayed.
     */
    public boolean isProfilePageDisplayed() {
        return isElementVisible(30, chooseProfileTitle);
    }

    /**
     * Returns the total number of profiles available on the page.
     */
    public int getProfileCount() {
        waitForElementToBeVisible(30, chooseProfileTitle);
        int count = profileTiles.size();
        logger.info("Total profiles available: {}", count);
        return count;
    }

    // ==================== PROFILE CHECKS ====================

    /**
     * Returns the profile tile at the given 1-based index.
     */
    private WebElement getProfileTile(int index) {
        return driver.findElement(By.xpath(String.format(PROFILE_TILE, index)));
    }

    /**
     * Returns true if the profile at the given 1-based index is locked (PIN-protected).
     */
    public boolean isProfileLocked(int index) {
        boolean locked = !getProfileTile(index)
                .findElements(By.xpath(".//img[@alt='lock']")).isEmpty();
        logger.info("Profile [{}] locked: {}", index, locked);
        return locked;
    }

    /**
     * Returns true if the profile at the given 1-based index is a Kids profile.
     */
    public boolean isKidsProfile(int index) {
        boolean kids = !getProfileTile(index)
                .findElements(By.xpath(".//img[contains(@src,'" + KIDS_IMAGE_SRC + "')]")).isEmpty();
        logger.info("Profile [{}] is Kids profile: {}", index, kids);
        return kids;
    }

    // ==================== PROFILE SELECTION ====================

    /**
     * Selects the primary profile (first tile).
     */
    public void selectPrimaryProfile() {
        selectProfileByIndex(1);
        logger.info("Primary profile selected");
    }

    /**
     * Selects the profile at the given 1-based index.
     */
    public void selectProfileByIndex(int index) {
        WebElement tile = getProfileTile(index);
        scrollIntoView(tile);
        waitForElementToBeClickable(10, tile);
        tile.click();
        logger.info("Profile [{}] selected", index);
    }
}
