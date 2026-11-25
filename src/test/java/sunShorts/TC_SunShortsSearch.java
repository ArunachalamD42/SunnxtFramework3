package sunShorts;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.Log;
import pagesSunShorts.HomePageSunShorts;
import pagesSunShorts.SearchSunShorts;
import pagesSunnxt.ContentLanguageSelectionPage;
import reporting.ExtentReportManager;

public class TC_SunShortsSearch extends TestBase {
	
	  private static final Logger logger = Log.getLogger(TC_SunShortsSearch.class);

	    @BeforeMethod
	    public void setup(Method method) {
	        test = ExtentReportManager.createTest(method.getName());
	        setupDriver();
	        launchSunnxt();
	    }

	    @Test(enabled = true)
	    public void shortsSearchPageBasicsTest() {
	        test.info("Search page basics test starts...");

	        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
	        cls.selectSNLanguages("tamil", "telugu");
	        cls.proceedToSNSScreen();
	        cls.selectSNSLanguages("tamil", "telugu");
	        cls.clickProceed();
	        test.info("Language selected");
	        
	        HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
	        shortsHome.shortsNavigation();
	        Assert.assertFalse(shortsHome.isSwitchConfirmationDisplayed(),
	                "Experience selection dialog did not close");
	        Assert.assertTrue(shortsHome.isSunShortsPageLoaded(), "Home navigation was not displayed");
	        

	        SearchSunShorts search = new SearchSunShorts(driver);
	        shortsHome.navigateToSearch();
	        
	        Assert.assertTrue(search.isSearchPageDisplayed(), "Search icon did not redirect to the search page");
	        test.info("Search page opened using search icon");

	        Assert.assertTrue(search.isTrendingOnShortsDisplayed(), "Trending Search header is not displayed");
	        test.info("Trending Search header displayed");

	        int trendingSearchCount = search.getTrendingContentCount();
	        Assert.assertTrue(trendingSearchCount > 0, "No Trending Search content found");
	        logger.info("Trending Search content count: " + trendingSearchCount);
	        test.info("Trending Search content count: " + trendingSearchCount);


	        search.enterSearchText("Sun");
	        Assert.assertEquals(search.getSearchBarText(), "Sun", "Search text Sun is not displayed");
	        
	        Assert.assertTrue(search.isSearchHeaderDisplayedOnShorts(), " Search header is not displayed");
	        test.info("Search header displayed");
	        
	        int searchCount = search.getSearchContentCount();
	        Assert.assertTrue(searchCount > 0, "No Search content found");
	        logger.info("Search content count: " + searchCount);
	        test.info(" Search content count: " + searchCount);
	        
	        
	        logger.info("Search text Sun displayed");
	        test.info("Search text Sun displayed");

	        search.enterSearchText("123");
	        Assert.assertEquals(search.getSearchBarText(), "123", "Search text 123 is not displayed");
	        logger.info("Search text 123 displayed\"");
	        test.info("Search text 123 displayed");

	        search.enterSearchText("Sun123");
	        Assert.assertEquals(search.getSearchBarText(), "Sun123", "Search text Sun123 is not displayed");
	        logger.info("Search text Sun123 displayed");
	        test.info("Search text Sun123 displayed");

	        search.enterSearchText("Sun NXT");
	        Assert.assertEquals(search.getSearchBarText(), "Sun NXT", "Search text Sun NXT is not displayed");
	        logger.info("Search text Sun NXT displayed");
	        test.info("Search text Sun NXT displayed");
	        
	        search.enterSearchText("zzzxqv987654321");
	        Assert.assertTrue(search.isNoResultsFoundDisplayed(),
	                "No Results Found message is not displayed for gibberish search input");
	        
	        test.info("No Results Found message displayed for gibberish search input");
	        test.pass("Search page basics verified");
	    }
	    @AfterMethod
	    public void tearDown() {
	        quitDriver();
	    }
}
