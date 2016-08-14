package io.phobotic.pavillion.email;

import java.io.File;

/**
 * Created by Jonathan Nelson on 8/12/16.
 */
public class Attachment {
    private File file;
    private String name;

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Attachment(File file, String name) {
        this.file = file;
        this.name = name;
    }

}
