/*
 * Copyright 2009 ZXing authors
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

import android.graphics.Bitmap

import com.google.zxing.LuminanceSource
import kotlin.experimental.and

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 *
 *
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class PlanarYUVLuminanceSource(private val yuvData: ByteArray, private val dataWidth: Int, private val dataHeight: Int, private val left: Int, private val top: Int, width: Int, height: Int, reverseHorizontal: Boolean) : LuminanceSource(width, height) {

    init {

        require(!(left + width > dataWidth || top + height > dataHeight)) { "Crop rectangle does not fit within image data." }
        if (reverseHorizontal) {
            reverseHorizontal(width, height)
        }
    }

    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        var row = row
        require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }
        val width = width
        if (row == null || row.size < width) {
            row = ByteArray(width)
        }
        val offset = (y + top) * dataWidth + left
        System.arraycopy(yuvData, offset, row, 0, width)
        return row
    }

    override fun getMatrix(): ByteArray {
        val width = width
        val height = height

        // If the caller asks for the entire underlying image, save the copy and give them the
        // original data. The docs specifically warn that result.length must be ignored.
        if (width == dataWidth && height == dataHeight) {
            return yuvData
        }

        val area = width * height
        val matrix = ByteArray(area)
        var inputOffset = top * dataWidth + left

        // If the width matches the full width of the underlying data, perform a single copy.
        if (width == dataWidth) {
            System.arraycopy(yuvData, inputOffset, matrix, 0, area)
            return matrix
        }

        // Otherwise copy one cropped row at a time.
        val yuv = yuvData
        for (y in 0 until height) {
            val outputOffset = y * width
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width)
            inputOffset += dataWidth
        }
        return matrix
    }

    override fun isCropSupported(): Boolean {
        return true
    }

    fun renderCroppedGreyscaleBitmap(): Bitmap {
        val width = width
        val height = height
        val pixels = IntArray(width * height)
        val yuv = yuvData
        var inputOffset = top * dataWidth + left

        for (y in 0 until height) {
            val outputOffset = y * width
            for (x in 0 until width) {
                val grey = yuv[inputOffset + x] and 0xff.toByte()
                pixels[outputOffset + x] = -0x1000000 or grey * 0x00010101
            }
            inputOffset += dataWidth
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun reverseHorizontal(width: Int, height: Int) {
        val yuvData = this.yuvData
        var y = 0
        var rowStart = top * dataWidth + left
        while (y < height) {
            val middle = rowStart + width / 2
            var x1 = rowStart
            var x2 = rowStart + width - 1
            while (x1 < middle) {
                val temp = yuvData[x1]
                yuvData[x1] = yuvData[x2]
                yuvData[x2] = temp
                x1++
                x2--
            }
            y++
            rowStart += dataWidth
        }
    }

}
