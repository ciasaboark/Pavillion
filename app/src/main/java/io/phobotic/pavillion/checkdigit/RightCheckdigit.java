package io.phobotic.pavillion.checkdigit;

import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;

/**
 * Created by Jonathan Nelson on 3/14/17.
 */

public class RightCheckdigit extends CheckDigit {
    private static final String TAG = LeftCheckdigit.class.getSimpleName();
    private String[] checkdigits = {"58", "59", "60", "61", "62", "63", "64", "65", "66",
            "74", "75", "76", "77" ,"78", "79", "80", "81", "84", "85", "86", "87", "89",
            "91", "92", "93", "94", "96", "97", "98"};
    private int[] multiples = { 1, 2, 3, 4, 5, 6, 7, 8};    //TODO multiple at index 7 not verified
    private String checkdigit;

    public RightCheckdigit(@NonNull String location) throws java.text.ParseException {
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

        int position = sum % 29;
        checkdigit = checkdigits[position];
    }

    public String toString() {
        return checkdigit;
    }
}
