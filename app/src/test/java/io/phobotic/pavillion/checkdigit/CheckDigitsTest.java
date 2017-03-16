package io.phobotic.pavillion.checkdigit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jonathan Nelson on 8/25/16.
 */
public class CheckDigitsTest {
    private CheckDigits checkDigits;

    @Before
    public void setUp() throws Exception {
        checkDigits = new CheckDigits("location", "main", "left", "middle", "right");
    }

    @Test
    public void testGetMainCheckdigit() throws Exception {
        assertEquals(checkDigits.getMainCheckdigit(), "main");
    }

    @Test
    public void testGetLeftCheckdigit() throws Exception {
        assertEquals(checkDigits.getLeftCheckdigit(), "left");
    }

    @Test
    public void testGetMiddleCheckdigit() throws Exception {
        assertEquals(checkDigits.getMiddleCheckdigit(), "middle");
    }

    @Test
    public void testGetRightCheckdigit() throws Exception {
        assertEquals(checkDigits.getRightCheckdigit(), "right");
    }

    @Test
    public void testGetLocation() throws Exception {
        assertEquals(checkDigits.getLocation(), "location");
    }
}