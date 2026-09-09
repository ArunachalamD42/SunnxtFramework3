package pages;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import config.Log;
import helpers.WaitUtils;

/**
 * ContentLanguageSelectionPage - Handles the dual-platform language selection during onboarding.
 *
 * Updated flow:
 * - Two platforms maintain separate language preferences: SUN NXT (SN) and SUN NXT SHORTS (SNS).
 * - User selects languages for SN first, then clicks Proceed to reach the SNS screen.
 * - SNS pre-selects languages based on SN selection (only for SNS-supported: Tamil, Telugu, Malayalam, Kannada).
 * - If none of the SNS-supported languages are chosen in SN, nothing is pre-selected for SNS,
 *   and the user must manually select at least one to proceed.
 * - In the SNS screen, unsupported languages (Hindi, Bengali, Marathi, English) are shown as
 *   "Coming Soon" and remain disabled.
 */
public class ContentLanguageSelectionPage extends WaitUtils {

    private static final Logger logger = Log.getLogger(ContentLanguageSelectionPage.class);

    /** Languages supported by the SNS (Sun NXT Shorts) platform. */
    private static final List<String> SNS_SUPPORTED_LANGUAGES =
            Arrays.asList("Tamil", "Telugu", "Malayalam", "Kannada");

    public ContentLanguageSelectionPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(xpath = "//span[text()='Movies, TV Shows & Live Channels']")
    private WebElement sunNxtTab;

    @FindBy(xpath = "//h3[text()='Select the languages you would like to watch']")
    private WebElement snHeader;

    @FindBy(xpath = "//h2[text()='Sun NXT Shorts Languages']")
    private WebElement snsHeader;

    @FindBy(xpath = "//button[text()='Proceed']")
    private WebElement proceedButton;

    // ==================== SN PLATFORM ACTIONS ====================

    /**
     * Selects languages for the SUN NXT (SN) platform.
     */
    public void selectSNLanguages(String... languages) {
        waitForElementToBeVisible(10, snHeader);
        for (String language : languages) {
            String capitalized = language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
            WebElement lang = driver.findElement(By.xpath("//div[contains(@style,'LanguageOnboarding') and contains(@style,'" + capitalized + ".png')]"));
            lang.click();
            logger.info("SN language selected: {}", capitalized);
        }
    }

    // ==================== SNS PLATFORM ACTIONS ====================

    /**
     * Proceeds from the SN language screen to the SNS language screen.
     */
    public void proceedToSNSScreen() {
        clickProceed();
        waitForElementToBeVisible(10, snsHeader);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Navigated to SUN NXT SHORTS language screen");
    }

    /**
     * Selects languages for the SUN NXT SHORTS (SNS) platform.
     * Only SNS-supported languages can be selected; others are disabled ("Coming Soon").
     */
    public void selectSNSLanguages(String... languages) {
        waitForElementToBeVisible(10, snsHeader);
        for (String language : languages) {
            String capitalized = language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
            if (!SNS_SUPPORTED_LANGUAGES.contains(capitalized)) {
                logger.warn("{} is not supported on SNS (Coming Soon). Skipping.", capitalized);
                continue;
            }
            if (isSNSLanguagePreSelected(capitalized)) {
                logger.info("SNS language {} already selected (inherited from SN). Skipping click.", capitalized);
                continue;
            }
            WebElement lang = driver.findElement(By.xpath("//div[contains(@style,'LanguageOnboarding') and contains(@style,'" + capitalized + ".png')]"));
            lang.click();
            logger.info("SNS language selected: {}", capitalized);
        }
    }

    // ==================== VALIDATION HELPERS ====================

    /**
     * Checks if an SNS language shows "Coming Soon" (i.e., is disabled/unsupported).
     */
    public boolean isSNSLanguageComingSoon(String language) {
        String capitalized = language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
        boolean comingSoon = !driver.findElements(By.xpath("//div[contains(@style,'" + capitalized + ".png')][.//*[contains(text(),'Coming Soon')]]")).isEmpty();
        logger.info("SNS language {} Coming Soon: {}", capitalized, comingSoon);
        return comingSoon;
    }

    /**
     * Checks whether an SNS language card is pre-selected based on SN selection.
     *
     * DOM structure:
     *   parent div: class="... transition-opacity duration-300 opacity-100"           (selected)
     *   parent div: class="... transition-opacity duration-300 opacity-50 hover:opacity-100" (unselected)
     *     child div: class="w-full h-full rounded-[6px] overflow-hidden" style="background-image: .../Tamil.png"
     *
     * The background-image (with the language PNG) is on the CHILD div, while the
     * opacity class is on the PARENT. An unselected card carries "opacity-50";
     * a selected card does not. We check the parent for absence of "opacity-50".
     */
    public boolean isSNSLanguagePreSelected(String language) {
        String capitalized = language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
        try {
            WebElement parentCard = driver.findElement(By.xpath(
                    "//div[contains(@style,'" + capitalized + ".png')]/parent::div"));
            String classAttr = parentCard.getAttribute("class");
            boolean preSelected = classAttr != null && !classAttr.contains("opacity-50");
            logger.info("SNS language {} pre-selected: {}", capitalized, preSelected);
            return preSelected;
        } catch (Exception e) {
            logger.warn("Could not determine pre-selection state for {}", capitalized);
            return false;
        }
    }

    /**
     * Returns true if any SNS-supported language is present in the SN selection.
     */
    public boolean anySNSSupportedLanguageIn(String... snLanguages) {
        for (String language : snLanguages) {
            String capitalized = language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
            if (SNS_SUPPORTED_LANGUAGES.contains(capitalized)) {
                return true;
            }
        }
        return false;
    }

    // ==================== PROCEED ====================

    public void clickProceed() {
        proceedButton.click();
        logger.info("Proceed button clicked");
    }
}
