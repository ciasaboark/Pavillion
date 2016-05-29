package io.phobotic.pavillion.checkdigit;

import java.util.HashMap;

/**
 * Created by Jonathan Nelson on 5/27/16.
 */
public abstract class CheckDigit {
    protected static HashMap<Character, Integer> charValues;
    static {
        charValues = new HashMap<>();
        charValues.put('A', 1);
        charValues.put('B', 2);
        charValues.put('C', 3);
        charValues.put('D', 4);
        charValues.put('E', 5);
        charValues.put('F', 6);
        charValues.put('G', 7);
        charValues.put('H', 8);
        charValues.put('I', 9);
        charValues.put('J', 10);
        charValues.put('K', 11);
        charValues.put('L', 12);
        charValues.put('M', 13);
        charValues.put('N', 14);
        charValues.put('O', 15);
        charValues.put('P', 16);
        charValues.put('Q', 17);
        charValues.put('R', 18);
        charValues.put('S', 19);
        charValues.put('T', 20);
        charValues.put('U', 21);
        charValues.put('V', 22);
        charValues.put('W', 23);
        charValues.put('X', 24);
        charValues.put('Y', 25);
        charValues.put('Z', 26);
        charValues.put('0', 27);
        charValues.put('1', 28);
        charValues.put('2', 29);
        charValues.put('3', 30);
        charValues.put('4', 31);
        charValues.put('5', 32);
        charValues.put('6', 33);
        charValues.put('7', 34);
        charValues.put('8', 35);
        charValues.put('9', 36);
    }
}
