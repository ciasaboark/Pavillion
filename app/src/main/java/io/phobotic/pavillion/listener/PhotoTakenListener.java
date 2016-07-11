package io.phobotic.pavillion.listener;

import java.io.File;

/**
 * Created by Jonathan Nelson on 6/3/16.
 */
public interface PhotoTakenListener {
    void onPhotoTaken(File pictureFile, long searchId);
}
