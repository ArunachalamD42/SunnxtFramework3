package searchPage;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.Log;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.Search;
import reporting.ExtentReportManager;

public class TC_SearchFunctionality extends TestBase {

    private static final Logger logger = Log.getLogger(TC_SearchFunctionality.class);

    @BeforeMethod
    public void setup(Method method) {
        test = ExtentReportManager.createTest(method.getName());
        setupDriver();
        launchSunnxt();
    }

    @Test(enabled = false)
    public void searchPageBasicsTest() {
        test.info("Search page basics test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
       // hp.waitForHomePageLoaded();
        String homeUrl = driver.getCurrentUrl();

        Search search = new Search(driver);
        search.clickSearchIcon();
        Assert.assertTrue(search.isSearchPageDisplayed(), "Search icon did not redirect to the search page");
        test.info("Search page opened using search icon");

        //driver.get(homeUrl);
       // hp.waitForHomePageLoaded();

        search.clickSearchMenu();
        Assert.assertTrue(search.isSearchPageDisplayed(), "Search menu did not redirect to the search page");
        test.info("Search page opened using search menu");

        Assert.assertTrue(search.isTrendingSearchHeaderDisplayed(), "Trending Search header is not displayed");
        test.info("Trending Search header displayed");

        int trendingSearchCount = search.getTrendingSearchItemsCount();
        Assert.assertTrue(trendingSearchCount > 0, "No Trending Search content found");
        test.info("Trending Search content count: " + trendingSearchCount);



        search.enterSearchText("Sun");
        Assert.assertEquals(search.getSearchBarText(), "Sun", "Search text Sun is not displayed");
        test.info("Search text Sun displayed");

        search.enterSearchText("123");
        Assert.assertEquals(search.getSearchBarText(), "123", "Search text 123 is not displayed");
        test.info("Search text 123 displayed");

        search.enterSearchText("Sun123");
        Assert.assertEquals(search.getSearchBarText(), "Sun123", "Search text Sun123 is not displayed");
        test.info("Search text Sun123 displayed");

        search.enterSearchText("Sun NXT");
        Assert.assertEquals(search.getSearchBarText(), "Sun NXT", "Search text Sun NXT is not displayed");
        test.info("Search text Sun NXT displayed");
        

        test.pass("Search page basics verified");
    }

    @Test(enabled = true)
    public void searchResultsTest() {
        test.info("Search result test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
     //   hp.waitForHomePageLoaded();

        Search search = new Search(driver);
        search.clickSearchIcon();
        Assert.assertTrue(search.isSearchPageDisplayed(), "Search page did not open");

        search.enterSearchText("love");
        
        Assert.assertTrue(search.isLanguageFilterDisplayed(), "Language filter is not displayed");
        logger.info("Language filter displayed");
        test.info("Language filter displayed");
        
        List<String> resultHeaders = search.getAllResultHeaders();
        Assert.assertFalse(resultHeaders.isEmpty(), "No result carousels shown for love");
        Assert.assertEquals(new HashSet<>(resultHeaders).size(), resultHeaders.size(),
                "Duplicate result carousel headers found: " + resultHeaders);
        test.info("Search result carousels: " + resultHeaders);

        test.pass("Search results verified");
    }

    @Test(enabled = true)
    public void noResultFoundTest() {
        test.info("No result search test starts...");

        ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
        cls.selectSNLanguages("tamil", "telugu");
        cls.proceedToSNSScreen();
        cls.selectSNSLanguages("tamil", "telugu");
        cls.clickProceed();
        test.info("Language selected");

        HomePage hp = new HomePage(driver);
    //    hp.waitForHomePageLoaded();

        Search search = new Search(driver);
        search.clickSearchIcon();
        Assert.assertTrue(search.isSearchPageDisplayed(), "Search page did not open");

        search.enterSearchText("zzzxqv987654321");
        Assert.assertTrue(search.isNoResultFoundDisplayed(),
                "No Results Found message is not displayed for gibberish search input");
        test.info("No Results Found message displayed for gibberish search input");

        test.pass("No result search verified");
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }
}
