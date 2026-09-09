package dataProvider;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtility {

    public static Object[][] getDataFromExcel(String sheetName, String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);

        int rowCount = sheet.getPhysicalNumberOfRows();
        int columnCount = sheet.getRow(0).getLastCellNum();

        Object[][] data = new Object[rowCount - 1][columnCount];

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < columnCount; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    data[i - 1][j] = "";
                } else if (cell.getCellType() == CellType.STRING) {
                    data[i - 1][j] = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    data[i - 1][j] = String.valueOf(((Double) cell.getNumericCellValue()).longValue());
                } else {
                    data[i - 1][j] = cell.toString();
                }
            }
        }

        workbook.close();
        fis.close();
        return data;
    }
}
