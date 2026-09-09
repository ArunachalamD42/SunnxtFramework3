package dataProvider;

import java.io.IOException;

import org.testng.annotations.DataProvider;

public class EntitlementDataProvider {

    private static final String ENTITLEMENT_DATA_FILE = "testdata/entitlement-testdata.xlsx";

    @DataProvider(name = "planConfig")
    public static Object[][] planConfigData() throws IOException {
        return ExcelUtility.getDataFromExcel("PlanConfig", ENTITLEMENT_DATA_FILE);
    }

    @DataProvider(name = "testContent")
    public static Object[][] testContentData() throws IOException {
        return ExcelUtility.getDataFromExcel("TestContent", ENTITLEMENT_DATA_FILE);
    }
}
