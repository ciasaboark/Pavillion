package io.phobotic.pavillion.report;

import android.content.Context;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Converts a two dimensional array into an Excel file
 * Created by Jonathan Nelson on 9/29/16.
 */
public class ExcelArrayWriter {
    private static final String TAG = ExcelArrayWriter.class.getSimpleName();
    private static final String DEFAULT_FILENAME = "data";
    private final Context context;
    private String customFilename;
    private Object[][] data;
    private Object[] headers;
    private CellStyle dataCellStyle;
    private CellStyle headerCellStyle;

    public ExcelArrayWriter(Context context, Object[] headers, Object[][] data) {
        this.context = context;
        this.headers = headers;
        this.data = data;
    }

    public File generate() {
        File file = null;
        Workbook wb = new HSSFWorkbook();
        Font headerFont = wb.createFont();

        short borderStyle = HSSFCellStyle.BORDER_THIN;

        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(borderStyle);
        headerCellStyle.setBorderTop(borderStyle);
        headerCellStyle.setBorderRight(borderStyle);
        headerCellStyle.setBorderLeft(borderStyle);
        headerCellStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerCellStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerCellStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerCellStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);

        dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderBottom(borderStyle);
        dataCellStyle.setBorderTop(borderStyle);
        dataCellStyle.setBorderRight(borderStyle);
        dataCellStyle.setBorderLeft(borderStyle);
        dataCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        dataCellStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        dataCellStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        dataCellStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        dataCellStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);

        Sheet sheet = wb.createSheet("Case Caps");
        writeHeaders(sheet);
        writeData(sheet);
        resizeColumns(sheet);

        // Write the output to a file
        try {
            String postfix = "";
            String outputFile = getFilenameWithPath();
            if (!outputFile.toUpperCase().endsWith(".XLS")) {
                postfix = ".xls";
            }

            outputFile = outputFile + postfix;

            //get file path
            file = new File(outputFile);
            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to write excel file: " + e.getMessage());
        }

        return file;
    }

    private void writeHeaders(Sheet sheet) {
        //write out the header row
        Row row = sheet.createRow(0);
        int cellNum = 0;
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(String.valueOf(headers[i]));
            cell.setCellStyle(headerCellStyle);
            cellNum++;
        }
    }

    private void writeData(Sheet sheet) {
        int rowNum = 1;
        //write out the data rows
        for (int row = 0; row < data.length; row++) {
            Row productRow = sheet.createRow(rowNum++);

            for (int col = 0; col < data[row].length; col++) {
                Cell cell = productRow.createCell(col);
                cell.setCellValue(String.valueOf(data[row][col]));
                cell.setCellStyle(dataCellStyle);
            }
        }
    }

    private void resizeColumns(Sheet sheet) {
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String getFilename() {
        String filename;
        if (customFilename != null) {
            return customFilename;
        } else {
            return DEFAULT_FILENAME;
        }
    }

    private String getFilenameWithPath() throws IOException {
        String filename = getFilename();

        File baseDir = context.getFilesDir();
        File reportsDir = new File(baseDir, "/reports/");
        File outputFile =  new File(reportsDir + filename);

        //create the parent folder if needed
        reportsDir.mkdirs();
        if (!reportsDir.isDirectory()) {
            //if the reports directory still does not exist then we have no place to store the
            //+ generated excel file
            String message = "Unable to create reports directory '" +
                    reportsDir.getAbsolutePath() + "': ";
            Log.e(TAG, message);
            throw new IOException(message);
        }

        return outputFile.getAbsolutePath();
    }

    public ExcelArrayWriter setFilename(String filename) {
        this.customFilename = filename;
        return this;
    }
}
