package com.egrand.web.view

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.egrand.web.R

/**
 * ico on 2017/10/10.
 */

abstract class BaseFragmentDialog : DialogFragment() {


    private var mListener: LocalDismissListener? = null

    /**
     * 获取布局文件资源id
     *
     * @return 布局文件资源id
     */
    protected abstract val layoutRes: Int

    fun setDismissListener(mListener: LocalDismissListener) {
        this.mListener = mListener
    }

    protected open fun canCancel(): Boolean {
        return true
    }

    protected open fun fullScreen(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = this.canCancel()
        if (fullScreen()) {
            setStyle(STYLE_NORMAL, R.style.FragmentDialog)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (dialog != null) {
            val window = dialog!!.window
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE)// 隐藏标题
                window.attributes.windowAnimations = R.style.FragmentDialog
            }
        }
        return inflater.inflate(layoutRes, container, false)
    }

    // 注意：只有在这里才能设置dialog的大小
    override fun onResume() {
        super.onResume()
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        if (!fullScreen()) {
            dialog!!.window!!.setLayout(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        if (!canCancel()) {
            dialog!!.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss()
                    return@OnKeyListener true
                }
                false
            })
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mListener != null) {
            mListener!!.onDismiss(dialog)
        }
    }

    protected fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    protected fun showToast() {
        Toast.makeText(context, "暂未实现!", Toast.LENGTH_SHORT).show()
    }

    interface LocalDismissListener {
        fun onDismiss(dialog: DialogInterface)
    }
}
