package io.phobotic.pavillion.checkdigit;

import java.io.Serializable;

/**
 * Created by Jonathan Nelson on 5/26/16.
 */
public class CheckDigits implements Serializable {
    private String location;
    private String mainCheckdigit;
    private String leftCheckdigit;
    private String middleCheckdigit;
    private String rightCheckdigit;


    public CheckDigits(String location, String main, String left, String middle, String right) {
        this.location = location;
        this.mainCheckdigit = main;
        this.leftCheckdigit = left;
        this.middleCheckdigit = middle;
        this.rightCheckdigit = right;
    }

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

    public String getLocation() {
        return location;
    }
}
