package com.egrand.web.qrcode.decoding

import android.graphics.Bitmap

import com.google.zxing.LuminanceSource

class BitmapLuminanceSource(bitmap: Bitmap) : LuminanceSource(bitmap.width, bitmap.height) {

    private val bitmapPixels: ByteArray

    init {

        // 首先，要取得该图片的像素数组内容
        val data = IntArray(bitmap.width * bitmap.height)
        this.bitmapPixels = ByteArray(bitmap.width * bitmap.height)
        bitmap.getPixels(data, 0, width, 0, 0, width, height)

        // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
        for (i in data.indices) {
            this.bitmapPixels[i] = data[i].toByte()
        }
    }

    override fun getMatrix(): ByteArray {
        // 返回我们生成好的像素数据
        return bitmapPixels
    }

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        // 这里要得到指定行的像素数据
        System.arraycopy(bitmapPixels, y * width, row, 0, width)
        return row
    }
}
