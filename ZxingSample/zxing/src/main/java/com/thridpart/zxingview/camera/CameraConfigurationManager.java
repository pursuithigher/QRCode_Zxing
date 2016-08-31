/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thridpart.zxingview.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.thridpart.zxingview.camera.open.CameraFacing;
import com.thridpart.zxingview.camera.open.OpenCamera;


/**
 * 相机辅助类，主要用于设置相机的各类参数
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
final class CameraConfigurationManager {

//	private static final String TAG = "CameraConfiguration";

    private final Context context;
    private int cwNeededRotation;
    private int cwRotationFromDisplayToCamera;
    private Point screenResolution;
    private Point cameraResolution;
    private Point bestPreviewSize;
    private Point previewSizeOnScreen;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * 计算了屏幕分辨率和当前最适合的相机像素
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(OpenCamera camera) {
        Camera.Parameters parameters = camera.getCamera().getParameters();
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: "
                            + displayRotation);
                }
        }
//		Log.i(TAG, "Display at: " + cwRotationFromNaturalToDisplay);

        int cwRotationFromNaturalToCamera = camera.getOrientation();
//		Log.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
//            Log.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
        }

		/*
         * SharedPreferences prefs =
		 * PreferenceManager.getDefaultSharedPreferences(context); String
		 * overrideRotationString; if (camera.getFacing() == CameraFacing.FRONT)
		 * { overrideRotationString =
		 * prefs.getString(PreferencesActivity.KEY_FORCE_CAMERA_ORIENTATION_FRONT
		 * , null); } else { overrideRotationString =
		 * prefs.getString(PreferencesActivity.KEY_FORCE_CAMERA_ORIENTATION,
		 * null); } if (overrideRotationString != null &&
		 * !"-".equals(overrideRotationString)) { Log.i(TAG,
		 * "Overriding camera manually to " + overrideRotationString);
		 * cwRotationFromNaturalToCamera =
		 * Integer.parseInt(overrideRotationString); }
		 */

        cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
//        Log.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);
        if (camera.getFacing() == CameraFacing.FRONT) {
//            Log.i(TAG, "Compensating rotation for front camera");
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }
//        Log.i(TAG, "Clockwise rotation from display to camera: " + cwNeededRotation);

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
//        Log.i(TAG, "Screen resolution in current orientation: " + screenResolution);
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(
                parameters, screenResolution);
//        Log.i(TAG, "Camera resolution: " + cameraResolution);
        bestPreviewSize = CameraConfigurationUtils.findBestPreviewSizeValue(
                parameters, screenResolution);
//        Log.i(TAG, "Best available preview size: " + bestPreviewSize);

        boolean isScreenPortrait = screenResolution.x < screenResolution.y;
        boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;

        if (isScreenPortrait == isPreviewSizePortrait) {
            previewSizeOnScreen = bestPreviewSize;
        } else {
            previewSizeOnScreen = new Point(bestPreviewSize.y,
                    bestPreviewSize.x);
        }
//        Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
    }

    /**
     * 读取配置设置相机的对焦模式、闪光灯模式等等
     *
     * @param camera
     * @param safeMode
     */
    void setDesiredCameraParameters(OpenCamera camera, boolean safeMode) {

        Camera theCamera = camera.getCamera();
        Camera.Parameters parameters = theCamera.getParameters();

        if (parameters == null) {
//            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

//        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

//        if (safeMode) {
//            Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
//        }

        // 默认关闪光灯
        initializeTorch(parameters, FrontLightMode.OFF, safeMode);
        // 自动对焦
        boolean autoFocus = true;
        // 持续对焦
        boolean disableContinuousFocus = true;
        CameraConfigurationUtils.setFocus(parameters, autoFocus, disableContinuousFocus, safeMode);

        if (!safeMode) {
            // 反色，扫描黑色背景上的白色条码。仅适用于部分设备。
            boolean invertScan = false;
            if (invertScan) {
                CameraConfigurationUtils.setInvertColor(parameters);
            }
            // 不进行条形码场景匹配
            boolean barCodeSceneMode = true;
            if (!barCodeSceneMode) {
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }

            // 不使用距离测量
            boolean disableMetering = true;
            if (!disableMetering) {
                CameraConfigurationUtils.setVideoStabilization(parameters);
                CameraConfigurationUtils.setFocusArea(parameters);
                CameraConfigurationUtils.setMetering(parameters);
            }

        }

        parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y);

        theCamera.setParameters(parameters);

        theCamera.setDisplayOrientation(cwRotationFromDisplayToCamera);

        Camera.Parameters afterParameters = theCamera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null
                && (bestPreviewSize.x != afterSize.width || bestPreviewSize.y != afterSize.height)) {
//            Log.w(TAG, "Camera said it supported preview size " + bestPreviewSize.x + 'x' + bestPreviewSize.y
//                    + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            bestPreviewSize.x = afterSize.width;
            bestPreviewSize.y = afterSize.height;
        }
    }

    Point getBestPreviewSize() {
        return bestPreviewSize;
    }

    Point getPreviewSizeOnScreen() {
        return previewSizeOnScreen;
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    int getCWNeededRotation() {
        return cwNeededRotation;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = camera.getParameters().getFlashMode();
                return flashMode != null
                        && (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || Camera.Parameters.FLASH_MODE_TORCH
                        .equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting, false);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters,
                                 FrontLightMode frontLightMode, boolean safeMode) {
        boolean currentSetting = frontLightMode == FrontLightMode.ON;
        doSetTorch(parameters, currentSetting, safeMode);
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting,
                            boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, newSetting);
        // 不曝光
        boolean disableExposure = true;
        if (!safeMode && !disableExposure) {
            CameraConfigurationUtils.setBestExposure(parameters, newSetting);
        }
    }

}
