/**
 * Copyright (c) 2012-2017, Smart Engines Ltd
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the Smart Engines Ltd nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package kg.onoi.smart_sdk.recognition;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Semaphore;

import biz.smartengines.smartid.swig.ImageOrientation;
import biz.smartengines.smartid.swig.RecognitionEngine;
import biz.smartengines.smartid.swig.RecognitionResult;
import biz.smartengines.smartid.swig.RecognitionSession;
import biz.smartengines.smartid.swig.SessionSettings;

import static android.content.Context.WINDOW_SERVICE;
import static java.lang.Math.abs;


/**
 * Main recognition activity view
 */
public class SmartIDView implements SurfaceHolder.Callback,
        Camera.PreviewCallback,
        Camera.AutoFocusCallback {

    private Context context;
    private Callback callback;

    private boolean camera_opened = false;
    private Camera camera = null;
    private boolean autofocus = false;
    private static int angle = -1;

    private SurfaceView preview;
    private RelativeLayout drawing;
    private SmartIDDraw draw = null;

    private static RecognitionEngine engine;
    private static SessionSettings sessionSettings;
    private static boolean engine_configured = false;
    private static RecognitionSession session;

    private boolean processing = false;

    private Semaphore frame_waiting;  // semaphore waiting for the frame
    private Semaphore frame_ready;  // semaphore the frame is ready

    private static volatile byte[] data = null;  // current frame buffer

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void initializeEngine(Context context_, Callback callback_) {
        callback = callback_;
        context = context_;

        new initEngineTask().execute();
    }

    public void setSurface(SurfaceView preview_, RelativeLayout drawing_) {
        preview = preview_;
        preview.setOnClickListener(onFocus);

        draw = new SmartIDDraw(context);
        drawing = drawing_;
        drawing.addView(draw);

        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(this);
    }

    public void startRecognition(String document_mask, String time_out) {
        if (engine_configured == true && camera_opened == true) {
            try {
                // enable document list by mask
                String[] masks = document_mask.split(";");
                for (int i = 0; i < masks.length; i++) {
                    sessionSettings.AddEnabledDocumentTypes(masks[i]);
                }
                // set session timeout
                sessionSettings.SetOption("common.sessionTimeout", time_out);
                sessionSettings.SetOption("common.extractTemplateImages", "true");
                session = engine.SpawnSession(sessionSettings);

                frame_waiting = new Semaphore(1, true);  // create semaphores
                frame_ready = new Semaphore(0, true);
                camera.setPreviewCallback(this);  // set preview callback onPreviewFrame(...)
                processing = true;

                new workEngineTask().execute();  // start recognition thread
            } catch (RuntimeException e) {
                String message = "Error while spawning session: " + e.toString();
                Log.d("smartid", message);
                callback.error(message);
            }
        }
    }

    public void stopRecognition() {
        if (processing == true) {
            processing = false;

            data = null;

            frame_waiting.release();  // release semaphores
            frame_ready.release();

            draw.showResult(null);
            draw.invalidate();

            camera.setPreviewCallback(null);  // stop preview
        }
    }

    private View.OnClickListener onFocus = new View.OnClickListener() {

        public void onClick(View v) {
            if (camera_opened == true) { // focus on tap
                try {
                    Camera.Parameters cparams = camera.getParameters();

                    // focus if at least one focus area exists
                    if (autofocus && cparams.getMaxNumFocusAreas() > 0) {
                        camera.cancelAutoFocus();
                        cparams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        camera.setParameters(cparams);
                        camera.autoFocus(SmartIDView.this);
                    }
                } catch (RuntimeException e) {
                    // empty body
                }
            }
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        updatePreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // empty body
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRecognition();
        closeCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data_, Camera camera) {
        // if frame is waiting status - get current frame
        if (frame_waiting.tryAcquire() && processing == true) {
            data = data_;
            frame_ready.release();  // frame is ready
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // empty body
    }

    public void updatePreview() {
        angle = getAngle();
        try {
            setView(preview.getWidth(), preview.getHeight());
        } catch (Exception e) {
            // empty body
        }
    }

    private int getAngle() {
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        int angle_ = (info.orientation - display.getRotation() * 90 + 360) % 360;
        return angle_;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setView(int width, int height) throws Exception {
        if (camera_opened == false) {
            camera = Camera.open();

            if (camera == null) {
                return;
            }

            camera_opened = true;

            Camera.Parameters params = camera.getParameters();

            List<String> focus_modes = params.getSupportedFocusModes();  // supported focus modes
            String focus_mode = Camera.Parameters.FOCUS_MODE_AUTO;
            autofocus = focus_modes.contains(focus_mode);

            if (autofocus == true) {  // camera has autofocus
                if (focus_modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    focus_mode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                } else if (focus_modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    focus_mode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                }
            } else {
                // camera doesn't support autofocus so select the first mode
                focus_mode = focus_modes.get(0);
            }

            params.setFocusMode(focus_mode);
            camera.setParameters(params);
        }

        setPreviewSize(width, height);
        camera.setDisplayOrientation(angle);
        camera.getParameters().setJpegThumbnailSize(width, height);
        camera.getParameters().setJpegQuality(100);
        camera.setPreviewDisplay(preview.getHolder());
        camera.startPreview();
        camera_opened = true;
    }

    void setPreviewSize(int width, int height) {
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        // minimal width of preview (if less - quality of recognition will be low)
        final int minimum_width = 800;
        final float tolerance = 0.1f;

        Camera.Size preview_size = sizes.get(0);

        final boolean landscape = false;

        if (landscape) {
            int tmp = height;
            height = width;
            width = tmp;
        }

        float best_ratio = (float) height / (float) width;
        // difference ratio between preview and best
        float preview_ratio_diff = abs((float) preview_size.width / (
                float) preview_size.height - best_ratio);

        for (int i = 1; i < sizes.size(); i++) {
            Camera.Size tmp_size = sizes.get(i);

            if (tmp_size.width < minimum_width) {
                continue;
            }

            float tmp_ratio_diff = abs((float) tmp_size.width / (float) tmp_size.height - best_ratio);

            if (abs(tmp_ratio_diff - preview_ratio_diff) < tolerance &&
                    tmp_size.width > preview_size.width || tmp_ratio_diff < preview_ratio_diff) {
                preview_size = tmp_size;
                preview_ratio_diff = tmp_ratio_diff;
            }
        }

        params.setPreviewSize(preview_size.width, preview_size.height);
        camera.setParameters(params);

        // recalculate surface size according to preview ratio

        int height_new = 0, width_new = 0;

        height_new = width * preview_size.width / preview_size.height;
        width_new = height * preview_size.height / preview_size.width;

        // select new surface size no more than original size

        if (height_new > height) {
            height = height_new;
        }

        if (width_new > width) {
            width = width_new;
        }

        int preview_width = preview_size.width;
        int preview_height = preview_size.height;

        if (landscape) {
            int tmp = height;
            height = width;
            width = tmp;

            tmp = preview_height;
            preview_height = preview_width;
            preview_width = tmp;
        }

        ViewGroup.LayoutParams layout = preview.getLayoutParams();
        layout.width = width;
        layout.height = height;
        preview.setLayoutParams(layout);

        drawing.setLayoutParams(layout);
        draw.setRecognitionZone(layout.width, layout.height, preview_height, preview_width);
    }

    void closeCamera() {
        if (camera_opened == true) {
            camera.stopPreview();
            camera.release();
            camera = null;
            camera_opened = false;
        }
    }


    public void takePicture(final ResultRecognitionWrapper result) throws IOException {
        try {
            final File createImageFile = createImageFile(result.getDocType());
            FileOutputStream outStream = new FileOutputStream(createImageFile);
            result.getDocImage().compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            callback.photoCreated(createImageFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            callback.photoCreated(null);
        }
    }

    private File createImageFile(String fileName) throws IOException {
        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName.replace(".", "_") + "_", ".jpg", filesDir);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class initEngineTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            if (engine_configured == true) {
                return null;
            }

            try {
                byte[] bundle_data = prepareBundle("data");
                configureEngine(bundle_data);
            } catch (Exception e) {
                String message = "Error while configuring engine: " + e.toString();
                Log.d("smartid", message);
                callback.error(message);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            callback.initialized(engine_configured);
        }

    }

    byte[] prepareBundle(String bundle_dir) throws Exception {
        AssetManager assetManager = context.getAssets();

        String bundle_name = "";
        String[] file_list = assetManager.list(bundle_dir);

        if (file_list.length <= 0) {
            throw new Exception("Assets directory empty: configuration bundle needed!");
        } else {
            for (String file : file_list) {
                if (file.endsWith(".zip")) {
                    bundle_name = file;
                    break;
                }
            }

            if (!bundle_name.endsWith(".zip")) {
                throw new Exception("No configuration bundle found!");
            }
        }

        final String input_bundle_path = bundle_dir + File.separator + bundle_name;

        InputStream is = assetManager.open(input_bundle_path);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        is.close();
        return os.toByteArray();
    }

    private void configureEngine(final byte[] bundle_data) throws Exception {
        System.loadLibrary("jniSmartIdEngine");  // load library
        engine = new RecognitionEngine(bundle_data);  // create engine
        sessionSettings = engine.CreateSessionSettings();  // create setting for engine
        engine_configured = true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    class workEngineTask extends AsyncTask<Void, RecognitionResult, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            while (true) {
                try {
                    frame_ready.acquire();  // waiting for the frame

                    if (processing == false) {
                        break;
                    }

                    Camera.Size size = camera.getParameters().getPreviewSize();
                    RecognitionResult result;

                    switch (angle) {
                        case 0:
                            result = session.ProcessYUVSnapshot(data, size.width, size.height, ImageOrientation.Landscape);
                            break;
                        case 180:
                            result = session.ProcessYUVSnapshot(data, size.width, size.height, ImageOrientation.InvertedLandscape);
                            break;
                        case 270:
                            result = session.ProcessYUVSnapshot(data, size.width, size.height, ImageOrientation.InvertedPortrait);
                            break;
                        default:
                            result = session.ProcessYUVSnapshot(data, size.width, size.height, ImageOrientation.Portrait);
                    }
                    publishProgress(result);  // show current result
                } catch (Exception e) {
                    String message = "Error while processing frame: " + e.toString();
                    Log.d("smartid", message);
                    callback.error(message);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(RecognitionResult... res) {
            RecognitionResult result = res[0];
            draw.showResult(result);
            draw.invalidate();

            callback.recognized(result);
            frame_waiting.release();
        }
    }


    public interface Callback {
        void initialized(boolean engine_initialized);

        void recognized(RecognitionResult result);

        void error(String message);

        void photoCreated(String path);
    }
}
