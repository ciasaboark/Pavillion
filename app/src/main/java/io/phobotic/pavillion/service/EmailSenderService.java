package io.phobotic.pavillion.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.List;

import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.email.ExcelFileBuilder;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class EmailSenderService extends IntentService {
    public static final int REQUEST_CODE = 1;
    private static final String TAG = EmailSenderService.class.getSimpleName();


    public EmailSenderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "starting email sender service");
        SearchesDatabase db = SearchesDatabase.getInstance(this);
        Log.d(TAG, "pruning database of old entries");
        db.pruneDatabase();
        Log.d(TAG, "searching for unsent records");
        List<SearchRecord> unsetRecords = db.getUnsentSearches();
        Log.d(TAG, "found " + unsetRecords.size() + " searches");
        HashMap<String, Integer> locationLookups = new HashMap<>();
        for (SearchRecord record : unsetRecords) {
            Integer numLookups = locationLookups.get(record.getLocation());
            if (numLookups == null) {
                numLookups = 1;
            } else {
                numLookups++;
            }
            locationLookups.put(record.getLocation(), numLookups);
        }

        Log.d(TAG, "searches condensed into " + locationLookups.size() + " unique locations");

        try {
            sendEmailWithLocations(locationLookups);
            db.markRecordsAsSent(unsetRecords);
        } catch (Exception e) {
            Log.e(TAG, "caught exception while sending email of searched locations: " + e.getMessage());
            Log.d(TAG, "skipping marking " + locationLookups.size() + " records as sent");
        }
    }

    private void sendEmailWithLocations(HashMap<String, Integer> locationLookups) {
        Log.d(TAG, "building excel workbook with location entries");
        Workbook workbook = new ExcelFileBuilder(locationLookups).buildFile();


    }

    public void scheduleEmailTask() {
        //todo read schedule time from prefs
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, );
    }
}
