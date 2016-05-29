package io.phobotic.pavillion.checkdigit;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;

/**
 * Created by Jonathan Nelson on 5/26/16.
 */
public class MainCheckdigit extends CheckDigit {
    private static final String TAG = MainCheckdigit.class.getSimpleName();
    private String[] checkdigits = {"1", "8", "F", "P", "Y", "7", "E", "N", "X", "6",
            "D", "M", "W", "5", "C", "L", "U", "4", "B", "K", "T", "3", "A",
            "H", "S", "2", "9", "G", "R"};
    private int[] multiples = {25, 21, 17, 13, 9, 5, 1};
    private String checkdigit;

    public MainCheckdigit(@NotNull String location) throws ParseException {
        if (location == null || location.length() != 7) {
            throw new ParseException("Location string must be non-null with length of exactly 7", 0);
        }

        char[] chars = location.toUpperCase().toCharArray();
        int sum = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Integer value = charValues.get(c);
            if (value == null) {
                throw new ParseException("Unable to parse location: '" + location +
                        "' unknown character: '" + c + "'", 0);
            }
            int multiple = multiples[i];
            sum += value * multiple;

        }

        //the checkdigit is calculated as the position equal to the mod of the sum and 29
        int position = sum % 29;
        checkdigit = checkdigits[position];
    }

    @Override
    public String toString() {
        return checkdigit;
    }
}
