package io.phobotic.pavillion.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.pavillion.converter.ExcelConverter;
import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.email.Attachment;
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
    public static final String FLAG_SEND_TYPE = "send_type";
    public static final int FLAG_SEND_DAILY = 1;
    public static final int FLAG_SEND_WEEKLY = 2;
    public static final int FLAG_SEND_MONTHLY = 3;


    public EmailSenderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "starting email sender service");
        Bundle bundle = intent.getExtras();
        int sendType = bundle.getInt(FLAG_SEND_TYPE, -1);


        if (sendType == FLAG_SEND_DAILY
                || sendType == FLAG_SEND_WEEKLY
                || sendType == FLAG_SEND_MONTHLY) {
            SearchesDatabase db = SearchesDatabase.getInstance(this);
            Log.d(TAG, "pruning database of old entries");
            db.pruneDatabase();
        } else {
            Log.w(TAG, "service was woken, but no reports were marked to be sent.  Make sure to " +
                    "set bundle flag FLAG_SEND_TYPE");
        }

        if (sendType == FLAG_SEND_DAILY) {
            sendDailyReportIfEnabled(intent);
        }

        if (sendType == FLAG_SEND_WEEKLY) {
            sendWeeklyReportIfEnabled(intent);
        }

        if (sendType == FLAG_SEND_MONTHLY) {
            sendMonthlyReportIfEnabled(intent);
        }
    }

    private void sendWeeklyReportIfEnabled(Intent intent) {
        Log.d(TAG, "Send weekly report not finished yet");
        //// TODO: 10/28/16  
    }
    
    private void sendMonthlyReportIfEnabled(Intent intent) {
        Log.d(TAG, "Send monthly report not finished yet");
        //// TODO: 10/28/16  
    }

    private void sendDailyReportIfEnabled(Intent intent) {
        //only send the email if the setting has been enabled
        final SearchesDatabase db = SearchesDatabase.getInstance(this);
        Preferences prefs = Preferences.getInstance(this);
        boolean dailyReportsEnabled = prefs.shouldDailyReportsBeSent();
        boolean forceSend = intent.getBooleanExtra(FORCE_SEND, false);
        if (!(dailyReportsEnabled || forceSend)) {
            Log.d(TAG, "Skipping email send, disabled in settings");
        } else {
            Log.d(TAG, "searching for unsent records");
            List<SearchRecord> unsetRecords = db.getUnsentSearches();
            Log.d(TAG, "found " + unsetRecords.size() + " searches");
            final Context context = this;
            DateFormat df = SimpleDateFormat.getDateInstance();
            String dateString = df.format(new Date());
            
            List<SearchRecord> records = db.getUnsentSearches();
            Attachment attachment = null;
            
            try {
                ExcelConverter converter = new ExcelConverter(context, records);
                File file = converter.convert();
                List<Attachment> attachments = new ArrayList<>();
                attachment = new Attachment(file, file.getName());
                attachments.add(attachment);

                EmailSender emailSender = new EmailSender(this)
                        .setFailedListener(new EmailSender.EmailStatusListener() {
                            @Override
                            public void onEmailSendResult(String message, Object tag) {
                                //the email sender server will have sent a broadcast notification
                                Log.e(TAG, "Daily email could not be send, but no exception was thrown");
                            }
                        }, null)
                        .setSuccessListener(new EmailSender.EmailStatusListener() {
                            @Override
                            public void onEmailSendResult(String message, Object tag) {
                                Log.d(TAG, "Daily email was successfully sent");
                                if (tag instanceof List) {
                                    db.markRecordsAsSent((List<SearchRecord>) tag);
                                }

                            }
                        }, unsetRecords)
                        .setSubject("Check Digit Lookups for " + dateString)
                        .setBody("Location check digit lookups for " + dateString + "\n\n")
                        .withAttachments(attachments)
                        .send();
            } catch (IOException e) {
                String message = "Caught Exception while sending daily report email: " + e.getMessage();
                Log.e(TAG, message);
                e.printStackTrace();
            }
        }

        EmailScheduler emailScheduler = new EmailScheduler(this);
        emailScheduler.reschedule();
    }
}
