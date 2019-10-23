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

package com.egrand.web.qrcode.camera

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.hardware.Camera
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager

//import com.google.zxing.client.android.PreferencesActivity;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
internal class CameraConfigurationManager(private val context: Context) {
    var screenResolution: Point? = null
        private set
    var cameraResolution: Point? = null
        private set

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        var width = display.width
        var height = display.height
        // We're landscape-only, and have apparently seen issues with display
        // thinking it's portrait
        // when waking from sleep. If it's not landscape, assume it's mistaken
        // and reverse them:
        if (width < height) {
            Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect")
            val temp = width
            width = height
            height = temp
        }
        screenResolution = Point(height, width)
        Log.i(TAG, "Screen resolution: " + screenResolution!!)

        cameraResolution = findBestPreviewSizeValue(parameters, Point(width, height), false)
        Log.i(TAG, "Camera resolution: " + cameraResolution!!)
    }

    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.parameters

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.")
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        initializeTorch(parameters, prefs)
        val focusMode = findSettableValue(parameters.supportedFocusModes, Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_MACRO)
        if (focusMode != null) {
            parameters.focusMode = focusMode
        }

        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        /* 竖屏显示 */
        camera.setDisplayOrientation(90)
        camera.parameters = parameters
    }

    fun setTorch(camera: Camera, newSetting: Boolean) {
        val parameters = camera.parameters
        doSetTorch(parameters, newSetting)
        camera.parameters = parameters
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val currentSetting = prefs.getBoolean(KEY_FRONT_LIGHT, false)// PreferencesActivity.
        if (currentSetting != newSetting) {
            val editor = prefs.edit()
            editor.putBoolean(KEY_FRONT_LIGHT, newSetting)// PreferencesActivity.
            editor.commit()
        }
    }

    companion object {

        private val TAG = "CameraConfiguration"
        private val MIN_PREVIEW_PIXELS = 320 * 240 // small screen
        private val MAX_PREVIEW_PIXELS = 800 * 480 // large/HD screen

        val KEY_FRONT_LIGHT = "preferences_front_light"

        private fun initializeTorch(parameters: Camera.Parameters, prefs: SharedPreferences) {
            val currentSetting = prefs.getBoolean(KEY_FRONT_LIGHT, false)// PreferencesActivity.
            doSetTorch(parameters, currentSetting)
        }

        private fun doSetTorch(parameters: Camera.Parameters, newSetting: Boolean) {
            val flashMode: String?
            if (newSetting) {
                flashMode = findSettableValue(parameters.supportedFlashModes, Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON)
            } else {
                flashMode = findSettableValue(parameters.supportedFlashModes, Camera.Parameters.FLASH_MODE_OFF)
            }
            if (flashMode != null) {
                parameters.flashMode = flashMode
            }
        }

        private fun findBestPreviewSizeValue(parameters: Camera.Parameters, screenResolution: Point, portrait: Boolean): Point {
            var bestSize: Point? = null
            var diff = Integer.MAX_VALUE
            for (supportedPreviewSize in parameters.supportedPreviewSizes) {
                val pixels = supportedPreviewSize.height * supportedPreviewSize.width
                if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                    continue
                }
                val supportedWidth = if (portrait) supportedPreviewSize.height else supportedPreviewSize.width
                val supportedHeight = if (portrait) supportedPreviewSize.width else supportedPreviewSize.height
                val newDiff = Math.abs(screenResolution.x * supportedHeight - supportedWidth * screenResolution.y)
                if (newDiff == 0) {
                    bestSize = Point(supportedWidth, supportedHeight)
                    break
                }
                if (newDiff < diff) {
                    bestSize = Point(supportedWidth, supportedHeight)
                    diff = newDiff
                }
            }
            if (bestSize == null) {
                val defaultSize = parameters.previewSize
                bestSize = Point(defaultSize.width, defaultSize.height)
            }
            return bestSize
        }

        private fun findSettableValue(supportedValues: Collection<String>?, vararg desiredValues: String): String? {
            Log.i(TAG, "Supported values: " + supportedValues!!)
            var result: String? = null
            if (supportedValues != null) {
                for (desiredValue in desiredValues) {
                    if (supportedValues.contains(desiredValue)) {
                        result = desiredValue
                        break
                    }
                }
            }
            Log.i(TAG, "Settable value: " + result!!)
            return result
        }
    }

}
