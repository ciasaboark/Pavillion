package io.phobotic.pavillion.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.email.EmailRecipient;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class Preferences {
//    private static final String PREFS_FILE = "prefs";
    private static final String TAG = Preferences.class.getSimpleName();
    private static Preferences instance;
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public static Preferences getInstance(@NotNull Context ctx) {
        if (instance == null) {
            instance = new Preferences(ctx);
        }

        return instance;
    }

    private Preferences(Context ctx) {
        this.context = ctx;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getEmailServer() {
        return sharedPreferences.getString(context.getString(R.string.pref_email_key_server), null);
    }

    public int getEmailPort() {
        String portString = sharedPreferences.getString(
                context.getString(R.string.pref_email_key_port), "-1");
        if (portString.equals("")) portString = "-1";
        return Integer.valueOf(portString);
    }

    public String getEmailUsername() {
        return sharedPreferences.getString(context.getString(R.string.pref_email_key_username), null);
    }

    public String getEmailPassword() {
        return sharedPreferences.getString(context.getString(R.string.pref_email_key_password), null);
    }

    /**
     * This method will only be kept around for the next one or two version releases, after that
     * it (and the key it references) will be removed.
     */
    @Deprecated
    private void upgradeOldRecipientsList() {
        List<EmailRecipient> emailRecipientList = new ArrayList<>();

        String oldRecipients = sharedPreferences.getString(
                context.getResources().getString(R.string.pref_email_key_recipients), null);
        if (oldRecipients == null) {
            Log.d(TAG, "Old recipients list not found, skipping conversion");
        } else {
            Log.d(TAG, "Converting old recipients list to new format");
            try {
                String[] recipients = oldRecipients.split(",");
                for (String recipient : recipients) {
                    EmailRecipient emailRecipient = new EmailRecipient(recipient);
                    //Since the daily report was all that was available at the time email recipients
                    //+ were stored in this format then all previously exisitng emails should receive
                    //+ at least the daily report
                    emailRecipient.setReceiveDailyReport(true);
                    emailRecipientList.add(emailRecipient);
                }
            } catch (Exception e) {
                Log.e(TAG, "caught exception converting old recipients string to new list format.  " +
                        "Old recipients list will be discarded and new empty list inserted");
            }
            sharedPreferences.edit().remove(context.getResources().getString(R.string.pref_email_key_recipients)).apply();
            setEmailRecipients(emailRecipientList);
        }
    }

    public List<EmailRecipient> getEmailRecipients() {
        try {
            upgradeOldRecipientsList();
        } catch (Exception e) {
            Log.e(TAG, "caught exception converting old recipients list: " + e.getMessage());
        }

        List<EmailRecipient> recipients = new ArrayList<>();
        try {
            String json = sharedPreferences.getString(context.getString(
                    R.string.pref_email_key_recipients_set), "");
            if (json.equals("")) {
                throw new Exception("Found empty string when converting saved recipients list to " +
                        "JSON.  This is normal if no recipients have been defined yet");
            }

            Type listType = new TypeToken<List<EmailRecipient>>() {
            }.getType();
            Gson gson = new Gson();
            recipients = gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e(TAG, "Unable to parse recipients list string as JSON");
        }

        return recipients;
    }

    public void setEmailRecipients(List<EmailRecipient> recipients) {
        Type listType = new TypeToken<List<EmailRecipient>>(){}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(recipients, listType);
        sharedPreferences.edit().putString(context.getString(
                R.string.pref_email_key_recipients_set), json).apply();
    }

    public String getEmailSubject() {
        return sharedPreferences.getString(context.getString(R.string.pref_email_key_subject),
                context.getString(R.string.pref_email_subject_default));
    }

    /**
     * Returns the hour of the day (in 24hr format) that the email update should be sent.
     */
    public int getSyncHours() {
        return sharedPreferences.getInt(Keys.EMAIL_TIME_HOUR, DefaultPrefs.EMAIL_TIME_HOUR);
    }

    /**
     * Sets the hour of the day that the email update should be sent
     *
     * @param syncHours the integer value of the hour.  Must be a value between 0 and 23 inclusive.
     */
    public void setSyncHours(int syncHours) {
        if (syncHours < 0 || syncHours > 23) {
            throw new IllegalArgumentException("Sync hour must be between 0 and 23 inclusive");
        }

        sharedPreferences.edit().putInt(Keys.EMAIL_TIME_HOUR, syncHours).apply();
    }

    /**
     * Returns the minute portion of the time the email update should be sent.
     */
    public int getSyncMinutes() {
        return sharedPreferences.getInt(Keys.EMAIL_TIME_MIN, DefaultPrefs.EMAIL_TIME_MINUTE);
    }

    /**
     * Set the minute portion of the time the email should be sent.  Should be between 0 and 59 inclusive.
     *
     * @param syncMinutes
     */
    public void setSyncMinutes(int syncMinutes) {
        if (syncMinutes < 0 || syncMinutes > 59) {
            throw new IllegalArgumentException("Sync minutes should be between 0 and 59 inclusive");
        }
        sharedPreferences.edit().putInt(Keys.EMAIL_TIME_MIN, syncMinutes).apply();
    }

    /**
     * The maximum age of a snapshot in ms.  Photos older than this will be deleted from disk during
     * the database pruning process.  This is separate from the max search age.  If the max search
     * age is less than the max photo age then the photo file will be deleted when the search
     * record is purged.  If the max photo age is less than the max search age the then photo
     * will be deleted but the search record will be kept until it's max age is reached.
     *
     * @return
     */
    public long getMaxPhotosAge() {
        long age =  sharedPreferences.getLong(Keys.MAX_PHOTO_AGE, DefaultPrefs.DB_MAX_PIC_AGE);
        return age;
    }

    /**
     * Set the maximum age of a photo snapshot in ms. Photos older than this will be deleted from
     * disk during the database pruning process.  This is separate from the max search age.  If the
     * max search age is less than the max photo age then the photo file will be deleted when the
     * search record is purged.  If the max photo age is less than the max search age the then photo
     * will be deleted but the search record will be kept until it's max age is reached.
     */
    public void setMaxPhotosAge(long maxAge) {
        sharedPreferences.edit().putLong(Keys.MAX_PHOTO_AGE, maxAge).apply();
    }

    /**
     * Returns the maximum age of a search record in ms.  Records older than this value will be
     * deleted during the database pruning process.  If the record includes a reference to a photo
     * snapshot then that photo will also be deleted from disk.
     *
     * @return
     */
    public long getMaxSearchAge() {
        long age =  sharedPreferences.getLong(Keys.MAX_SEARCH_AGE, DefaultPrefs.DB_MAX_SEARCH_AGE);
        return age;
    }

    /**
     * Sets the maximum age of a search record in ms.  Records older than this value will be
     * deleted during the database pruning process.  If the record includes a reference to a photo
     * snapshot then that photo will also be deleted from disk.
     *
     * @param maxAge
     */
    public void setMaxSearchAge(long maxAge) {
        sharedPreferences.edit().putLong(Keys.MAX_SEARCH_AGE, maxAge).apply();
    }

    /**
     * Get the current password to access the settings activity, or the default password if none
     * has been set.
     */
    public String getSettingsPassword() {
        return sharedPreferences.getString(context.getString(R.string.pref_password_key_password), DefaultPrefs.SETTINGS_PASSWORD);
    }

    public boolean isSettingsPasswordSet() {
        String curPass = sharedPreferences.getString(context.getString(R.string.pref_password_key_password), null);
        return curPass != null;
    }

    /**
     * Set a new password for access to the settings activity.
     * @param password The new password for the Settings activity.  This password is stored as
     *                 plaintext
     */
    public void setSettingsPassword(String password) {
        if (password == null || password.equals("")) {
            sharedPreferences.edit().remove(context.getString(R.string.pref_password_key_password)).apply();
        } else {
            sharedPreferences.edit().putString(context.getString(R.string.pref_password_key_password), password).apply();
        }
    }

    /**
     * Returns true if _any_ email report will be sent out
     */
    public boolean shouldEmailsBeSent() {
        boolean dailyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_daily), false);
        boolean weeklyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_daily), false);
        boolean monthlyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_daily), false);

        return dailyReportsEnabled || weeklyReportsEnabled || monthlyReportsEnabled;
    }

    public boolean shouldDailyReportsBeSent() {
        boolean dailyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_daily), false);

        return dailyReportsEnabled;
    }

    public boolean shouldWeeklyReportsBeSent() {
        boolean weeklyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_weekly), false);

        return weeklyReportsEnabled;
    }

    public boolean shouldMonthlyReportsBeSent() {
        boolean monthlyReportsEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.pref_report_monthly), false);

        return monthlyReportsEnabled;
    }

    public void setEmailTime(String syncTime) {
        sharedPreferences.edit().putString(context.getString(R.string.pref_email_key_time),
                syncTime).apply();
    }

    public String getEmailTime() {
        String time = sharedPreferences.getString(context.getString(R.string.pref_email_key_time),
                null);
        if (time == null) {
            time = DefaultPrefs.EMAIL_TIME_HOUR + ":" + DefaultPrefs.EMAIL_TIME_MINUTE;
        }

        return time;
    }

    public void setEmailDays(String days) {
        sharedPreferences.edit().putString(context.getString(R.string.pref_email_key_days),
                days).apply();
    }

    public String getEmailDays() {
        String days = sharedPreferences.getString(context.getString(R.string.pref_email_key_days),
                DefaultPrefs.EMAIL_DAYS);
        return days;
    }

    private static class Keys {
        public static final String MAX_PHOTO_AGE = "max_photo_age";
        public static final String MAX_SEARCH_AGE = "max_search_age";
        public static final String EMAIL_TIME_HOUR = "email_time_hour";
        public static final String EMAIL_TIME_MIN = "email_time_min";
        public static final String EMAIL_RECEPIENT_LIST = "email_recepient_list";
    }


}
