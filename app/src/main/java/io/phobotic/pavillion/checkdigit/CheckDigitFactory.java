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
//            String middle = new MiddleCheckdigit(location).toString();
//            String right = new RightCheckdigit(location).toString();
            String middle = "\uD83E\uDD37";
            String right = "\uD83E\uDD37";
            CheckDigits checkDigits = new CheckDigits(main, left, middle, right);
            return checkDigits;
        } catch (IllegalFormatException e) {
            throw e;
        }
    }


}
