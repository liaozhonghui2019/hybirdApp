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

package com.egrand.web.qrcode.decoding

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.util.Log
import com.egrand.web.qrcode.CaptureActivity
import com.egrand.web.qrcode.MessageIDs
import com.egrand.web.qrcode.view.ViewfinderResultPointCallback
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import java.util.*


/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class CaptureActivityHandler(private val activity: CaptureActivity, decodeFormats: Vector<BarcodeFormat>?, characterSet: String?) : Handler() {
    private val decodeThread: DecodeThread
    private var state: State? = null

    private enum class State {
        PREVIEW, SUCCESS, DONE
    }

    init {
        decodeThread = DecodeThread(activity, decodeFormats, characterSet, ViewfinderResultPointCallback(activity.viewfinderView!!))
        decodeThread.start()
        state = State.SUCCESS

        // Start ourselves capturing previews and decoding.
        //CameraManager.get().startPreview();
        activity.cameraManager!!.startPreview()
        restartPreviewAndDecode()
    }

    override fun handleMessage(message: Message) {
        when (message.what) {
            MessageIDs.auto_focus ->
                //Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (state == State.PREVIEW) {
                    // CameraManager.get().requestAutoFocus(this, MessageIDs.auto_focus);
                    activity.cameraManager!!.requestAutoFocus(this, MessageIDs.auto_focus)
                }
            MessageIDs.restart_preview -> {
                Log.d(TAG, "Got restart preview message")
                restartPreviewAndDecode()
            }
            MessageIDs.decode_succeeded -> {
                Log.d(TAG, "Got decode succeeded message")
                state = State.SUCCESS
                val bundle = message.data
                val barcode = bundle?.getParcelable<Parcelable>(DecodeThread.BARCODE_BITMAP)
                activity.handleDecode(message.obj as Result, barcode as Bitmap?)
            }
            MessageIDs.decode_failed -> {
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW
                //CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageIDs.decode);
                activity.cameraManager!!.requestPreviewFrame(decodeThread.getHandler()!!, MessageIDs.decode)
            }
            MessageIDs.return_scan_result -> {
                Log.d(TAG, "Got return scan result message")
                activity.setResult(Activity.RESULT_OK, message.obj as Intent)
                activity.finish()
            }
            MessageIDs.launch_product_query -> {
                Log.d(TAG, "Got product query message")
                val url = message.obj as String
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                activity.startActivity(intent)
            }
        }
    }

    fun quitSynchronously() {
        state = State.DONE
        //CameraManager.get().stopPreview();
        activity.cameraManager!!.stopPreview()
        val quit = Message.obtain(decodeThread.getHandler(), MessageIDs.quit)
        quit.sendToTarget()
        try {
            decodeThread.join()
        } catch (e: InterruptedException) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(MessageIDs.decode_succeeded)
        removeMessages(MessageIDs.decode_failed)
    }

    private fun restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW
            // CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageIDs.decode);
            activity.cameraManager!!.requestPreviewFrame(decodeThread.getHandler()!!, MessageIDs.decode)
            // CameraManager.get().requestAutoFocus(this, MessageIDs.auto_focus);
            activity.cameraManager!!.requestAutoFocus(this, MessageIDs.auto_focus)
            activity.drawViewfinder()
        }
    }

    companion object {

        private val TAG = CaptureActivityHandler::class.java.simpleName
    }

}
