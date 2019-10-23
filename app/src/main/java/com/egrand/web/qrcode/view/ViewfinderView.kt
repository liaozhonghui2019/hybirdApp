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

package com.egrand.web.qrcode.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.egrand.web.R
import com.egrand.web.qrcode.camera.CameraManager
import com.google.zxing.ResultPoint
import java.util.*


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class ViewfinderView// This constructor is used when the class is built from an XML resource.
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val maskColor: Int
    private val resultColor: Int
    private val resultPointColor: Int
    internal var isFirst: Boolean = false
    /**
     * 四个绿色边角对应的长度
     */
    private val ScreenRate: Int
    /**
     * 画笔对象的引用
     */
    private val paint: Paint
    /**
     * 中间滑动线的最顶端位置
     */
    private var slideTop: Int = 0
    /**
     * 中间滑动线的最底端位置
     */
    private var slideBottom: Int = 0
    /**
     * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
     */
    private var resultBitmap: Bitmap? = null
    private val possibleResultPoints: MutableCollection<ResultPoint>
    private val lastPossibleResultPoints: Collection<ResultPoint>?
    private var cameraManager: CameraManager? = null

    init {

        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskColor = 0x60000000
        resultColor = -0x50000000
        resultPointColor = -0x3f000100
        possibleResultPoints = ArrayList(5)
        lastPossibleResultPoints = null

        density = context.resources.displayMetrics.density
        // 将像素转换成dp
        ScreenRate = (20 * density).toInt()
    }

    fun setCameraManager(cameraManager: CameraManager) {
        this.cameraManager = cameraManager
    }

    public override fun onDraw(canvas: Canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        val frame = cameraManager!!.framingRect ?: return

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true
            slideTop = frame.top
            slideBottom = frame.bottom
        }

        // 获取屏幕的宽和高
        val width = canvas.width
        val height = canvas.height

        paint.color = if (resultBitmap != null) resultColor else maskColor

        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), (frame.bottom + 1).toFloat(), paint)
        canvas.drawRect((frame.right + 1).toFloat(), frame.top.toFloat(), width.toFloat(), (frame.bottom + 1).toFloat(), paint)
        canvas.drawRect(0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = OPAQUE
            canvas.drawBitmap(resultBitmap!!, frame.left.toFloat(), frame.top.toFloat(), paint)
        } else {

            // Draw a two pixel solid black border inside the framing rect
            paint.color = -0x1
            canvas.drawRect(frame.left.toFloat(), frame.top.toFloat(), (frame.right + 1).toFloat(), (frame.top + 2).toFloat(), paint)
            canvas.drawRect(frame.left.toFloat(), (frame.top + 2).toFloat(), (frame.left + 2).toFloat(), (frame.bottom - 1).toFloat(), paint)
            canvas.drawRect((frame.right - 1).toFloat(), frame.top.toFloat(), (frame.right + 1).toFloat(), (frame.bottom - 1).toFloat(), paint)
            canvas.drawRect(frame.left.toFloat(), (frame.bottom - 1).toFloat(), (frame.right + 1).toFloat(), (frame.bottom + 1).toFloat(), paint)

            // 这里画取景框四个角落的夹角
            paint.color = -0xf13c01
            paint.isAntiAlias = true
            canvas.drawRect((frame.left - CORNER_WIDTH + 2).toFloat(), (frame.top - CORNER_WIDTH + 2).toFloat(), (frame.left + ScreenRate - CORNER_WIDTH + 2).toFloat(), (frame.top + 2).toFloat(), paint)
            canvas.drawRect((frame.left - CORNER_WIDTH + 2).toFloat(), (frame.top - CORNER_WIDTH + 2).toFloat(), (frame.left + 2).toFloat(), (frame.top + ScreenRate - CORNER_WIDTH + 2).toFloat(), paint)
            canvas.drawRect((frame.right - ScreenRate + CORNER_WIDTH - 2).toFloat(), (frame.top - CORNER_WIDTH + 2).toFloat(), (frame.right + CORNER_WIDTH - 2).toFloat(), (frame.top + 2).toFloat(), paint)
            canvas.drawRect((frame.right - 2).toFloat(), (frame.top - CORNER_WIDTH + 2).toFloat(), (frame.right + CORNER_WIDTH - 2).toFloat(), (frame.top + ScreenRate - CORNER_WIDTH + 2).toFloat(), paint)

            canvas.drawRect((frame.left - CORNER_WIDTH + 2).toFloat(), (frame.bottom - 2).toFloat(), (frame.left + ScreenRate - CORNER_WIDTH + 2).toFloat(), (frame.bottom + CORNER_WIDTH - 2).toFloat(), paint)
            canvas.drawRect((frame.left - CORNER_WIDTH + 2).toFloat(), (frame.bottom - ScreenRate + CORNER_WIDTH - 2).toFloat(), (frame.left + 2).toFloat(), (frame.bottom + CORNER_WIDTH - 2).toFloat(), paint)
            canvas.drawRect((frame.right - ScreenRate + CORNER_WIDTH - 2).toFloat(), (frame.bottom - 2).toFloat(), (frame.right + CORNER_WIDTH - 2).toFloat(), (frame.bottom + CORNER_WIDTH - 2).toFloat(), paint)
            canvas.drawRect((frame.right - 2).toFloat(), (frame.bottom - ScreenRate + CORNER_WIDTH - 2).toFloat(), (frame.right + CORNER_WIDTH - 2).toFloat(), (frame.bottom + CORNER_WIDTH - 2).toFloat(), paint)

            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE
            if (slideTop >= frame.bottom) {
                slideTop = frame.top
            }

            val lineRect = Rect()
            lineRect.left = frame.left
            lineRect.right = frame.right
            lineRect.top = slideTop
            lineRect.bottom = slideTop + 18
            canvas.drawBitmap((resources.getDrawable(R.drawable.qr_scan_line) as BitmapDrawable).bitmap, null, lineRect, paint)

            /** 不显示关键点
             * Collection<ResultPoint> currentPossible = possibleResultPoints;
             * Collection<ResultPoint> currentLast = lastPossibleResultPoints;
             * if (currentPossible.isEmpty()) {
             * lastPossibleResultPoints = null;
             * } else {
             * possibleResultPoints = new HashSet<ResultPoint>(5);
             * lastPossibleResultPoints = currentPossible;
             * paint.setAlpha(OPAQUE);
             * paint.setColor(resultPointColor);
             * for (ResultPoint point : currentPossible) {
             * canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
             * }
             * }
             * if (currentLast != null) {
             * paint.setAlpha(OPAQUE / 2);
             * paint.setColor(resultPointColor);
             * for (ResultPoint point : currentLast) {
             * canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
             * }
             * }
            </ResultPoint></ResultPoint></ResultPoint> */
            // 只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom)

        }
    }

    fun drawViewfinder() {
        val resultBitmap = this.resultBitmap
        this.resultBitmap = null
        resultBitmap?.recycle()
        invalidate()
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    fun drawResultBitmap(barcode: Bitmap) {
        resultBitmap = barcode
        invalidate()
    }

    fun addPossibleResultPoint(point: ResultPoint) {
        possibleResultPoints.add(point)
    }

    companion object {
        /**
         * 刷新界面的时间
         */
        private val ANIMATION_DELAY = 30L
        private val OPAQUE = 0xFF
        private val MAX_RESULT_POINTS = 5
        /**
         * 四个绿色边角对应的宽度
         */
        private val CORNER_WIDTH = 10
        /**
         * 扫描框中的中间线的宽度
         */
        private val MIDDLE_LINE_WIDTH = 6
        /**
         * 扫描框中的中间线的与扫描框左右的间隙
         */
        private val MIDDLE_LINE_PADDING = 5
        /**
         * 中间那条线每次刷新移动的距离
         */
        private val SPEEN_DISTANCE = 5
        /**
         * 字体大小
         */
        private val TEXT_SIZE = 16
        /**
         * 字体距离扫描框下面的距离
         */
        private val TEXT_PADDING_TOP = 30
        /**
         * 手机的屏幕密度
         */
        private var density: Float = 0.0f
    }
}
