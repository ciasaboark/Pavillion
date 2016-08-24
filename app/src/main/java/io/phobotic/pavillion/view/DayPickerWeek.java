package io.phobotic.pavillion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import io.phobotic.pavillion.R;

/**
 * Created by Jonathan Nelson on 8/12/16.
 */

public class DayPickerWeek extends LinearLayout {
    private View mLayout;
    private DayPickerDay sun;
    private DayPickerDay mon;
    private DayPickerDay tue;
    private DayPickerDay wed;
    private DayPickerDay thu;
    private DayPickerDay fri;
    private DayPickerDay sat;

    public DayPickerWeek(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mLayout = inflate(super.getContext(), R.layout.view_day_picker_week, this);
        if (!isInEditMode()) {
            sun = (DayPickerDay) mLayout.findViewById(R.id.sun);
            mon = (DayPickerDay) mLayout.findViewById(R.id.mon);
            tue = (DayPickerDay) mLayout.findViewById(R.id.tue);
            wed = (DayPickerDay) mLayout.findViewById(R.id.wed);
            thu = (DayPickerDay) mLayout.findViewById(R.id.thu);
            fri = (DayPickerDay) mLayout.findViewById(R.id.fri);
            sat = (DayPickerDay) mLayout.findViewById(R.id.sat);


            sun.setDay("S");
            mon.setDay("M");
            tue.setDay("T");
            wed.setDay("W");
            thu.setDay("H");
            fri.setDay("F");
            sat.setDay("S");
        }
    }

    public String toPersistentString() {
        String string = "";
        String prefix = "";
        final String set = "T";
        final String unset = "F";
        for (int i = 0; i < 7; i++) {
            switch (i) {
                case 0:
                    string += prefix + (sun.isSelected() ? set : unset);
                    break;
                case 1:
                    string += prefix + (mon.isSelected() ? set : unset);
                    break;
                case 2:
                    string += prefix + (tue.isSelected() ? set : unset);
                    break;
                case 3:
                    string += prefix + (wed.isSelected() ? set : unset);
                    break;
                case 4:
                    string += prefix + (thu.isSelected() ? set : unset);
                    break;
                case 5:
                    string += prefix + (fri.isSelected() ? set : unset);
                    break;
                case 6:
                    string += prefix + (sat.isSelected() ? set : unset);
                    break;
            }
            prefix = ":";
        }

        return string;
    }

    public void fromPersistentString(String string) {
        String[] parts = string.split(":");
        for (int i = 0; i < 7; i++) {
            String part = parts[i];
            switch (i) {
                case 0:
                    sun.setSelected("T".equals(part));
                    break;
                case 1:
                    mon.setSelected("T".equals(part));
                    break;
                case 2:
                    tue.setSelected("T".equals(part));
                    break;
                case 3:
                    wed.setSelected("T".equals(part));
                    break;
                case 4:
                    thu.setSelected("T".equals(part));
                    break;
                case 5:
                    fri.setSelected("T".equals(part));
                    break;
                case 6:
                    sat.setSelected("T".equals(part));
                    break;
            }
        }
    }
}
