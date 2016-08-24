package io.phobotic.pavillion.prefs;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class DefaultPrefs {
    public static final String ADMIN_PASSWORD = "abc123";

    /*
     * Email Settings
     */
    public static final int EMAIL_TIME_HOUR = 17;
    public static final int EMAIL_TIME_MINUTE = 0;
    public static final String EMAIL_DAYS = "F:T:T:T:T:T:F";

    public static final String EMAIL_RECEPIENT_LIST = "";
    public static final String EMAIL_SERVER_ADDRESS = "";
    public static final int EMAIL_SERVER_PORT = 0;
    public static final String EMAIL_SUBJECT = "Recent Location Lookups";

    /**
     * Database Settings
     */
    //how long to keep pictures associated with a search
    public static final long DB_MAX_PIC_AGE = 1000 * 60 * 60 * 24 * 31;
    //how long to keep the queries associated with a search
    public static final long DB_MAX_SEARCH_AGE = DB_MAX_PIC_AGE;

    /**
     * Password for settings
     */
    public static final String SETTINGS_PASSWORD = "1234";
}
