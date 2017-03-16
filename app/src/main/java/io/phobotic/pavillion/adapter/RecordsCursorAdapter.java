package io.phobotic.pavillion.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.database.Columns;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.photo.PhotoLoader;

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
        View view = inflater.inflate(R.layout.view_search_record, viewGroup, false);
        RecordHolder holder = new RecordHolder();
        holder.card = (CardView) view.findViewById(R.id.card);
        holder.location = (TextView) view.findViewById(R.id.location);
        holder.timestamp = (TextView) view.findViewById(R.id.timestamp);
        holder.photo = (ImageView) view.findViewById(R.id.photo);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        String locString = cursor.getString(cursor.getColumnIndex(Columns.LOCATION));
        long epoctime = cursor.getLong(cursor.getColumnIndex(Columns.TIMESTAMP));
        String filename = cursor.getString(cursor.getColumnIndex(Columns.PIC_FILE));
        final int recordSent = cursor.getInt(cursor.getColumnIndex(Columns.NOTIFICATION_SENT));
        final int id = cursor.getInt(cursor.getColumnIndex(Columns._ID));

        Log.d(TAG, "bindview() for record " + id);

        final RecordHolder holder = (RecordHolder) view.getTag();

        holder.location.setText(locString);
        Date date = new Date(epoctime);
        SimpleDateFormat df = new SimpleDateFormat();
        String dateString = df.format(date);
        holder.timestamp.setText(dateString);

        //if this record has been sent then switch to a dark theme
        if (recordSent == 0) {
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.record_unsent_background));
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.record_unsent_text));
            holder.location.setTextColor(context.getResources().getColor(R.color.record_unsent_text));
        } else {
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.record_sent_background));
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.record_sent_text));
            holder.location.setTextColor(context.getResources().getColor(R.color.record_sent_text));
        }

        //blank the preview so we can load it in async
        holder.photo.setImageBitmap(null);
        holder.photo.setVisibility(View.INVISIBLE);
//        holder.photo.setBackgroundColor(context.getResources().getColor(R.color.preview_background));

        //load the photo async
        PhotoLoader photoLoader = new PhotoLoader(filename)
                .setTag(id)
                .setOnImageLoadedListener(new PhotoLoader.OnPhotoLoadedListener() {
                    @Override
                    public void onPhotoLoaded(final Bitmap bitmap, int tag) {
                        if (tag != id) {
                            Log.d(TAG, "bindview() took too long loading photo, current id: " + id + ", but photo belongs to id: " + tag);
                        } else {
                            Log.d(TAG, "bindview() photo finished loading for id " + id);
                            ((Activity)context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.photo.setImageBitmap(bitmap);
                                    holder.photo.setVisibility(View.VISIBLE);
//                                    if (recordSent == 0) {
//                                        holder.photo.setColorFilter(null);
//                                    } else {
//                                        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0.2f);
//                                        animator.setDuration(1000);
//                                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                            @Override
//                                            public void onAnimationUpdate(ValueAnimator animation) {
//                                                float saturation = (float) animation.getAnimatedValue();
//                                                ColorMatrix matrix = new ColorMatrix();
//                                                matrix.setSaturation(saturation);
//                                                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
//                                                holder.photo.setColorFilter(filter);
//                                            }
//                                        });
//                                        animator.start();
//                                    }
                                }
                            });
                        }
                    }
                })
                .load();

        //todo
    }

    private class RecordHolder {
        public CardView card;
        public TextView location;
        public TextView timestamp;
        public ImageView photo;
        public boolean recordSent;
    }
}



