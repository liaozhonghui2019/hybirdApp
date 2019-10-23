/*
 * Copyright (C) 2008 ZXing authors
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

package com.egrand.web.qrcode.camera

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceHolder
import java.io.IOException

//import com.google.zxing.client.android.PreferencesActivity;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class CameraManager(private val context: Context) {
    private val configManager: CameraConfigurationManager
    private var camera: Camera? = null
    internal var framingRect: Rect? = null
    private var framingRectInPreview: Rect? = null
    private var initialized: Boolean = false
    private var previewing: Boolean = false
    private var reverseImage: Boolean = false
    private var requestedFramingRectWidth: Int = 0
    private var requestedFramingRectHeight: Int = 0
    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */
    private val previewCallback: PreviewCallback
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which
     * requested them.
     */
    private val autoFocusCallback: AutoFocusCallback

    init {
        this.configManager = CameraConfigurationManager(context)
        previewCallback = PreviewCallback(configManager)
        autoFocusCallback = AutoFocusCallback()
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames
     * into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder) {
        var theCamera = camera
        if (theCamera == null) {
            theCamera = Camera.open()
            if (theCamera == null) {
                throw IOException()
            }
            camera = theCamera
        }
        theCamera.setPreviewDisplay(holder)

        if (!initialized) {
            initialized = true
            configManager.initFromCameraParameters(theCamera)
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight)
                requestedFramingRectWidth = 0
                requestedFramingRectHeight = 0
            }
        }
        configManager.setDesiredCameraParameters(theCamera)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        reverseImage = prefs.getBoolean(KEY_REVERSE_IMAGE, false)
    }

    /**
     * Closes the camera driver if still in use.
     */
    fun closeDriver() {
        if (camera != null) {
            camera!!.release()
            camera = null
            // Make sure to clear these each time we close the camera, so that
            // any scanning rect
            // requested by intent is forgotten.
            framingRect = null
            framingRectInPreview = null
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     * 请求相机硬件在屏幕上开始绘制画面
     */
    fun startPreview() {
        val theCamera = camera
        if (theCamera != null && !previewing) {
            theCamera.startPreview()
            previewing = true
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     * 通知相机停止绘制预览画面
     */
    fun stopPreview() {
        if (camera != null && previewing) {
            camera!!.stopPreview()
            previewCallback.setHandler(null, 0)
            autoFocusCallback.setHandler(null, 0)
            previewing = false
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    fun requestPreviewFrame(handler: Handler, message: Int) {
        val theCamera = camera
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message)
            theCamera.setOneShotPreviewCallback(previewCallback)
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    fun requestAutoFocus(handler: Handler, message: Int) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message)
            try {
                camera!!.autoFocus(autoFocusCallback)
            } catch (re: RuntimeException) {
                // Have heard RuntimeException reported in Android 4.0.x+;
                // continue?
                Log.w(TAG, "Unexpected exception while focusing", re)
            }

        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user
     * where to place the barcode. This target helps with alignment as well as
     * forces the user to hold the device far enough away to ensure the image
     * will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    fun getFramingRect(): Rect? {
        if (framingRect == null) {
            if (camera == null) {
                return null
            }
            val screenResolution = configManager.screenResolution
            var width = screenResolution!!.x * 3 / 4
            if (width < MIN_FRAME_WIDTH) {
                width = MIN_FRAME_WIDTH
            } else if (width > MAX_FRAME_WIDTH) {
                width = MAX_FRAME_WIDTH
            }
            var height = screenResolution.y * 3 / 4
            if (height < MIN_FRAME_HEIGHT) {
                height = MIN_FRAME_HEIGHT
            } else if (height > MAX_FRAME_HEIGHT) {
                height = MAX_FRAME_HEIGHT
            }
            val leftOffset = (screenResolution.x - width) / 2
            val topOffset = (screenResolution.y - height) * 2 / 5
            framingRect = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
            Log.d(TAG, "Calculated framing rect: " + framingRect!!)
        }
        return framingRect
    }

    /**
     * Like [.getFramingRect] but coordinates are in terms of the preview
     * frame, not UI / screen.
     */
    fun getFramingRectInPreview(): Rect? {
        if (framingRectInPreview == null) {
            val framingRect = getFramingRect() ?: return null
            val rect = Rect(framingRect)
            val cameraResolution = configManager.cameraResolution
            val screenResolution = configManager.screenResolution
            /*
             * 横屏 rect.left = rect.left * cameraResolution.x /
             * screenResolution.x; rect.right = rect.right * cameraResolution.x
             * / screenResolution.x; rect.top = rect.top * cameraResolution.y /
             * screenResolution.y; rect.bottom = rect.bottom *
             * cameraResolution.y / screenResolution.y;
             */

            /* 竖屏 */
            rect.left = rect.left * cameraResolution!!.y / screenResolution!!.x
            rect.right = rect.right * cameraResolution.y / screenResolution.x
            rect.top = rect.top * cameraResolution.x / screenResolution.y
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y
            framingRectInPreview = rect
        }
        return framingRectInPreview
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions,
     * rather than determine them automatically based on screen resolution.
     *
     * @param width  The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    fun setManualFramingRect(width: Int, height: Int) {
        var width = width
        var height = height
        if (initialized) {
            val screenResolution = configManager.screenResolution
            if (width > screenResolution!!.x) {
                width = screenResolution.x
            }
            if (height > screenResolution.y) {
                height = screenResolution.y
            }
            val leftOffset = (screenResolution.x - width) / 2
            val topOffset = (screenResolution.y - height) / 2
            framingRect = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
            Log.d(TAG, "Calculated manual framing rect: " + framingRect!!)
            framingRectInPreview = null
        } else {
            requestedFramingRectWidth = width
            requestedFramingRectHeight = height
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource? {
        val rect = getFramingRectInPreview() ?: return null
// Go ahead and assume it's YUV rather than die.
        return PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), reverseImage)
    }

    fun openFlashLight() {
        if (camera != null) {
            val parameter = camera!!.parameters
            parameter.flashMode = Parameters.FLASH_MODE_TORCH
            camera!!.parameters = parameter
        }
    }

    fun offFlashLight() {
        if (camera != null) {
            val parameter = camera!!.parameters
            parameter.flashMode = Parameters.FLASH_MODE_OFF
            camera!!.parameters = parameter
        }
    }

    fun switchFlashLight() {
        if (camera != null) {
            val parameter = camera!!.parameters
            if (parameter.flashMode == Parameters.FLASH_MODE_TORCH) {
                parameter.flashMode = Parameters.FLASH_MODE_OFF
            } else {
                parameter.flashMode = Parameters.FLASH_MODE_TORCH
            }

            camera!!.parameters = parameter
        }
    }

    companion object {

        private val TAG = CameraManager::class.java.simpleName

        private val MIN_FRAME_WIDTH = 240
        private val MIN_FRAME_HEIGHT = 240
        private val MAX_FRAME_WIDTH = 400
        private val MAX_FRAME_HEIGHT = 400

        val KEY_REVERSE_IMAGE = "preferences_reverse_image"
    }
}
