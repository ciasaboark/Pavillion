package io.phobotic.pavillion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.phobotic.pavillion.R;

/**
 * Created by Jonathan Nelson on 8/12/16.
 */

public class DayPickerDay extends RelativeLayout {
    private RelativeLayout mLayout;
    private View mBackground;
    private TextView mText;
    private String day = "M";
    private boolean isSelected = false;

    public DayPickerDay(Context ctx) {
        super(ctx);
        init();
    }

    public DayPickerDay(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            mLayout = (RelativeLayout) inflate(super.getContext(), R.layout.view_day_picker_day, this);
            drawText();
            drawBackground();
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggle();
                }
            });
        }
    }

    private void drawBackground() {
        mBackground = mLayout.findViewById(R.id.background);

        if (isSelected) {
            mBackground.setBackground(getResources().getDrawable(R.drawable.day_picker_background_selected));
        } else {
            mBackground.setBackground(getResources().getDrawable(R.drawable.day_picker_background_unselected));
        }
    }

    public void toggle() {
        if (isSelected()) {
            unselect();
        } else {
            select();
        }
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            this.select();
        } else {
            this.unselect();
        }
    }

    public void unselect() {
        isSelected = false;
        drawBackground();
    }

    public void select() {
        isSelected = true;
        drawBackground();
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
        drawText();
    }

    private void drawText() {
        mText = (TextView) mLayout.findViewById(R.id.text);
        mText.setText(day);
    }
}