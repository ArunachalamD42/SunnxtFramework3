package sunShorts;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import pagesSunShorts.PlayerSunShorts;

/** Validates the actual popup locator against supplied markup, without a browser. */
public class PlayerShortsLocatorTest {

    @DataProvider(name = "popupHeadings")
    public Object[][] popupHeadings() {
        return new Object[][] {
            {"<html><h2 class='text-[24px]'><span><img alt='subscription' src='/images/shorts_icons/Subscription_crown_icon.svg'/></span>Keep the Drama Going</h2></html>", 1.0},
            {"<html><h2><span><img alt='subscription'/></span>  Keep the Drama Going  </h2></html>", 1.0},
            {"<html><h2>Episodes</h2><h2>Subscriptions</h2></html>", 0.0}
        };
    }

    @Test(dataProvider = "popupHeadings")
    public void matchesOnlyTheSuppliedPaidEpisodePopup(String markup, double expectedCount) throws Exception {
        String xpath = PlayerSunShorts.class.getDeclaredField("subscriptionPopup")
                .getAnnotation(FindBy.class).xpath();
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(markup)));
        double count = (Double) XPathFactory.newInstance().newXPath()
                .evaluate("count(" + xpath + ")", document, XPathConstants.NUMBER);
        Assert.assertEquals(count, expectedCount);
    }
}
