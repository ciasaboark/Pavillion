package io.phobotic.pavillion.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

/**
 * Created by Jonathan Nelson on 9/4/16.
 */

public class PhotoLoader {
    private static final String TAG = PhotoLoader.class.getSimpleName();
    private int tag;
    private String filename;
    private OnPhotoLoadedListener listener;

    public PhotoLoader(String filename) {
        this.filename = filename;
    }

    public PhotoLoader setTag(int tag) {
        this.tag = tag;
        return this;
    }

    public PhotoLoader setOnImageLoadedListener(OnPhotoLoadedListener listener) {
        this.listener = listener;
        return this;
    }

    public PhotoLoader load() {
        BitmapLoader loader = new BitmapLoader();
        loader.execute(filename);
        return this;
    }

    private class BitmapLoader extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String filename = params[0];
            Bitmap bitmap = null;
            if (filename != null && !filename.equals("")) {
                File file = new File(filename);
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap orig = BitmapFactory.decodeFile(filename, options);
                    int origWidth = orig.getWidth();
                    int origHeight = orig.getHeight();
                    float scaleFactor;
                    if (origWidth < origHeight) {
                        scaleFactor = 200f / (float)origWidth;
                    } else {
                        scaleFactor = 200f / (float)origHeight;
                    }

                    int smallWidth = (int) (origWidth * scaleFactor);
                    int smallHeight = (int) (origHeight * scaleFactor);
                    bitmap = Bitmap.createScaledBitmap(orig, smallWidth, smallHeight, false);
                } catch (Exception e) {
                    Log.e(TAG, "Caught exception while loading photo '" + filename + "' from disk: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (listener != null) {
                listener.onPhotoLoaded(bitmap, tag);
            }
            return null;
        }
    }

    public interface OnPhotoLoadedListener {
        void onPhotoLoaded(Bitmap bitmap, int tag);
    }
}


