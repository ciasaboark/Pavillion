package io.phobotic.pavillion.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.database.Columns;
import io.phobotic.pavillion.database.SearchesDatabase;

/**
 * Created by Jonathan Nelson on 8/27/16.
 */

public class RecordsCursorAdapter extends CursorAdapter {
    private static final String TAG = CursorAdapter.class.getSimpleName();
    private SearchesDatabase db;
    private LayoutInflater inflater;

    public RecordsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        db = SearchesDatabase.getInstance(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(R.layout.view_search_record, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String locString = cursor.getString(cursor.getColumnIndex(Columns.LOCATION));
        long epoctime = cursor.getLong(cursor.getColumnIndex(Columns.TIMESTAMP));
        String filename = cursor.getString(cursor.getColumnIndex(Columns.PIC_FILE));
        int recordSent = cursor.getInt(cursor.getColumnIndex(Columns.NOTIFICATION_SENT));

        TextView location = (TextView) view.findViewById(R.id.location);
        TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        location.setText(locString);
        Date date = new Date(epoctime);
        SimpleDateFormat df = new SimpleDateFormat();
        String dateString = df.format(date);
        timestamp.setText(dateString);

        if (filename != null && !filename.equals("")) {
            File file = new File(filename);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
                photo.setImageBitmap(bitmap);

            } catch (Exception e) {
                Log.e(TAG, "Caught exception while loading photo '" + filename + "' from disk: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
