package io.phobotic.pavillion.report;

import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Jonathan Nelson on 10/28/16.
 */

public class SummaryReport {
    private static final String TAG = SummaryReport.class.getSimpleName();
    private static final String DEFAULT_NAME = "summary_report";
    private final SummaryByRange summary;
    private final Context context;
    private String name;

    public SummaryReport setName(String name) {
        this.name = name;
        return this;
    }

    public SummaryReport(Context context, SummaryByRange summary) {
        this.context = context;
        this.summary = summary;
    }

    public File generateReport() {

        return null;    //// TODO: 10/28/16
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public File toPDF() {
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setMinMargins(new PrintAttributes.Margins(19, 19, 19, 19))
                .build();
        PrintedPdfDocument pdf = new PrintedPdfDocument(context, printAttributes);
        
        return null;    // TODO: 11/7/16  
    }


    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total records in this period: " + summary.getTotalRecords());
        sb.append("\nTotal unique records in this period: " + summary.getTotalUniqueRecords());
        sb.append("\n\n");

        sb.append("Hourly usage (hour of day vs number of locations)\n");
        sb.append("------------\n");
        for (int key: summary.getHourlyUsage().keySet()) {
            sb.append(key + " -> " + summary.getHourlyUsage().get(key) + "\n");
        }
        sb.append("------------\n\n\n");

        sb.append("Daily usage (Day of week vs number of locations)\n");
        sb.append("------------\n");
        Calendar calendar = Calendar.getInstance();
        for (int key: summary.getWeekdayUsage().keySet()) {
            calendar.set(Calendar.DAY_OF_WEEK, key);
            DateFormat df = new SimpleDateFormat("E");
            String day = df.format(calendar.getTime());
            sb.append(day + " -> " + summary.getWeekdayUsage().get(key) + "\n");
        }
        sb.append("------------\n");

        return sb.toString();
    }
}
