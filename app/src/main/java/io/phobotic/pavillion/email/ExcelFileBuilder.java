package io.phobotic.pavillion.email;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.Map;

/**
 * Created by Jonathan Nelson on 6/3/16.
 */
public class ExcelFileBuilder {
    private static final String TAG = ExcelFileBuilder.class.getSimpleName();
    private static final short borderStyle = HSSFCellStyle.BORDER_THIN;
    private Map<String, Integer> locationLookups;
    private CellStyle headerStyle;
    private Workbook workbook;

    public ExcelFileBuilder(Map<String, Integer> locationLookups) {
        this.locationLookups = locationLookups;
    }

    public Workbook buildFile() {
        File file = null;
        workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Locations");
        generateStyles();
        writeHeaders(sheet);
        writeLocations(sheet);
        return workbook;
    }

    private void generateStyles() {
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        headerStyle.setBorderBottom(borderStyle);
        headerStyle.setBorderTop(borderStyle);
        headerStyle.setBorderRight(borderStyle);
        headerStyle.setBorderLeft(borderStyle);
    }

    private void writeHeaders(Sheet sheet) {
        String[] headers = {
                "Location",
                "Number of Searches",
                "Label Needed"
        };

        //write out the header row
        Row row = sheet.createRow(0);
        int cellNum = 0;
        for (; cellNum < headers.length; cellNum++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(headers[cellNum]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeLocations(Sheet sheet) {
        int rowNum = 1;
        for (String location : locationLookups.keySet()) {
            Row row = sheet.createRow(rowNum++);
            int numLookups = locationLookups.get(location);
            Cell locationCell = row.createCell(0);
            Cell lookupsCell = row.createCell(1);
            locationCell.setCellValue(location);
            lookupsCell.setCellValue(numLookups);
        }
    }
}
