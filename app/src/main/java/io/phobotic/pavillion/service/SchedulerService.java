package io.phobotic.pavillion.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import io.phobotic.pavillion.prefs.Preferences;

/**
 * Created by Jonathan Nelson on 6/3/16.
 * SchedulerServer starts the AlarmManager wakeup process for any background services that need
 * to be run on a regular basis
 */
public class SchedulerService extends IntentService {
    private static final String TAG = SchedulerService.class.getSimpleName();

    public SchedulerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "scheduler service starting");
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(this, EmailSenderService.REQUEST_CODE, new Intent(this, EmailSenderService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Preferences prefs = Preferences.getInstance(this);
        int hour = prefs.getSyncHours();
        int minutes = prefs.getSyncMinutes();

        //try to set the next sync period to later today.  If we have already passed the sync time
        //+ then push the update until tomorrow
        LocalDateTime now = new LocalDateTime(System.currentTimeMillis());
        LocalDateTime syncTime = new LocalDateTime(System.currentTimeMillis());
        syncTime = syncTime.withHourOfDay(hour);
        syncTime = syncTime.withMinuteOfHour(minutes);
        syncTime = syncTime.withSecondOfMinute(0);
        if (syncTime.isBefore(now)) {
            //move the sync period to the next day
            syncTime = syncTime.plus(new Period().withDays(1));
        }

        long wakeTime = syncTime.toDate().getTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, wakeTime, pi);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, wakeTime, pi);
        }
    }
}
