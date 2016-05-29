package io.phobotic.pavillion.checkdigit;

import java.util.HashMap;

/**
 * Created by Jonathan Nelson on 5/26/16.
 */
public class CheckDigits {
    private String mainCheckdigit;
    private String leftCheckdigit;
    private String middleCheckdigit;
    private String rightCheckdigit;


    public String getMainCheckdigit() {
        return mainCheckdigit;
    }

    public String getLeftCheckdigit() {
        return leftCheckdigit;
    }

    public String getMiddleCheckdigit() {
        return middleCheckdigit;
    }

    public String getRightCheckdigit() {
        return rightCheckdigit;
    }

    public CheckDigits(String main, String left, String middle, String right) {
        this.mainCheckdigit = main;
        this.leftCheckdigit = left;
        this.middleCheckdigit = middle;
        this.rightCheckdigit = right;
    }
}
