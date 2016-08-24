package io.phobotic.pavillion.converter;

import android.content.Context;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.pavillion.database.SearchRecord;

/**
 * Created by Jonathan Nelson on 8/12/16.
 */

public class ExcelConverter {
    private static final String FILE_NAME = "location_lookups.xls";
    private final Context context;
    private List<SearchRecord> records;
    private static final short borderStyle = HSSFCellStyle.BORDER_THIN;
    private CellStyle headerStyle;
    private CellStyle recordStyle;
    private CellStyle recordHiddenStyle;
    private Workbook wb;
    private Sheet sheet;
    private static final String[] FIELDS = {
            "Location",
            "Timestamp",
            "CD Main",
            "CD Left",
            "CD Mid",
            "CD Right",
            "Lookups",
            "Needs Label?"
    };
    private Map<String, Integer> locationLookups;

    public ExcelConverter(Context context, List<SearchRecord> records) {
        this.context = context;
        this.records = records;
        init();
    }

    private void init() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("Location Lookups");
        sheet.setColumnWidth(0, 100);
        sheet.setColumnWidth(1, 50);
        sheet.setColumnWidth(2, 25);
        initStyles();
    }

    private void initStyles() {
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short)11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(borderStyle);
        headerStyle.setBorderTop(borderStyle);
        headerStyle.setBorderRight(borderStyle);
        headerStyle.setBorderLeft(borderStyle);
        headerStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        headerStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);


        recordStyle = wb.createCellStyle();
        recordStyle.setBorderBottom(borderStyle);
        recordStyle.setBorderTop(borderStyle);
        recordStyle.setBorderRight(borderStyle);
        recordStyle.setBorderLeft(borderStyle);
        recordStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setRightBorderColor(IndexedColors.GREY_40_PERCENT.index);
        recordStyle.setTopBorderColor(IndexedColors.GREY_40_PERCENT.index);

        recordHiddenStyle = wb.createCellStyle();
        recordHiddenStyle.setBorderBottom(borderStyle);
        recordHiddenStyle.setBorderTop(borderStyle);
        recordHiddenStyle.setBorderRight(borderStyle);
        recordHiddenStyle.setBorderLeft(borderStyle);
        recordHiddenStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        recordHiddenStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
    }

    public File convert() throws IOException {
        File file = null;

        sortRecords();
        countRecords();
        writeHeaders();
        writeRows();
//        resizeColumns();

        File dir = context.getFilesDir();
        file = new File(dir, "/excel/" + FILE_NAME);
        try {
            File parent = file.getParentFile();
            parent.mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return file;
    }

    private void resizeColumns() {
        for (int col = 0; col < FIELDS.length; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private void sortRecords() {
        Collections.sort(records, new Comparator<SearchRecord>() {
            @Override
            public int compare(SearchRecord searchRecord, SearchRecord t1) {
                return searchRecord.getLocation().compareTo(t1.getLocation());
            }
        });
    }

    private void countRecords() {
        locationLookups = new HashMap<>();
        for (SearchRecord record: records) {
            String location = record.getLocation();
            Integer counts = locationLookups.get(location);
            if (counts == null) {
                counts = 0;
            }
            locationLookups.put(location, ++counts);
        }
    }

    private void writeHeaders() {
        Row row = sheet.createRow(0);
        for (int col = 0; col < FIELDS.length; col++) {
            Cell cell = row.createCell(col);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(FIELDS[col]);
        }

    }

    private void writeRows() {
        int rowNum = 1;
        String lastLocation = null;
        for (SearchRecord record: records) {
            Row row = sheet.createRow(rowNum);
            if (record.getLocation().equals(lastLocation)) {
                writeHiddenRecord(row, record);
                CellStyle rowStyle = row.getRowStyle();
                if (rowStyle == null) {
                    //ugly workaround, just set the row height to 0
                    row.setZeroHeight(true);
                } else {
                    row.getRowStyle().setHidden(true);
                }
            } else {
                writeVisibleRecord(row, record);
            }

            lastLocation = record.getLocation();
            rowNum++;
        }
    }

    private void writeHiddenRecord(Row row, SearchRecord record) {
        writeRecord(row, record, recordHiddenStyle);
    }

    private void writeVisibleRecord(Row row, SearchRecord record) {
        writeRecord(row, record, recordStyle);
    }

    private void writeRecord(Row row, SearchRecord record, CellStyle style) {
        for (int col = 0; col < FIELDS.length; col++) {
            Cell cell = row.createCell(col);
            switch (col) {
                case 0:
                    cell.setCellValue(record.getLocation());
                    break;
                case 1:
                    Date date = new Date(record.getTimestamp());
                    DateFormat df = new SimpleDateFormat();
                    cell.setCellValue(df.format(date));
                    break;
                case 2:
                    cell.setCellValue(record.getCdMain());
                    break;
                case 3:
                    cell.setCellValue(record.getCdLeft());
                    break;
                case 4:
                    cell.setCellValue(record.getCdMiddle());
                    break;
                case 5:
                    cell.setCellValue(record.getCdRight());
                    break;
                case 6:
                    Integer lookups = locationLookups.get(record.getLocation());
                    cell.setCellValue(lookups);
                    break;

            }
            cell.setCellStyle(style);
        }
    }
}
