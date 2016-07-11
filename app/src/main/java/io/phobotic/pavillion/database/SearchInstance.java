package io.phobotic.pavillion.database;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Jonathan Nelson on 6/1/16.
 */
public class SearchInstance {
    private long timestamp;
    private String location;
    private String cdMain;
    private String cdLeft;
    private String cdMiddle;
    private String cdRight;
    private File picFile;

    public SearchInstance(long timestamp, @NotNull String location, @Nullable String cdMain,
                          @Nullable String cdLeft, @Nullable String cdMiddle,
                          @Nullable String cdRight, @Nullable File picFile) {
        this.timestamp = timestamp;
        this.location = location;
        this.cdMain = cdMain;
        this.cdLeft = cdLeft;
        this.cdMiddle = cdMiddle;
        this.cdRight = cdRight;
        this.picFile = picFile;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public String getCdMain() {
        return cdMain;
    }

    public String getCdLeft() {
        return cdLeft;
    }

    public String getCdMiddle() {
        return cdMiddle;
    }

    public String getCdRight() {
        return cdRight;
    }

    public File getPicFile() {
        return picFile;
    }

}
