package io.phobotic.pavillion.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.codetroopers.betterpickers.OnDialogDismissListener;
import com.codetroopers.betterpickers.expirationpicker.ExpirationPickerBuilder;
import com.codetroopers.betterpickers.expirationpicker.ExpirationPickerDialogFragment;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.report.SummaryByRange;
import io.phobotic.pavillion.schedule.CalendarHelper;

import static android.os.Build.VERSION.SDK;
import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by Jonathan Nelson on 10/29/16.
 */

public class UsageActivity extends AppCompatActivity {
    private SearchesDatabase searchesDatabase;
    private SummaryByRange monthSummary;
    private long timestamp = System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);
        setupActionBar();

        View dateBox = findViewById(R.id.date_box);
        final Context context = this;
        dateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SDK_INT >= 24) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);

                    DatePickerDialog dialog = new DatePickerDialog(UsageActivity.this, null,
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH))

                    {
                        @Override
                        protected void onCreate(Bundle savedInstanceState)
                        {
                            super.onCreate(savedInstanceState);
                            int year = getContext().getResources()
                                    .getIdentifier("android:id/year", null, null);
                            if(year != 0){
                                View yearPicker = findViewById(year);
                                if(yearPicker != null){
                                    yearPicker.setVisibility(View.GONE);
                                }
                            }
                        }
                    };

                    dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            timestamp = cal.getTimeInMillis();

                            buildViews();
                        }
                    });

                    dialog.show();
                }
            }
        });




        buildViews();

    }

    private void buildViews() {
        searchesDatabase = SearchesDatabase.getInstance(this);
        buildSummaries();
        //show the graphs early to prevent the error from flashing on screen
        showGraphs();
        initGraphs();

        if (monthSummary.getSearchRecords().size() == 0) {
            hideGraphs();
        }
    }

    private void hideGraphs() {
        View graphs = findViewById(R.id.graph_scroll_view);
        graphs.setVisibility(View.GONE);

        View error = findViewById(R.id.usage_error);
        error.setVisibility(View.VISIBLE);
    }

    private void showGraphs() {
        View graphs = findViewById(R.id.graph_scroll_view);
        graphs.setVisibility(View.VISIBLE);

        View error = findViewById(R.id.usage_error);
        error.setVisibility(View.GONE);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void buildSummaries() {
        long begin = CalendarHelper.getFirstDayOfMonth(timestamp);
        long end = CalendarHelper.getLastDayOfMonth(timestamp);
        monthSummary = new SummaryByRange(this, begin, end);
        monthSummary.setIncludeEmpties(true);
        monthSummary.summarize();
    }

    private void initGraphs() {
        initTotalUsage();

        if (monthSummary.getSearchRecords().size() > 0) {
            initDailyUsageGraph();
            initScatterPlotGraph();
            initTestGraph();
        }
    }

    private void initTestGraph() {
        final XYPlot plot = new XYPlot(this, "test plot");
        plot.setRenderMode(Plot.RenderMode.USE_BACKGROUND_THREAD);
        plot.layout(0, 0, 500, 200);
        plot.setMinimumHeight(200);
        plot.setMinimumWidth(500);

        SearchRecord firstRecord = monthSummary.getSearchRecords().get(0);
        plot.setTitle("");
        plot.setRangeLabel("Lookups");
        plot.setDomainLabel("Day of month");
        plot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBorderPaint().setColor(Color.TRANSPARENT);
        plot.setBackgroundColor(Color.TRANSPARENT);
        plot.setPlotMargins(0, 0, 0, 0);


        int daysInMonth = monthSummary.getDailyUsage().keySet().size();
        List<Number> calendarDays = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            calendarDays.add(i);
        }

        List<Number> values = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i <= daysInMonth; i++) {
            int dayValue = monthSummary.getDailyUsage().get(i);
            values.add(dayValue);
        }

        final Number[] domainLabels = calendarDays.toArray(new Number[]{});
        Number[] series1Numbers = values.toArray(new Number[]{});

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Usage by day");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        int lineColor = Color.GRAY;
        int vertexColor = getResources().getColor(R.color.colorAccent);
        int fillColor = getResources().getColor(R.color.graph_fill);
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(lineColor, vertexColor, fillColor, null);



        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));


        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        //add pretty suffixes to the dates.  Conversion code from
        //+ http://stackoverflow.com/questions/6810336/is-there-a-way-in-java-to-convert-an-integer-to-its-ordinal
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                int day = (int)domainLabels[i];

                String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
                String dayString;
                int mod = day % 100;
                switch (mod) {
                    case 11:
                    case 12:
                    case 13:
                        dayString =  day + "th";
                        break;
                    default:
                        String suffix = sufixes[day % 10];
                        dayString =  day + suffix;

                }
                return toAppendTo.append(dayString);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(i);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });


        plot.setDrawingCacheEnabled(true);
        plot.buildDrawingCache();
        Bitmap bitmap = plot.getDrawingCache();
        plot.redraw();
        plot.setDrawingCacheEnabled(false);
    }

    private void initTotalUsage() {
        TextView monthTitle = (TextView) findViewById(R.id.usage_month);
        TextView yearTitle = (TextView) findViewById(R.id.usage_year);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        DateFormat monthFormatter = new SimpleDateFormat("MMMM");
        DateFormat yearFormatter = new SimpleDateFormat("yyyy");

        String month = monthFormatter.format(new Date(calendar.getTimeInMillis()));
        String year = yearFormatter.format(new Date(calendar.getTimeInMillis()));
        monthTitle.setText(month);
        yearTitle.setText(year);


        TextView totalLookups = (TextView) findViewById(R.id.total_lookups);
        TextView uniqueLookups = (TextView) findViewById(R.id.unique_locations);
        totalLookups.setText(String.valueOf(monthSummary.getTotalRecords()));
        uniqueLookups.setText(String.valueOf(monthSummary.getTotalUniqueRecords()));
    }

    private void initScatterPlotGraph() {
        XYPlot plot = (XYPlot) findViewById(R.id.hourly_usage_graph);
        plot.setTitle("");
        plot.setRangeLabel("");
        plot.setDomainLabel("");
        plot.getGraph().setPaddingLeft(20f);

        plot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBorderPaint().setColor(Color.TRANSPARENT);
        plot.setBackgroundColor(Color.TRANSPARENT);
//        int left_pad = plot.getGraph().getpaint() - 1;
//        mTripPlot.getGraphWidget().setPadding(left_pad, top_pad, right_pad, bottom_pad);
//        plot.setPlotMargins(100, 0, 0, 0);


        //the range is the hour of the day
        List<Number> range = new ArrayList<>();
        for (int i = 0; i <= 24; i++) {
            range.add(i);
        }

        //the domain is the days of the week
        final Number[] domain = {
                1, 2, 3, 4, 5, 6, 7
        };

        final String[] dayLabels = {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };

        List<Number> days = new ArrayList<>();
        List<Number> hour = new ArrayList<>();

        //convert the daily hour map into a list
        Map<Integer, Map<Integer, Integer>> dayMap = monthSummary.getHourlyUsageByDay();
        for (int dayKey: dayMap.keySet()) {
            Map<Integer, Integer> hourMap = dayMap.get(dayKey);
            for (int hourKey: hourMap.keySet()) {
                int hourValue = hourMap.get(hourKey);
                if (hourValue != 0) {
                    days.add(dayKey);
                    hour.add(hourKey);
                }
            }
        }

        Number[] dayData = days.toArray(new Number[]{});
        Number[] hourData = hour.toArray(new Number[]{});

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)

        XYSeries series1 = new XYSeriesShimmer(days, hour, 0, "Usage");
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.TRANSPARENT,
                getResources().getColor(R.color.graph_vertex), Color.TRANSPARENT, null);
        series1Format.getVertexPaint().setStrokeWidth(PixelUtils.dpToPix(20));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.setDomainBoundaries(1, 7, BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);
        plot.setRangeBoundaries(0, 23, BoundaryMode.FIXED);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                //convert the 24 hr time to 12 format, i will be indexed at 0
                int i = Math.round(((Number) obj).floatValue());
                String hour;
                if (i == 0) {
                    hour = 12 + "AM";
                } else if (i < 12) {
                    hour = i + " AM";
                } else if (i == 12) {
                    hour = i + "PM";
                } else {
                    hour = i - 12 + " PM";
                }
                return toAppendTo.append(hour);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                int i = Math.round(((Number) obj).floatValue());
                String day = dayLabels[i -1];  //days will be indexed starting at 1
                return toAppendTo.append(day);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });


    }

    private void initDailyUsageGraph() {
        // initialize our XYPlot reference:
        XYPlot plot = (XYPlot) findViewById(R.id.daily_usage_graph);
        SearchRecord firstRecord = monthSummary.getSearchRecords().get(0);
        plot.setTitle("");
        plot.setRangeLabel("Lookups");
        plot.setDomainLabel("Day of month");
        plot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.getBorderPaint().setColor(Color.TRANSPARENT);
        plot.setBackgroundColor(Color.TRANSPARENT);
        plot.setPlotMargins(0, 0, 0, 0);




        int daysInMonth = monthSummary.getDailyUsage().keySet().size();
        List<Number> calendarDays = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            calendarDays.add(i);
        }

        float maxVal = 0;
        List<Number> values = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            int dayValue = monthSummary.getDailyUsage().get(i);
            values.add(dayValue);

            if (dayValue > maxVal) {
                maxVal = (float) dayValue;
            }
        }

        int subdivide = (int) Math.ceil(maxVal / 5);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL, subdivide);

        final Number[] domainLabels = calendarDays.toArray(new Number[]{});
        Number[] series1Numbers = values.toArray(new Number[]{});

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Usage by day");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        int lineColor = Color.GRAY;
        int vertexColor = getResources().getColor(R.color.colorAccent);
        int fillColor = getResources().getColor(R.color.graph_fill);
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(lineColor, vertexColor, fillColor, null);



        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));


        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                int day = (int)domainLabels[i];
                //conversion code from http://stackoverflow.com/questions/6810336/is-there-a-way-in-java-to-convert-an-integer-to-its-ordinal
                String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
                String dayString;
                int mod = day % 100;
                switch (mod) {
                    case 11:
                    case 12:
                    case 13:
                        dayString =  day + "th";
                        break;
                    default:
                        String suffix = sufixes[day % 10];
                        dayString =  day + suffix;

                }
                return toAppendTo.append(dayString);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(i);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        plot.setDrawingCacheEnabled(true);
        plot.buildDrawingCache();
        Bitmap bitmap = plot.getDrawingCache();
        plot.redraw();
        plot.setDrawingCacheEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public class XYSeriesShimmer implements XYSeries {
        private List<Number> dataX;
        private List<Number> dataY;
        private int seriesIndex;
        private String title;

        public XYSeriesShimmer(List<Number> datasource, int seriesIndex, String title) {
            this.dataY = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        public XYSeriesShimmer(List<Number> datasourceX, List<Number> datasourceY, int seriesIndex, String title) {
            this.dataX = datasourceX;
            this.dataY = datasourceY;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return dataY.size();
        }

        @Override
        public Number getY(int index) {
            return dataY.get(index);
        }

        @Override
        public Number getX(int index) {
            return dataX.get(index);
        }

        public void updateData(List<Number> datasourceX){ //dont need to use this cause, the reference is only stored, modifying the datasource externally will cause this to be updated as well
            this.dataY=datasourceX;
        }

        public void updateData(List<Number> datasourceX, List<Number> datasourceY){ //dont need to use this cause, the reference is only stored, modifying the datasource externally will cause this to be updated as well
            this.dataX=datasourceX;
            this.dataY=datasourceY;
        }

    }
}
