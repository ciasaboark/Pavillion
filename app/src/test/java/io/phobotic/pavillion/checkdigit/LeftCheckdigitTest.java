package io.phobotic.pavillion.checkdigit;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Created by Jonathan Nelson on 8/25/16.
 */
public class LeftCheckdigitTest {
    /**
     * Generating checkdigits with a null string should fail
     */
    @Test(expected =  ParseException.class)
    public void testNullString() throws ParseException {
        LeftCheckdigit cd = new LeftCheckdigit(null);
    }

    /**
     * Generating checkdigits with an empty string should fail
     */
    @Test(expected = ParseException.class)
    public void testEmptyString() throws ParseException {
        LeftCheckdigit cd = new LeftCheckdigit("");
    }

    /**
     * Generating checkdigits with less than 7 characters should fail
     */
    @Test(expected = ParseException.class)
    public void testShortString() throws ParseException {
        LeftCheckdigit cd = new LeftCheckdigit("AAAAAA");
    }

    /**
     * Generating checkdigits over 7 characters should fail
     */
    @Test(expected = ParseException.class)
    public void testLongString() throws ParseException {
        LeftCheckdigit cd = new LeftCheckdigit("AAAAAAAAA");
    }

    /**
     * Expects generated checkdigit to match record in IMLOA
     */
    @Test
    public void testMainCheckdigit() throws ParseException {
        LeftCheckdigit cd = new LeftCheckdigit("GA10321");
        assertEquals("40", cd.toString());
    }

    /**
     * EXE does not not allow lower case locations, so the generated checkdigit
     * for a lowercase string should be the same as the checkdigit for the uppercase string
     */
    @Test
    public void testUpperLowerEqual() throws ParseException {
        LeftCheckdigit cd1 = new LeftCheckdigit("ga10211");
        LeftCheckdigit cd2 = new LeftCheckdigit("GA10211");
        assertEquals(cd1.toString(), cd2.toString());
    }

}