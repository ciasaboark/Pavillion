package io.phobotic.pavillion.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.phobotic.pavillion.prefs.Preferences;

/**
 * Created by Jonathan Nelson on 5/31/16.
 */
public class SearchesDatabase {
    private static final String TAG = SearchesDatabase.class.getSimpleName();
    public static final String[] DB_PROJECTION = new String[]{
            Columns._ID,
            Columns.TIMESTAMP,
            Columns.LOCATION,
            Columns.CD_MAIN,
            Columns.CD_LEFT,
            Columns.CD_MID,
            Columns.CD_RIGHT,
            Columns.PIC_FILE,
            Columns.NOTIFICATION_SENT
    };
    private static final int DB_PROJECTION_ID = 0;
    private static final int DB_PROJECTION_TIMESTAMP = 1;
    private static final int DB_PROJECTION_LOCATION = 2;
    private static final int DB_PROJECTION_CD_MAIN = 3;
    private static final int DB_PROJECTION_CD_LEFT = 4;
    private static final int DB_PROJECTION_CD_MID = 5;
    private static final int DB_PROJECTION_CD_RIGHT = 6;
    private static final int DB_PROJECTION_PIC_FILE = 7;
    private static final int DB_PROJECTION_NOTIFICATION_SENT = 8;

    private static final int DB_MAX_RECORD_AGE = 365;
    private static final int DB_MAX_PHOTO_AGE = 30;
    private static SearchesDatabase instance;
    private final SQLiteDatabase db;
    private Context context;

    public static SearchesDatabase getInstance(Context ctx) {
        if (instance == null) {
            instance = new SearchesDatabase(ctx);
        }

        return instance;
    }

    private SearchesDatabase(Context ctx) {
        context = ctx;
        SearchesDatabaseOpenHelper dbHelper = new SearchesDatabaseOpenHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    private Cursor getSearchesCursor(long begin, long end) {
        assert begin >= 0;
        assert end >= 0;

        String[] args = {String.valueOf(begin), String.valueOf(end)};
        String selection = Columns.TIMESTAMP + " >= ? AND " + Columns.TIMESTAMP + " <= ?";
        Cursor cursor = db.query(SearchesDatabaseOpenHelper.TABLE_NAME, null, selection, args,
                null, null, Columns.TIMESTAMP, null);
        return cursor;
    }

    public Cursor getSearchesCursor() {
        return getOrderedSearchesCursor(Columns.TIMESTAMP);
    }

    private Cursor getOrderedSearchesCursor(String order) {
        return db.query(SearchesDatabaseOpenHelper.TABLE_NAME, null, null, null, null,
                null, order + " DESC", null);
    }

    public void pruneDatabase() {
        int prunedSearches = 0;
        int prunedPics = 0;

        Preferences prefs = Preferences.getInstance(context);

        //delete records older than 365 days
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, -DB_MAX_RECORD_AGE);
        long yearRecords = calendar.getTimeInMillis();

        //delete photos older than 30 days
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, -DB_MAX_PHOTO_AGE);
        long monthRecords = calendar.getTimeInMillis();

        deleteRecordsOlderThan(yearRecords);
        deletePhotosOlderThan(monthRecords);
    }

    private int deleteRecordsOlderThan(long timestamp) {
        int deletedRecords = 0;
        int deletedPhotos = 0;
        Cursor cursor = null;

        try {
            cursor = getOldSearchesCursor(timestamp);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(DB_PROJECTION_ID);
                long recordTimestamp = cursor.getLong(DB_PROJECTION_TIMESTAMP);
                String picfile = cursor.getString(DB_PROJECTION_PIC_FILE);
                long recordAge = System.currentTimeMillis() - recordTimestamp;
                boolean deleteFile = false;
                boolean deleteRecord = false;

                if (deleteFileIfExists(picfile)) {
                    deletedPhotos++;
                }
                deleteSearchWithId(id);
                deletedRecords++;
            }

            Log.d(TAG, "deleted " + deletedRecords + " search records and " +
                    deletedPhotos + " photos");
        } catch (Exception e) {
            Log.e(TAG, "caught exception while pruning old records from searches database: " + e.getMessage());
            e.printStackTrace();
        }

        return deletedRecords;
    }

    private int deletePhotosOlderThan(long timestamp) {
        int deletedPhotos = 0;
        Cursor cursor = null;

        try {
            cursor = getOldSearchesCursor(timestamp);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(DB_PROJECTION_ID);
                long recordTimestamp = cursor.getLong(DB_PROJECTION_TIMESTAMP);
                String picfile = cursor.getString(DB_PROJECTION_PIC_FILE);
                long recordAge = System.currentTimeMillis() - recordTimestamp;
                boolean deleteFile = false;
                boolean deleteRecord = false;

                if (deleteFileIfExists(picfile)) {
                    deletedPhotos++;
                }
            }

            Log.d(TAG, "deleted " + deletedPhotos + " photos");
        } catch (Exception e) {
            Log.e(TAG, "caught exception while pruning old photos from file system: " + e.getMessage());
            e.printStackTrace();
        }

        return deletedPhotos;
    }

    private Cursor getOldSearchesCursor(long timestamp) {
        String[] args = {String.valueOf(timestamp)};
        String selection = Columns.TIMESTAMP + " < ?";
        Cursor cursor = db.query(SearchesDatabaseOpenHelper.TABLE_NAME, null, selection, args,
                null, null, Columns.TIMESTAMP, null);
        return cursor;
    }

    private boolean deleteFileIfExists(String filename) {
        boolean fileDeleted = false;
        try {
            File f = new File(filename);
            if (f.exists()) {
                f.delete();
                fileDeleted = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "caught exception while deleting old photo in file '" + filename + "': " + e.getMessage());
        }

        return fileDeleted;
    }

    private void deleteSearchWithId(int id) {
        String selection = Columns._ID + " = ?";
        db.delete(SearchesDatabaseOpenHelper.TABLE_NAME, selection, new String[]{String.valueOf(id)});
    }

    public long insertSearch(SearchInstance instance) {
        ContentValues cv = new ContentValues();
        cv.put(Columns.TIMESTAMP, instance.getTimestamp());
        cv.put(Columns.LOCATION, instance.getLocation());
        cv.put(Columns.CD_MAIN, instance.getCdMain());
        cv.put(Columns.CD_LEFT, instance.getCdLeft());
        cv.put(Columns.CD_MID, instance.getCdMiddle());
        cv.put(Columns.CD_RIGHT, instance.getCdRight());
        String picFile = instance.getPicFile() == null ? "" : instance.getPicFile().toString();
        cv.put(Columns.PIC_FILE, picFile);
        cv.put(Columns.NOTIFICATION_SENT, 0);

        long rowID = db.insertWithOnConflict(SearchesDatabaseOpenHelper.TABLE_NAME, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted search for " + instance.getLocation() + " at time: " +
                instance.getTimestamp() + " as row " + rowID);

        return rowID;
    }

    public List<SearchRecord> getRecordsBetween(long start, long end) {
        List<SearchRecord> records = new ArrayList<>();
        String [] args = {String.valueOf(start), String.valueOf(end)};
        String selection = Columns.TIMESTAMP + " BETWEEN ? AND  ?";
        Cursor cursor;
        try {
            cursor = db.query(SearchesDatabaseOpenHelper.TABLE_NAME, null, selection, args,
                    null, null, Columns.TIMESTAMP, null);
            if (cursor.getCount() == 0) {
                Log.d(TAG, "Query finished OK, but no records found");
            } else {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(DB_PROJECTION_ID);
                    long timestamp = cursor.getLong(DB_PROJECTION_TIMESTAMP);
                    String location = cursor.getString(DB_PROJECTION_LOCATION);
                    String cdMain = cursor.getString(DB_PROJECTION_CD_MAIN);
                    String cdLeft = cursor.getString(DB_PROJECTION_CD_LEFT);
                    String cdMid = cursor.getString(DB_PROJECTION_CD_MID);
                    String cdRight = cursor.getString(DB_PROJECTION_CD_RIGHT);
                    String picFilename = cursor.getString(DB_PROJECTION_PIC_FILE);
                    File picFile = new File(picFilename);

                    SearchRecord searchRecord = new SearchRecord(id, timestamp, location, cdMain, cdLeft, cdMid, cdRight, picFile);
                    records.add(searchRecord);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while building list of records between " + start +
                    " and " + end + ": " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    public List<SearchRecord> getUnsentSearches() {
        List<SearchRecord> records = new ArrayList<>();
        String[] args = {String.valueOf(0)};
        String selection = Columns.NOTIFICATION_SENT + " = ?";
        Cursor cursor;

        try {
            cursor = db.query(SearchesDatabaseOpenHelper.TABLE_NAME, null, selection, args,
                    null, null, Columns.TIMESTAMP, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(DB_PROJECTION_ID);
                long timestamp = cursor.getLong(DB_PROJECTION_TIMESTAMP);
                String location = cursor.getString(DB_PROJECTION_LOCATION);
                String cdMain = cursor.getString(DB_PROJECTION_CD_MAIN);
                String cdLeft = cursor.getString(DB_PROJECTION_CD_LEFT);
                String cdMid = cursor.getString(DB_PROJECTION_CD_MID);
                String cdRight = cursor.getString(DB_PROJECTION_CD_RIGHT);
                String picFilename = cursor.getString(DB_PROJECTION_PIC_FILE);
                File picFile = new File(picFilename);

                SearchRecord searchRecord = new SearchRecord(id, timestamp, location, cdMain, cdLeft, cdMid, cdRight, picFile);
                records.add(searchRecord);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while building list of unsent searches: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }


    public void markRecordsAsSent(List<SearchRecord> records) {
        for (SearchRecord record : records) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Columns.NOTIFICATION_SENT, 1);
            String selection = Columns._ID + " = ? ";
            String[] selectionArgs = {String.valueOf(record.getId())};
            db.update(SearchesDatabaseOpenHelper.TABLE_NAME, contentValues, selection, selectionArgs);
        }
    }

    public void updateSearchFileName(long searchId, File pictureFile) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Columns.PIC_FILE, pictureFile.toString());
        String selection = Columns._ID + " = ? ";
        String[] selectionArgs = {String.valueOf(searchId)};
        db.updateWithOnConflict(SearchesDatabaseOpenHelper.TABLE_NAME, contentValues, selection, selectionArgs, SQLiteDatabase.CONFLICT_ROLLBACK);
    }
}


