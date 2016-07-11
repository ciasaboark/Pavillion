package io.phobotic.pavillion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonathan Nelson on 5/31/16.
 */
public class SearchesDatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "cd_searches";
    public static final int VERSION = 2;
    private static final String DB_NAME = "pavillion.sqlite";

    public SearchesDatabaseOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " +
                Columns._ID + " integer primary key not null, " +
                Columns.TIMESTAMP + " integer, " +
                Columns.LOCATION + " varchar(18), " +
                Columns.CD_MAIN + " varchar(1), " +
                Columns.CD_LEFT + " varchar(2), " +
                Columns.CD_MID + " varchar(2), " +
                Columns.CD_RIGHT + " varchar(2), " +
                Columns.PIC_FILE + " varchar(100), " +
                Columns.NOTIFICATION_SENT + " integer default 0" +
                ")");
    }
}
