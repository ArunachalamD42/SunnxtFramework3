package pagesSunShorts;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import helpers.WaitUtils;

public class SearchSunShorts extends WaitUtils {

	public SearchSunShorts(WebDriver driver) {
		super(driver);
	}

	@FindBy (xpath = "//input[@placeholder='Search your favourite content']")
	private WebElement searchShorts;
	
	
	
}
