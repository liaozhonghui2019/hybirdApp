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

package com.egrand.web.qrcode.decoding

import android.content.Intent
import android.net.Uri
import com.google.zxing.BarcodeFormat
import java.util.*
import java.util.regex.Pattern

object DecodeFormatManager {

    val PRODUCT_FORMATS: Vector<BarcodeFormat>
    val ONE_D_FORMATS: Vector<BarcodeFormat>
    val QR_CODE_FORMATS: Vector<BarcodeFormat>
    val DATA_MATRIX_FORMATS: Vector<BarcodeFormat>

    private val COMMA_PATTERN = Pattern.compile(",")

    init {
        PRODUCT_FORMATS = Vector(5)
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_A)
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_E)
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_13)
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_8)
        PRODUCT_FORMATS.add(BarcodeFormat.RSS_14)//RSS14

        ONE_D_FORMATS = Vector(PRODUCT_FORMATS.size + 4)
        ONE_D_FORMATS.addAll(PRODUCT_FORMATS)
        ONE_D_FORMATS.add(BarcodeFormat.CODE_39)
        ONE_D_FORMATS.add(BarcodeFormat.CODE_93)
        ONE_D_FORMATS.add(BarcodeFormat.CODE_128)
        ONE_D_FORMATS.add(BarcodeFormat.ITF)

        QR_CODE_FORMATS = Vector(1)
        QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE)

        DATA_MATRIX_FORMATS = Vector(1)
        DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX)
    }

    internal fun parseDecodeFormats(intent: Intent): Vector<BarcodeFormat>? {
        var scanFormats: List<String>? = null
        val scanFormatsString = intent.getStringExtra(Intents.Scan.SCAN_FORMATS)
        if (scanFormatsString != null) {
            scanFormats = Arrays.asList(*COMMA_PATTERN.split(scanFormatsString))
        }
        return parseDecodeFormats(scanFormats, intent.getStringExtra(Intents.Scan.MODE))
    }

    internal fun parseDecodeFormats(inputUri: Uri): Vector<BarcodeFormat>? {
        var formats: List<String>? = inputUri.getQueryParameters(Intents.Scan.SCAN_FORMATS)
        if (formats != null && formats.size == 1 && formats[0] != null) {
            formats = Arrays.asList(*COMMA_PATTERN.split(formats[0]))
        }
        return parseDecodeFormats(formats, inputUri.getQueryParameter(Intents.Scan.MODE))
    }

    private fun parseDecodeFormats(scanFormats: Iterable<String>?, decodeMode: String?): Vector<BarcodeFormat>? {
        if (scanFormats != null) {
            val formats = Vector<BarcodeFormat>()
            try {
                for (format in scanFormats) {
                    formats.add(BarcodeFormat.valueOf(format))
                }
                return formats
            } catch (iae: IllegalArgumentException) {
                iae.printStackTrace()
            }

        }
        if (decodeMode != null) {
            if (Intents.Scan.PRODUCT_MODE == decodeMode) {
                return PRODUCT_FORMATS
            }
            if (Intents.Scan.QR_CODE_MODE == decodeMode) {
                return QR_CODE_FORMATS
            }
            if (Intents.Scan.DATA_MATRIX_MODE == decodeMode) {
                return DATA_MATRIX_FORMATS
            }
            if (Intents.Scan.ONE_D_MODE == decodeMode) {
                return ONE_D_FORMATS
            }
        }
        return null
    }

}
