package io.phobotic.pavillion.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.service.EmailSenderService;

/**
 * Created by Jonathan Nelson on 8/14/16.
 */

public class EmailScheduler {
    private static final String TAG = EmailScheduler.class.getSimpleName();
    private static final int MS_IN_MIN = 1000 * 60;
    private static final int MS_IN_HOUR = MS_IN_MIN * 60;
    private static final int MS_IN_DAY = MS_IN_HOUR * 24;
    private static final int EMAIL_DAILY_REQUEST_CODE = 101;
    private static final int EMAIL_WEEKLY_REQUEST_CODE = 102;
    private static final int EMAIL_MONTHLY_REQUEST_CODE = 103;
    final private Context context;
    Preferences prefs;
    private int hour;
    private int min;
    private boolean[] enabledDays;

    public EmailScheduler(Context context) {
        this.context = context;
    }

    public void reschedule() {
        Log.d(TAG, "rescheduling email service");
        prefs = Preferences.getInstance(context);
        boolean sendEmail = prefs.shouldEmailsBeSent();

        if (sendEmail) {
            scheduleWake();
        } else {
            Log.d(TAG, "email auto send disabled, canceling any previously scheduled wakes");
            cancelAlreadyScheduledAlarms();
        }
    }

    private void cancelAlreadyScheduledAlarms() {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getPendingIntent(EMAIL_DAILY_REQUEST_CODE));
        am.cancel(getPendingIntent(EMAIL_WEEKLY_REQUEST_CODE));
        am.cancel(getPendingIntent(EMAIL_MONTHLY_REQUEST_CODE));
    }

    private void scheduleWake() {
        //we know at least one method has been selected
        long dailyWakeTimestamp = -1;
        long weeklyWakeTimestamp = -1;
        long monthlyWakeTimeStamp = -1;
        if (prefs.shouldDailyReportsBeSent()) {
            dailyWakeTimestamp = getNextDailyAlarmTimestamp();
        }
        if (prefs.shouldWeeklyReportsBeSent()) {
            weeklyWakeTimestamp = getNextWeeklyAlarmTimestamp();
        }
        if (prefs.shouldMonthlyReportsBeSent()) {
            monthlyWakeTimeStamp = getNextMonthlyAlarmTimestamp();
        }


        if (dailyWakeTimestamp == -1 && weeklyWakeTimestamp == -1 && monthlyWakeTimeStamp == -1) {
            Log.d(TAG, "No email schedules have been set, canceling any previously scheduled wakes");
            cancelAlreadyScheduledAlarms();
        } else {
            //This is super lazy. Just schedule alarms for all three.  The first one to trigger wins
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent dailyPendingIntent = getPendingIntent(EMAIL_DAILY_REQUEST_CODE);
            PendingIntent weeklyPendingIntent = getPendingIntent(EMAIL_WEEKLY_REQUEST_CODE);
            PendingIntent monthlyPendingIntent = getPendingIntent(EMAIL_MONTHLY_REQUEST_CODE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dailyWakeTimestamp, dailyPendingIntent);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyWakeTimestamp, weeklyPendingIntent);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, monthlyWakeTimeStamp, monthlyPendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dailyWakeTimestamp, dailyPendingIntent);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, weeklyWakeTimestamp, weeklyPendingIntent);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, monthlyWakeTimeStamp, monthlyPendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, dailyWakeTimestamp, dailyPendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, weeklyWakeTimestamp, weeklyPendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, monthlyWakeTimeStamp, monthlyPendingIntent);
            }
        }
    }

    private PendingIntent getPendingIntent(int requestCode) {
        Intent i = new Intent(context, EmailSenderService.class);
        int sendType;
        switch (requestCode) {
            case EMAIL_DAILY_REQUEST_CODE:
                sendType = EmailSenderService.FLAG_SEND_DAILY;
                break;
            case EMAIL_WEEKLY_REQUEST_CODE:
                sendType = EmailSenderService.FLAG_SEND_WEEKLY;
                break;
            case EMAIL_MONTHLY_REQUEST_CODE:
                sendType = EmailSenderService.FLAG_SEND_MONTHLY;
                break;
            default:
                sendType = -1;
        }

        i.putExtra(EmailSenderService.FLAG_SEND_TYPE, sendType);
        PendingIntent pi = PendingIntent.getService(context, requestCode, i, 0);
        return pi;
    }

    private long getNextMonthlyAlarmTimestamp() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        //shift to the hour cutoff
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.add(Calendar.SECOND, 1);



        long timestamp;
        //there are 3 possiblilities here, either we use a later time this month, a later time today
        //+ or a later time next month

        if (currentDayOfMonth == lastDayOfMonth) {
            //we are on the last day of the month, but have we passed the time cutoff?
            long laterToday = calendar.getTimeInMillis();
            if (now <= laterToday) {
                //if now is before the wake time, then use today
                timestamp = laterToday;
            } else {
                //else shift to next month
                calendar.add(Calendar.MONTH, 1);
                timestamp = calendar.getTimeInMillis();
            }
        } else {
            //this is not the last day of the month, so just jump ahead
            calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
            timestamp = calendar.getTimeInMillis();
        }

        Date date = new Date(timestamp);

        return timestamp;
    }

    private long getNextWeeklyAlarmTimestamp() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timestamp;
        //there are 3 possiblilities here, either we use a later time this week, a later time today
        //+ or a later time next week

        //calendar days of week are indexed starting at 1
        if (currentDayOfWeek == 7) {
            //we are on the last day of the week, but have we passed the time cutoff?
            long laterToday = calendar.getTimeInMillis();
            if (now <= laterToday) {
                //if now is before the wake time, then use today
                timestamp = laterToday;
            } else {
                //else shift to next week
                calendar.add(Calendar.DATE, 7);
                timestamp = calendar.getTimeInMillis();
            }
        } else {
            //this is not the last day of the week, so just jump ahead
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            timestamp = calendar.getTimeInMillis();
        }

        Date date = new Date(timestamp);

        return timestamp;
    }

    private long getNextDailyAlarmTimestamp() {
        long wakeAt = -1;
        String timeString = prefs.getEmailTime();
        String[] timeParts = timeString.split(":");
        hour = Integer.parseInt(timeParts[0]);
        min = Integer.parseInt(timeParts[1]);

        enabledDays = new boolean[7];

        String days = prefs.getEmailDays();
        String[] dayParts = days.split(":");
        for (int i = 0; i < dayParts.length; i++) {
            enabledDays[i] = ("T".equals(dayParts[i]) ? true : false);
        }

        Log.d(TAG, "email schedule enabled for days (Sunday index 0): " + enabledDays);
        if (noDaysSelected(enabledDays)) {
            Log.d(TAG, "Daily emails have been enabled, but no days have been selected");
        } else {

            long epochTime = System.currentTimeMillis();
            Long trigger = null;
            Calendar cal = Calendar.getInstance();

            while (trigger == null) {
                int today = cal.get(Calendar.DAY_OF_WEEK) - 1; //calendar is indexed at 1
                boolean todayEnabled = enabledDays[today];

                if (todayEnabled) {
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, min);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long dayTrigger = cal.getTimeInMillis();
                    if (dayTrigger >= System.currentTimeMillis()) {
                        Log.d(TAG, "next wake will occur at: " + cal.getTime().toString());
                        trigger = dayTrigger;
                    }
                }

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            wakeAt = trigger;
        }

        Date date = new Date(wakeAt);

        return wakeAt;
    }

    private boolean noDaysSelected(boolean[] days) {
        return !atLeastOneDaySelected(days);
    }

    private boolean atLeastOneDaySelected(boolean[] days) {
        boolean atLeastOneDaySelected = false;
        for (Boolean isDaySelected : days) {
            if (isDaySelected) {
                atLeastOneDaySelected = true;
                break;
            }
        }

        return atLeastOneDaySelected;
    }
}
