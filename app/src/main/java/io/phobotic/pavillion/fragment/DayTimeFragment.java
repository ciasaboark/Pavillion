package io.phobotic.pavillion.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TimePicker;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.prefs.DefaultPrefs;
import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.schedule.EmailScheduler;
import io.phobotic.pavillion.view.DayPickerWeek;

/**
 * Created by Jonathan Nelson on 8/14/16.
 */

public class DayTimeFragment extends DialogFragment {
    private TimePicker tp;
    private DayPickerWeek dayPickerWeek;

    public static DayTimeFragment newInstance() {
        DayTimeFragment fragment = new DayTimeFragment();
        return fragment;
    }

    private void initView(View view) {
        initTimePicker(view);
        initDayPicker(view);
    }

    private void initTimePicker(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedTime = prefs.getString(getString(R.string.pref_email_key_time), null);
        int hour, min;
        if (storedTime == null) {
            hour = DefaultPrefs.EMAIL_TIME_HOUR;
            min = DefaultPrefs.EMAIL_TIME_MINUTE;
        } else {
            String[] pieces = storedTime.split(":");
            hour = Integer.parseInt(pieces[0]);
            min = Integer.parseInt(pieces[1]);
        }
        tp = (TimePicker) view.findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tp.setHour(hour);
            tp.setMinute(min);
        } else {
            tp.setCurrentHour(hour);
            tp.setCurrentMinute(min);
        }
    }

    private void initDayPicker(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedDays = prefs.getString(getString(R.string.pref_email_key_days), DefaultPrefs.EMAIL_DAYS);

        dayPickerWeek = (DayPickerWeek) view.findViewById(R.id.dayPicker);
        dayPickerWeek.fromPersistentString(storedDays);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_day_time_picker, null);
        final Preferences prefs = Preferences.getInstance(getActivity());
        initView(view);
        builder.setView(view)
                .setTitle("Choose Day and Time")
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //save time
                                int hour, min;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    hour = tp.getHour();
                                    min = tp.getMinute();
                                } else {
                                    hour = tp.getCurrentHour();
                                    min = tp.getCurrentMinute();
                                }
                                String time = hour + ":" + min;
                                prefs.setEmailTime(time);

                                //save days
                                String days = dayPickerWeek.toPersistentString();
                                prefs.setEmailDays(days);

                                EmailScheduler scheduler = new EmailScheduler(getActivity());
                                scheduler.reschedule();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        return builder.create();
    }
}
