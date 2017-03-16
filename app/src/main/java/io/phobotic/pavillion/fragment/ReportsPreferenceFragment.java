package io.phobotic.pavillion.fragment;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.activity.SettingsActivity;
import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.email.Attachment;
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.report.SummaryByRange;
import io.phobotic.pavillion.report.SummaryReport;
import io.phobotic.pavillion.schedule.CalendarHelper;
import io.phobotic.pavillion.service.EmailSenderService;


/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ReportsPreferenceFragment extends PreferenceFragment {
    private static final String TAG = ReportsPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_reports);
        setHasOptionsMenu(true);

        final Preference daily = findPreference(getString(R.string.pref_report_daily));
        daily.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean && (Boolean) newValue) {
                    //daily report will be sent
                    daily.setSummary("Daily reports will be sent");
                } else {
                    daily.setSummary("Daily reports will not be sent");
                }
                return true;
            }
        });

        final Preference weekly = findPreference(getString(R.string.pref_report_weekly));
        weekly.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean && (Boolean) newValue) {
                    weekly.setSummary("Weekly summaries will be sent every Saturday");
                } else {
                    weekly.setSummary("Weekly summaries will not be sent");
                }
                return true;
            }
        });

        final Preference monthly = findPreference(getString(R.string.pref_report_monthly));
        monthly.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean && (Boolean) newValue) {
                    monthly.setSummary("Monthly summaries will be sent");
                } else {
                    monthly.setSummary("Monthly summaries will not be sent");
                }
                return true;
            }
        });

        Preference timeButton = findPreference(getString(R.string.pref_report_daily_time));
        timeButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = DayTimeFragment.newInstance();
                newFragment.show(ft, "dialog");
                return false;
            }
        });

        Preference sendDailyNow = findPreference(getString(R.string.pref_report_daily_now_key));
        sendDailyNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getActivity();

                final SearchesDatabase db = SearchesDatabase.getInstance(context);
                final List<SearchRecord> records = db.getUnsentSearches();
                if (records.isEmpty()) {
                    AlertDialog noRecordsDialog = new AlertDialog.Builder(context)
                            .setTitle("No Records")
                            .setMessage("There are no unsent records in the database")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                } else {
                    AlertDialog sendNowDialog = new AlertDialog.Builder(context)
                            .setTitle("Send records")
                            .setMessage("There are " + records.size() + " unsent records in " +
                                    "the database.  Send them now?")
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.button_send, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(context, EmailSenderService.class);
                                    intent.putExtra(EmailSenderService.FORCE_SEND, true);
                                    intent.putExtra(EmailSenderService.FLAG_SEND_TYPE,
                                            EmailSenderService.FLAG_SEND_DAILY);
                                    PendingIntent pi = PendingIntent.getService(context, 0,
                                            intent, 0);
                                    AlarmManager alarmManager = (AlarmManager) context
                                            .getSystemService(Context.ALARM_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                                System.currentTimeMillis(), pi);
                                    } else {
                                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                                System.currentTimeMillis(), pi);
                                    }
                                }
                            })
                            .show();
                }
                return true;
            }
        });

        Preference sendWeeklyNow = findPreference(getString(R.string.pref_report_weekly_now_key));
        sendWeeklyNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //the weekly summary report should only be sent if there are records for the
                //+ current week

                SearchesDatabase db = SearchesDatabase.getInstance(getActivity());
                long now = System.currentTimeMillis();
                long start = CalendarHelper.getFirstDayOfWeek(now);
                long end = CalendarHelper.getLastDayOfWeek(now);

                List<SearchRecord> records = db.getRecordsBetween(start, end);
                if (records.isEmpty()) {
                    displayDialogWithMessage("Weekly Summary", "There are no records to send " +
                            "in the weekly summary");
                } else {
                    //// TODO: 10/28/16
                }

                return true;
            }
        });

        Preference sendMonthlyNow = findPreference(getString(R.string.pref_report_monthly_now_key));
        sendMonthlyNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //the monthly summary report should only be sent if there are records for the
                //+ current month
                SearchesDatabase db = SearchesDatabase.getInstance(getActivity());
                long now = System.currentTimeMillis();
                long start = CalendarHelper.getFirstDayOfMonth(now);
                long end = CalendarHelper.getLastDayOfMonth(now);

                List<SearchRecord> records = db.getRecordsBetween(start, end);
                if (records.isEmpty()) {
                    displayDialogWithMessage("Monthy Summary", "There are no records to send in " +
                            "the monthy summary");
                } else {
                    DateFormat df = new SimpleDateFormat("MMM - yyyy");
                    String reportName = df.format(new Date()) + " monthly report";
                    SummaryByRange summary = new SummaryByRange(getActivity(), start, end);
                    SummaryReport report = new SummaryReport(getActivity(), summary)
                            .setName(reportName);
                    File reportFile = report.generateReport();
                    List<Attachment> attachments = new ArrayList<>();

                    if (reportFile != null) attachments.add(new Attachment(reportFile, reportFile.getName()));


                    EmailSender emailSender = new EmailSender(getActivity())
                            .setFailedListener(new EmailSender.EmailStatusListener() {
                                @Override
                                public void onEmailSendResult(String message, Object tag) {
                                    // TODO: 10/28/16
                                }
                            }, null)
                            .setSuccessListener(new EmailSender.EmailStatusListener() {
                                @Override
                                public void onEmailSendResult(String message, Object tag) {
                                    // TODO: 10/28/16
                                }
                            }, null)
                            .withAttachments(attachments)
                            .setSubject("Check Digit Lookup Summary for " + df.format(new Date()))
                            .setBody(report.toPlainText())
                            .send();
                }

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDialogWithMessage(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
