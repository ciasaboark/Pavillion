package io.phobotic.pavillion.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

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
    private static final int EMAIL_WAKE_REQUEST_CODE = 101;
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
            cancelAlreadyScheduledWake();
        }
    }

    private void cancelAlreadyScheduledWake() {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getPendingIntent());
    }

    private void scheduleWake() {
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
            Log.d(TAG, "No days have been selected, canceling any previously scheduled wakes");
            cancelAlreadyScheduledWake();
        } else {
            long nextAlarm = getNextAlarmTimestamp();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = getPendingIntent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarm, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi);
            }
        }
    }

    private PendingIntent getPendingIntent() {
        Intent i = new Intent(context, EmailSenderService.class);
        return PendingIntent.getService(context, EMAIL_WAKE_REQUEST_CODE, i, 0);
    }

    private long getNextAlarmTimestamp() {
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

        return trigger;
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
