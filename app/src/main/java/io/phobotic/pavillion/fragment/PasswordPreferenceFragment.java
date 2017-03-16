package io.phobotic.pavillion.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.activity.SettingsActivity;
import io.phobotic.pavillion.prefs.Preferences;

/**
 * This fragment shows notification preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PasswordPreferenceFragment extends PreferenceFragment {
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
