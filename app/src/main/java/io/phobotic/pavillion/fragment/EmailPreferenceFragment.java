package io.phobotic.pavillion.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import java.util.List;

import io.phobotic.pavillion.prefs.PreferenceBinder;
import io.phobotic.pavillion.R;
import io.phobotic.pavillion.SettingsActivity;
import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.service.EmailSenderService;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EmailPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_email);
        setHasOptionsMenu(true);
        Preference server = findPreference(getString(R.string.pref_email_key_server));
        PreferenceBinder.bindPreferenceSummaryToValue(server);

        Preference port = findPreference(getString(R.string.pref_email_key_port));
        PreferenceBinder.bindPreferenceSummaryToValue(port);

        Preference username = findPreference(getString(R.string.pref_email_key_username));
        PreferenceBinder.bindPreferenceSummaryToValue(username);

        //use a separate preference change listener for the password to make sure it does not
        //+ show as plain text
        final EditTextPreference password = (EditTextPreference) findPreference(getString(R.string.pref_email_key_password));
        Preference.OnPreferenceChangeListener passwordListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String summary = String.valueOf(password.getEditText().getTransformationMethod()
                        .getTransformation(o.toString(), password.getEditText()));
                if ("".equals(summary)) {
                    summary = getString(R.string.pref_email_password_summary);
                }
                preference.setSummary(summary);
                return true;
            }
        };
        password.setOnPreferenceChangeListener(passwordListener);
        passwordListener.onPreferenceChange(password, PreferenceManager
                .getDefaultSharedPreferences(password.getContext())
                .getString(password.getKey(), ""));

        Preference subject = findPreference(getString(R.string.pref_email_key_subject));
        PreferenceBinder.bindPreferenceSummaryToValue(subject);

        Preference recipients = findPreference(getString(R.string.pref_email_key_recipients));
        PreferenceBinder.bindPreferenceSummaryToValue(recipients);

        Preference timeButton = findPreference(getString(R.string.pref_email_key_time));
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

        Preference testButton = findPreference(getString(R.string.pref_email_key_test));
        testButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getActivity();
                final AlertDialog sendEmailDialog = new AlertDialog.Builder(context)
                        .setTitle("Send test email")
                        .setMessage("Test email settings by sending an empty message?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                EmailSender emailSender = new EmailSender(context)
                                        .setFailedListener(new EmailSender.EmailStatusListener() {
                                            @Override
                                            public void onEmailSendResult(Object tag) {
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog failDialog = new AlertDialog.Builder(context)
                                                                .setTitle("Email Failed")
                                                                .setMessage("Test email could not be sent")
                                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        dialogInterface.dismiss();
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                });

                                            }
                                        }, null)
                                        .setSuccessListener(new EmailSender.EmailStatusListener() {
                                            @Override
                                            public void onEmailSendResult(Object tag) {
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog successDialog = new AlertDialog.Builder(context)
                                                                .setTitle("Email Sent")
                                                                .setMessage("Test email was sent successfully")
                                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        dialogInterface.dismiss();
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                });
                                            }
                                        }, null)
                                        .sendTestEmail();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }).show();
                return false;
            }
        });

        Preference sendNow = findPreference(getString(R.string.pref_email_key_send_now));
        sendNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context context = getActivity();

                final SearchesDatabase db = SearchesDatabase.getInstance(context);
                final List<SearchRecord> records = db.getUnsentSearches();
                if (records.size() == 0) {
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
                                    PendingIntent pi = PendingIntent.getService(context, 0,
                                            intent, 0);
                                    AlarmManager alarmManager =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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
                return false;
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
}
