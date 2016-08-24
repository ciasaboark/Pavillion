package io.phobotic.pavillion;


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
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.fragment.DayTimeFragment;
import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.service.EmailSenderService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        if (preference != null) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || EmailPreferenceFragment.class.getName().equals(fragmentName)
                || PasswordPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class EmailPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_email);
            setHasOptionsMenu(true);
            Preference server = findPreference(getString(R.string.pref_email_key_server));
            bindPreferenceSummaryToValue(server);

            Preference port = findPreference(getString(R.string.pref_email_key_port));
            bindPreferenceSummaryToValue(port);

            Preference username = findPreference(getString(R.string.pref_email_key_username));
            bindPreferenceSummaryToValue(username);

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
            bindPreferenceSummaryToValue(subject);

            Preference recipients = findPreference(getString(R.string.pref_email_key_recipients));
            bindPreferenceSummaryToValue(recipients);

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

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PasswordPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_password);
            setHasOptionsMenu(true);

            final Context context = this.getActivity();
            Preference changeButton = findPreference(context.getString(R.string.pref_password_key_change));
            changeButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    final EditText inputOne = new EditText(context);
                    inputOne.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    inputOne.setHint("Enter new password");
                    final EditText inputTwo = new EditText(context);
                    inputTwo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    inputTwo.setHint("Confirm password");
                    layout.addView(inputOne);
                    layout.addView(inputTwo);

                    AlertDialog changeDialog = new AlertDialog.Builder(context)
                            .setTitle("Change Password")
                            .setMessage("Change current password")
                            .setView(layout)
                            .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String passFirst = inputOne.getText().toString();
                                    String passSecond = inputTwo.getText().toString();
                                    if (!passFirst.equals(passSecond)) {
                                        AlertDialog passMismatchDialog = new AlertDialog.Builder(context)
                                                .setTitle("Error")
                                                .setMessage("Passwords do not match")
                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    } else {
                                        Preferences prefs = Preferences.getInstance(context);
                                        prefs.setSettingsPassword(passFirst);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return false;
                }
            });

            Preference resetButton = findPreference(getString(R.string.pref_password_key_reset));
            resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    AlertDialog resetDialog = new AlertDialog.Builder(context)
                            .setTitle("Reset Password")
                            .setMessage("Reset password to default of '1234'?")
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Preferences prefs = Preferences.getInstance(context);
                                    prefs.setSettingsPassword(null);
                                    Toast.makeText(context, "Password reset to default", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            })
                            .show();
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
    }
}
