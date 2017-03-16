package io.phobotic.pavillion.checkdigit;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.IllegalFormatException;

/**
 * Created by Jonathan Nelson on 5/26/16.
 */
public class CheckDigitFactory {
    public static CheckDigits fromString(@NotNull String location) throws ParseException {
        try {
            String main = new MainCheckdigit(location).toString();
            String left = new LeftCheckdigit(location).toString();
            String middle = new MidCheckdigit(location).toString();
            String right = new RightCheckdigit(location).toString();
            CheckDigits checkDigits = new CheckDigits(location, main, left, middle, right);
            return checkDigits;
        } catch (IllegalFormatException e) {
            throw e;
        }
    }


}
