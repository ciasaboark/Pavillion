package io.phobotic.pavillion.report;

import android.content.Context;
import android.util.Log;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;

/**
 * Created by Jonathan Nelson on 10/30/16.
 */

public class SummaryByRange {
    private static final String TAG = SummaryByRange.class.getSimpleName();
    private final Context context;
    private Map<String, List<Long>> collisions;
    private Map<Integer, Integer> hourlyUsage;
    private Map<Integer, Map<Integer, Integer>> hourlyUsageByDay;
    private Map<Integer, Integer> weekdayUsage;
    private Map<Integer, Integer> dailyUsage;
    private List<String> seenLocations;
    private int totalRecords;
    private int totalUniqueRecords;
    private List<SearchRecord> searchRecords;
    private final long begin;
    private final long end;
    private boolean includeEmpties = false;

    public SummaryByRange setIncludeEmpties(boolean includeEmties) {
        this.includeEmpties = includeEmties;
        return this;
    }

    public SummaryByRange(Context context, long begin, long end) {
        this.context = context;
        this.begin = begin;
        this.end = end;
    }

    public Map<String, List<Long>> getCollisions() {
        return collisions;
    }

    public Map<Integer, Integer> getHourlyUsage() {
        return hourlyUsage;
    }

    public Map<Integer, Integer> getWeekdayUsage() {
        return weekdayUsage;
    }

    public List<String> getSeenLocations() {
        return seenLocations;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getTotalUniqueRecords() {
        return totalUniqueRecords;
    }


    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public void summarize() {
        init();
        processRecords();
        minify();
    }

    private void init() {
        getRecords();
        initBuckets();
    }

    public List<SearchRecord> getSearchRecords() {
        return searchRecords;
    }

    private void getRecords() {
        SearchesDatabase db = SearchesDatabase.getInstance(context);
        this.searchRecords = db.getRecordsBetween(begin, end);
    }

    private void initBuckets() {
        collisions = new HashMap<String, List<Long>>();
        initHourlyMap();
        initWeekdayMap();
        initDailyMap();
        initHourlyUsageByDay();
        seenLocations = new ArrayList<>();
        totalRecords = 0;
        totalUniqueRecords = 0;
    }

    public Map<Integer, Map<Integer, Integer>> getHourlyUsageByDay() {
        return hourlyUsageByDay;
    }

    private void initHourlyUsageByDay() {
        hourlyUsageByDay = new TreeMap<>();
        if (includeEmpties) {
            for (int i = 1; i <= 7; i++) { //days of week are indexed starting at 1
                Map<Integer, Integer> hours = new TreeMap<>();
                for (int j = 0; j < 24; j++) { //hour of day indexed starting at 0
                    hours.put(j, 0);
                }
                hourlyUsageByDay.put(i, hours);

            }
        }
    }

    private void initWeekdayMap() {
        weekdayUsage = new TreeMap<>();
        if (includeEmpties) {
            for (int i = 0; i < 7; i++) {
                weekdayUsage.put(i, 0);
            }
        }
    }

    private void processRecords() {
        for (SearchRecord record: searchRecords) {
            calculateCollisions(record);
            calculateSums(record);
            calculateHourlyUsage(record);
            calculateWeekdayUsage(record);
            calculateDailyUsage(record);
            calculateHourlyUsageByDay(record);
        }
    }

    private void calculateHourlyUsageByDay(SearchRecord record) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getTimestamp());
        Date date = new Date(record.getTimestamp());
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        Map<Integer, Integer> dayMap = hourlyUsageByDay.get(day);
        Integer hourValue = dayMap.get(hour);
        if (hourValue == null) hourValue = 0;
        dayMap.put(hour, ++hourValue);
    }

    private void minify() {
        minifyCollisions();
    }

    private void initHourlyMap() {
        hourlyUsage = new TreeMap<>();
        if (includeEmpties) {
            for (int i = 0; i < 24; i++) {
                hourlyUsage.put(i, 0);
            }
        }
    }

    public Map<Integer, Integer> getDailyUsage() {
        return dailyUsage;
    }

    private void initDailyMap() {
        dailyUsage = new TreeMap<>();
        if (includeEmpties) {
            LocalDate begin = new LocalDate(this.begin);
            LocalDate end = new LocalDate(this.end);
            int days = Days.daysBetween(begin, end).getDays() + 1;
            for (int i = 1; i <= days; i++) {
                dailyUsage.put(i, 0);
            }
        }
    }

    private void calculateCollisions(SearchRecord record) {
        //collapse the records into buckets by location
        String location = record.getLocation();
        long timestamp = record.getTimestamp();
        List<Long> previousRecords = collisions.get(location);
        if (previousRecords == null) {
            previousRecords = new ArrayList<>();
        }
        previousRecords.add(timestamp);
        collisions.put(location, previousRecords);
    }

    private void calculateSums(SearchRecord record) {
        String location = record.getLocation();
        if (!seenLocations.contains(location)) {
            totalUniqueRecords++;
            seenLocations.add(location);
        }

        totalRecords++;
    }

    private void calculateHourlyUsage(SearchRecord record) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getTimestamp());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        DateFormat df = new SimpleDateFormat();
        Date date = new Date(calendar.getTimeInMillis());
        Log.d(TAG, "record date " + df.format(date));
        Integer numRecords = hourlyUsage.get(hourOfDay);
        if (numRecords == null) numRecords = 0;
        hourlyUsage.put(hourOfDay, ++numRecords);
    }

    private void calculateWeekdayUsage(SearchRecord record) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getTimestamp());
        DateFormat df = new SimpleDateFormat();
        Date date = new Date(calendar.getTimeInMillis());
        Log.d(TAG, "record date " + df.format(date));
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        Integer numRecords = weekdayUsage.get(weekDay);
        if (numRecords == null)  numRecords = 0;
        weekdayUsage.put(weekDay, ++numRecords);
    }

    private void calculateDailyUsage(SearchRecord record) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getTimestamp());
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Integer numRecords = dailyUsage.get(dayOfMonth);
        if (numRecords == null) numRecords = 0;
        dailyUsage.put(dayOfMonth, ++numRecords);
    }

    private void minifyCollisions() {
        Iterator<Map.Entry<String, List<Long>>> it = collisions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Long>> entry = it.next();
            List<Long> searches = entry.getValue();
            if (searches.size() <= 1) {
                it.remove();
            }
        }
    }
}
