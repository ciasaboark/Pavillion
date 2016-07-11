package io.phobotic.pavillion.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class Preferences {
    private static final String PREFS_FILE = "prefs";
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
        sharedPreferences = context.getSharedPreferences(PREFS_FILE, context.MODE_PRIVATE);
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
        return sharedPreferences.getLong(Keys.MAX_PHOTO_AGE, DefaultPrefs.DB_MAX_PIC_AGE);
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
        return sharedPreferences.getLong(Keys.MAX_SEARCH_AGE, DefaultPrefs.DB_MAX_SEARCH_AGE);
    }

    /**
     * Sets the maximum age of a search record in ms.  Records older than this value will be
     * deleted during the database pruning process.  If the record includes a reference to a photo
     * snapshot then that photo will also be deleted from disk.
     *
     * @param maxAge
     */
    public void setMaxSearchAge(long maxAge) {
        sharedPreferences.edit().putLong(Keys.MAX_SEARCH_AGE, maxAge);
    }

    private static class Keys {
        public static final String MAX_PHOTO_AGE = "max_photo_age";
        public static final String MAX_SEARCH_AGE = "max_search_age";
        public static final String EMAIL_TIME_HOUR = "email_time_hour";
        public static final String EMAIL_TIME_MIN = "email_time_min";
        public static final String EMAIL_RECEPIENT_LIST = "email_recepient_list";
        public static final String EMAIL_SERVER_ADDRESS = "email_server_address";
        public static final String EMAIL_SERVER_PORT = "email_server_port";
        public static final String EMAIL_SUBJECT = "email_subject";
    }


}
