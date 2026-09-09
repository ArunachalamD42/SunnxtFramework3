package dataProvider;

import java.io.IOException;

import org.testng.annotations.DataProvider;

public class DataProviderClass {

    @DataProvider(name = "loginData")
    public Object[][] loginData() throws IOException {
        return ExcelUtility.getDataFromExcel("Sheet1", "testdata/testdata.xlsx");
    }

    @DataProvider(name = "invalidUser")
    public Object[][] invalidUser() throws IOException {
        return ExcelUtility.getDataFromExcel("InvalidUsers", "testdata/invalidUserID.xlsx");
    }
}
