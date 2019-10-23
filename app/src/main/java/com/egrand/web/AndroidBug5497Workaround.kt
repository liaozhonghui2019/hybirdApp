package com.egrand.web

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.FrameLayout

class AndroidBug5497Workaround private constructor(activity: Activity) {
    // https://www.jianshu.com/p/87795b9cda4b
    // https://github.com/madebycm/AndroidBug5497Workaround
    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    private val mChildOfContent: View
    private var usableHeightPrevious: Int = 0
    private val frameLayoutParams: FrameLayout.LayoutParams

    init {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        mChildOfContent = content.getChildAt(0)
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent(activity) }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val usableHeightNow = computeUsableHeight(activity)
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard
            }
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(activity: Activity): Int {
        // Rect r = new Rect();
        // mChildOfContent.getWindowVisibleDisplayFrame(r);
        // return (r.bottom - r.top);
        val frame = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(frame)
        val statusBarHeight = frame.top
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)

        //这个判断是为了解决19之后的版本在弹出软键盘时，键盘和推上去的布局（adjustResize）之间有黑色区域的问题
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            r.bottom - r.top + statusBarHeight
        } else r.bottom - r.top
    }

    companion object {

        fun assistActivity(activity: Activity) {
            AndroidBug5497Workaround(activity)
        }
    }

}
