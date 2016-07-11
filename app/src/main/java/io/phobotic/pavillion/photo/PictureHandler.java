package io.phobotic.pavillion.photo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.pavillion.listener.PhotoTakenListener;

/**
 * Created by Jonathan Nelson on 5/29/16.
 */
public class PictureHandler {
    private static final String TAG = PictureHandler.class.getSimpleName();
    private CameraDevice mCamera;
    private PhotoTakenListener photoTakenListener;

    public void setPhotoTakenListener(PhotoTakenListener listener) {
        this.photoTakenListener = listener;
    }

    @TargetApi(21)
    public void takePicture(final Context context, final long captureId) throws Exception {
        final CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] ids = manager.getCameraIdList();
        for (String id : ids) {
            final CameraCharacteristics ch = manager.getCameraCharacteristics(id);
            if (ch.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                manager.openCamera(id, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(final CameraDevice camera) {
                        mCamera = camera;
                        StreamConfigurationMap configs = ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        Size[] sizes = configs.getOutputSizes(ImageFormat.JPEG);
                        final ImageReader mImageReader = ImageReader.newInstance(sizes[0].getWidth(), sizes[0].getHeight(), ImageFormat.JPEG, 2);
                        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader reader) {
                                Image image = reader.acquireLatestImage();
                                File file = createFile(context);
                                try {
                                    ByteBuffer bb = image.getPlanes()[0].getBuffer();
                                    byte[] bytes = new byte[bb.remaining()];
                                    bb.get(bytes);
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(bytes);
                                    fos.close();
                                    Log.d(TAG, "wrote image file to: " + file.toString());
                                    if (photoTakenListener != null) {
                                        photoTakenListener.onPhotoTaken(file, captureId);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "error writing camera image capture to file: " + e.getMessage());
                                }
                            }
                        }, null);
                        final Surface jpegCaptureSurface = mImageReader.getSurface();

                        List<Surface> surfaces = new ArrayList<Surface>();
                        surfaces.add(jpegCaptureSurface);
                        try {
                            mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(CameraCaptureSession session) {
                                    try {
                                        CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                        captureRequestBuilder.addTarget(jpegCaptureSurface);
                                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                                        CaptureRequest captureRequest = captureRequestBuilder.build();
                                        session.capture(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                                camera.close();
                                            }

                                            @Override
                                            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                                                super.onCaptureFailed(session, request, failure);
                                                camera.close();
                                            }
                                        }, null);
                                    } catch (CameraAccessException e) {
                                        //todo
                                    }
                                }

                                @Override
                                public void onConfigureFailed(CameraCaptureSession session) {

                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            //todo
                        }
                    }

                    @Override
                    public void onClosed(CameraDevice camera) {
                        super.onClosed(camera);
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {

                    }

                    @Override
                    public void onError(CameraDevice camera, int error) {

                    }
                }, null);
            }
        }
    }

    private File createFile(Context context) {
        File baseDir = context.getFilesDir();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        Date date = new Date();
        String fileName = df.format(date) + ".jpeg";
        File outputFile = new File(baseDir, "/photos/" + fileName);

        //create the parent folder if needed
        File parent = outputFile.getParentFile();
        parent.mkdirs();
        return outputFile;
    }

}
