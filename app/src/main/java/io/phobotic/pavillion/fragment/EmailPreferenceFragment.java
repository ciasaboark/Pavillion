package io.phobotic.pavillion.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.activity.SettingsActivity;
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.prefs.PreferenceBinder;

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

        Preference recipients = findPreference(getString(R.string.pref_email_key_recipients_set));
        recipients.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                EmailRecipientsFragment newFragment = EmailRecipientsFragment.newInstance();
                newFragment.show(ft, "dialog");

                return true;
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
                                            public void onEmailSendResult(final String message, Object tag) {
                                                ((Activity)context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog failDialog = new AlertDialog.Builder(context)
                                                                .setTitle("Email Failed")
                                                                .setMessage("Test email could not be sent\n" + message)
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
                                            public void onEmailSendResult(String message, Object tag) {
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
