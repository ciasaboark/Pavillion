package io.phobotic.pavillion.schedule;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jonathan Nelson on 10/28/16.
 */

public class CalendarHelper {
    public static final String TAG = CalendarHelper.class.getSimpleName();


    public static long getFirstDayOfWeek(long timestamp) {
        return getFirstDayOfWeek(timestamp, 0);
    }
    /**
     * Returns a timestamp representing the beginning of a week relative to the given timestamp
     * @param timestamp
     * @return
     */
    public static long getFirstDayOfWeek(long timestamp, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, offset);
        DateFormat df = new SimpleDateFormat();

        //shift the calendar to the first day of the week
        while (calendar.get(Calendar.DAY_OF_WEEK) > calendar.getFirstDayOfWeek()) {
            calendar.add(Calendar.DATE, -1); // Substract 1 day until first day of week.
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();
        Log.d(TAG, "found first day of week as " + df.format(new Date(start)));

        return start;
    }

    public static long getLastDayOfWeek(long timestamp) {
        return getLastDayOfWeek(timestamp, 0);
    }

    /**
     * Returns a timestamp representing the ending of a week relative to the given timestamp
     * @param timestamp
     * @return
     */
    public static long getLastDayOfWeek(long timestamp, int offset) {
        long weekStart = getFirstDayOfWeek(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(weekStart);
        calendar.add(Calendar.MONTH, offset);

        //shift the calendar to the last day of the week
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.MILLISECOND, -1);

        long end = calendar.getTimeInMillis();
        DateFormat df = new SimpleDateFormat();
        Log.d(TAG, "found last day of week as " + df.format(new Date(end)));

        return end;
    }

    public static long getFirstDayOfMonth(long timestamp) {
        return getFirstDayOfMonth(timestamp, 0);
    }

    /**
     * Returns a timestamp representing the beginning of a month relative to the given timestamp
     * @param timestamp
     * @return
     */
    public static long getFirstDayOfMonth(long timestamp, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, offset);

        DateFormat df = new SimpleDateFormat();

        //shift the calendar to the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();
        Log.d(TAG, "found first day of month as " + df.format(new Date(start)));

        return start;
    }

    public static long getLastDayOfMonth(long timestamp) {
        return getLastDayOfMonth(timestamp, 0);
    }

    /**
     * Returns a timestamp representing the end of a month relative to the given timestamp
     * @param timestamp
     * @return
     */
    public static long getLastDayOfMonth(long timestamp, int offset) {
        long monthStart = getFirstDayOfMonth(timestamp, offset);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(monthStart);

        //shift the calendar to the last day of the month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);

        long end = calendar.getTimeInMillis();
        DateFormat df = new SimpleDateFormat();
        Log.d(TAG, "found last day of month as " + df.format(new Date(end)));

        return end;
    }
}
