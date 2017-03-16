package io.phobotic.pavillion.checkdigit;

import android.support.annotation.NonNull;

import java.text.ParseException;

/**
 * Created by Jonathan Nelson on 5/27/16.
 */
public class LeftCheckdigit extends CheckDigit {
    private static final String TAG = LeftCheckdigit.class.getSimpleName();
    private String[] checkdigits = {"11", "12", "13", "14", "15", "16", "17", "18",
            "19", "20", "21", "22", "23", "24", "25", "26", "27", "30", "31", "32",
            "33", "35", "37", "38", "39", "40", "42", "43", "44"};
    private int[] multiples = { 1, 2, 3, 4, 5, 6, 7, 8}; //TODO multiple at index 7 is not verified
    private String checkdigit;

    public LeftCheckdigit(@NonNull String location) throws java.text.ParseException {
        if (location == null || location.length() < 7 || location.length() > 8) {
            throw new ParseException("Location string must be either 7 or 8 characters in length", 0);
        }

        char[] chars = location.toUpperCase().toCharArray();
        int sum = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Integer value = charValues.get(c);
            if (value == null) {
                throw new java.text.ParseException("Unable to parse location: '" + location +
                        "' unknown character: '" + c + "'", 0);
            }
            int multiple = multiples[i];
            sum += value * multiple;

        }

        //the checkdigit is calculated as the position equal to the mod of the sum and 29
        int position = sum % 29;
        checkdigit = checkdigits[position];
    }

    public String toString() {
        return checkdigit;
    }
}
