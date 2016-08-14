package io.phobotic.pavillion.database;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Jonathan Nelson on 6/3/16.
 */
public class SearchRecord extends SearchInstance {
    private int id;

    public SearchRecord(int id, long timestamp, @NotNull String location,
                        @Nullable String cdMain, @Nullable String cdLeft,
                        @Nullable String cdMiddle, @Nullable String cdRight,
                        @Nullable File picFile) {
        super(timestamp, location, cdMain, cdLeft, cdMiddle, cdRight, picFile);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
