package io.phobotic.pavillion.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import io.phobotic.pavillion.R;
import io.phobotic.pavillion.checkdigit.CheckDigits;

/**
 * Created by Jonathan Nelson on 8/21/16.
 */

public class LocationCard extends CardView {
    private View mLayout;
    private TextView location;
    private TextView mainCheckdigit;
    private TextView leftCheckdigit;
    private TextView midCheckdigit;
    private TextView rightCheckdigit;

    public LocationCard(Context context) {
        this(context, null);
    }

    public LocationCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mLayout = inflate(super.getContext(), R.layout.view_location, this);
        if (!isInEditMode()) {
            location = (TextView) findViewById(R.id.cd_location);
            mainCheckdigit = (TextView) findViewById(R.id.cd_main);
            leftCheckdigit = (TextView) findViewById(R.id.cd_left);
            midCheckdigit = (TextView) findViewById(R.id.cd_middle);
            rightCheckdigit = (TextView) findViewById(R.id.cd_right);
        }
    }

    public void setLocation(CheckDigits checkDigits) {
        location.setText(checkDigits.getLocation());
        mainCheckdigit.setText(checkDigits.getMainCheckdigit());
        leftCheckdigit.setText(checkDigits.getLeftCheckdigit());
        midCheckdigit.setText(checkDigits.getMiddleCheckdigit());
        rightCheckdigit.setText(checkDigits.getRightCheckdigit());
    }

    public void clear() {
        location.setText("");
        mainCheckdigit.setText("");
        leftCheckdigit.setText("");
        midCheckdigit.setText("");
        rightCheckdigit.setText("");
    }
}
