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
import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.schedule.EmailScheduler;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class EmailSenderService extends IntentService {
    public static final int REQUEST_CODE = 1;
    private static final String TAG = EmailSenderService.class.getSimpleName();
    public static final String FORCE_SEND = "force_send";


    public EmailSenderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "starting email sender service");
        SearchesDatabase db = SearchesDatabase.getInstance(this);
        Log.d(TAG, "pruning database of old entries");
        db.pruneDatabase();

        //only send the email if the setting has been enabled
        Preferences prefs = Preferences.getInstance(this);
        boolean autoSendEnabled = prefs.shouldEmailsBeSent();
        boolean forceSend = intent.getBooleanExtra(FORCE_SEND, false);
        if (!(autoSendEnabled || forceSend)) {
            Log.d(TAG, "Skipping email send, disabled in settings");
        } else {
            Log.d(TAG, "searching for unsent records");
            List<SearchRecord> unsetRecords = db.getUnsentSearches();
            Log.d(TAG, "found " + unsetRecords.size() + " searches");
            final Context context = this;
            EmailSender emailSender = new EmailSender(this)
                    .setFailedListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(Object tag) {
                            //a broadcast notification was sent
                        }
                    }, null)
                    .setSuccessListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(Object tag) {
                            SearchesDatabase db = SearchesDatabase.getInstance(context);
                            if (tag instanceof List) {
                                db.markRecordsAsSent((List<SearchRecord>) tag);
                            }

                        }
                    }, unsetRecords)
                    .send();
        }

        EmailScheduler emailScheduler = new EmailScheduler(this);
        emailScheduler.reschedule();
    }
}
