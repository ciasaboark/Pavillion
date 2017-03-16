package io.phobotic.pavillion.fragment;


import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.adapter.RecordsCursorAdapter;
import io.phobotic.pavillion.database.Columns;
import io.phobotic.pavillion.database.SearchesDatabase;


/**
 * Created by Jonathan Nelson on 8/27/16.
 */

public class RecordsPreferenceFragment extends PreferenceFragment {
    private static final String TAG = RecordsPreferenceFragment.class.getSimpleName();
    private View root;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_records, container, false);
        init();
        return root;
    }

    private void init() {
        list = (ListView) root.findViewById(R.id.list);


        SearchesDatabase db = SearchesDatabase.getInstance(getActivity());

        Cursor cursor = db.getSearchesCursor();
        final CursorAdapter adapter = new RecordsCursorAdapter(getActivity(), cursor, false);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Item " + i + " clicked");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setView(R.layout.dialog_preview);
                final AlertDialog dialog = builder.create();
                dialog.show();
//                dialog.getWindow().setBackgroundDrawableResource(color.transparent);

                final ImageView largeView = (ImageView) dialog.findViewById(R.id.largeview);
                final ImageView closeButton = (ImageView) dialog.findViewById(R.id.close_button);
                final TextView timeTV = (TextView) dialog.findViewById(R.id.timestamp);

                Cursor cur = adapter.getCursor();
                cur.moveToPosition(i);
                String filename = cur.getString(cur.getColumnIndex(Columns.PIC_FILE));
                long timestamp = cur.getLong(cur.getColumnIndex(Columns.TIMESTAMP));
                Date date = new Date(timestamp);
                DateFormat df = new SimpleDateFormat();
                String dateString = df.format(date);
                timeTV.setText(dateString);

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                if (filename != null && !filename.equals("")) {
                    int dispOrientation = getResources().getConfiguration().orientation;
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();

                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(filename);
                        int picOrientation;
                        Bitmap imageBitmap;
                        if (bitmap.getWidth() >= bitmap.getHeight()) {
                            picOrientation = Configuration.ORIENTATION_LANDSCAPE;
                        } else {
                            picOrientation = Configuration.ORIENTATION_PORTRAIT;
                        }

                        if (dispOrientation == Configuration.ORIENTATION_PORTRAIT
                                && picOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            // TODO: 8/27/16 rotate image 90 deg
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90.0f);
                            int orgWidth = bitmap.getWidth();
                            int orgHeight = bitmap.getHeight();
                            imageBitmap = Bitmap.createBitmap(bitmap, 0, 0, orgWidth,
                                    orgHeight, matrix, true);
                        } else {
                            imageBitmap = bitmap;
                        }

                        int newWidth = imageBitmap.getWidth();
                        int newHeight = imageBitmap.getHeight();


                        largeView.setImageBitmap(imageBitmap);

                    } catch (Exception e) {
                        Log.e(TAG, "unable to open pic file from '" + filename + "': " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
