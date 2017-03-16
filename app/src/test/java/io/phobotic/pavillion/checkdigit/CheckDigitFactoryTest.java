package io.phobotic.pavillion.checkdigit;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jonathan Nelson on 8/25/16.
 */
public class CheckDigitFactoryTest {
    /**
     * Generating checkdigits with a null string should fail
     */
    @Test(expected =  ParseException.class)
    public void testNullString() throws ParseException {
        CheckDigits cd = CheckDigitFactory.fromString(null);
    }

    /**
     * Generating checkdigits with an empty string should fail
     */
    @Test(expected = ParseException.class)
    public void testEmptyString() throws ParseException {
        CheckDigits cd = CheckDigitFactory.fromString("");
    }

    /**
     * Generating checkdigits with less than 7 characters should fail
     */
    @Test(expected = ParseException.class)
    public void testShortString() throws ParseException {
        CheckDigits cd = CheckDigitFactory.fromString("AAAAAA");
    }

    /**
     * Generating checkdigits over 7 characters should fail
     */
    @Test(expected = ParseException.class)
    public void testLongString() throws ParseException {
        CheckDigits cd = CheckDigitFactory.fromString("AAAAAAAAA");
    }

    /**
     * Expects generated checkdigit to match record in IMLOA
     */
    @Test
    public void testMainCheckdigit() throws ParseException {
        CheckDigits cd = CheckDigitFactory.fromString("GA10321");
        assertEquals("40", cd.toString());
    }

    /**
     * EXE does not not allow lower case locations, so the generated checkdigit
     * for a lowercase string should be the same as the checkdigit for the uppercase string
     */
    @Test
    public void testUpperLowerEqual() throws ParseException {
        CheckDigits cd1 = CheckDigitFactory.fromString("ga10211");
        CheckDigits cd2 = CheckDigitFactory.fromString("GA10211");
        assertEquals(cd1, cd2);
    }
}