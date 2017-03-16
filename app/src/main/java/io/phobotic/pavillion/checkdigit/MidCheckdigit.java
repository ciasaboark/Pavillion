package io.phobotic.pavillion.checkdigit;

import android.support.annotation.NonNull;

import java.text.ParseException;

/**
 * Created by Jonathan Nelson on 3/14/17.
 */

public class MidCheckdigit extends CheckDigit {
    private static final String TAG = LeftCheckdigit.class.getSimpleName();
    private String[] checkdigits = {"69", "70", "71", "72", "73", "74", "75", "76",
            "77", "42", "43", "44", "45", "46", "47", "48", "49", "52", "53", "54", "55", "57",
            "59", "60", "61", "62", "64", "65", "66"};
    private int[] multiples = { 1, 2, 3, 4, 5, 6, 7, 8};    //TODO multiple at index 7 not verified
    private String checkdigit;

    public MidCheckdigit(@NonNull String location) throws java.text.ParseException {
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
