package com.egrand.web.activity


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.egrand.web.AndroidBug5497Workaround
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*


abstract class BaseAppCompatActivity : AppCompatActivity() {

    /**
     * 获取布局文件资源id
     *
     * @return 布局文件资源id
     */
    protected abstract val layoutRes: Int

    protected open fun useToolbar() = true

    protected open fun useBack() = true

    protected open fun useMenu() = true

    /**
     * [.onCreate]执行完成后立马执行该方法,子类可以复写该方法做一些数据绑定操作
     */
    protected open fun onCreated(savedInstanceState: Bundle?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutRes)
        AndroidBug5497Workaround.assistActivity(this)
        initToolbar()
        this.onCreated(savedInstanceState)
    }

    protected open fun showToast(msg: CharSequence) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    protected open fun showSuccessDialog(msg: String) {
        if (!isFinishing) {
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE).setTitleText(msg).show()
        }
    }

    protected open fun showWarnDialog(msg: String) {
        if (!isFinishing) {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(msg).show()
        }
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        if (this.useToolbar()) {
            toolbar.tvTitle.text = getTitle()
        }
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        if (this.useToolbar()) {
            toolbar.tvTitle.text = title
        }
    }

    protected open fun onCreateMenu() {}

    protected open fun initToolbar() {
        if (this.useToolbar()) {
            toolbar.tvTitle.text = title
            if (this.useBack()) {
                toolbar.ivBack.visibility = View.VISIBLE
                toolbar.ivBack.setOnClickListener {
                    this.onBackPressed()
                }
            }

            if (this.useMenu()) {
                toolbar.ivMenu.visibility = View.VISIBLE
                toolbar.ivMenu.setOnClickListener {
                    this.onCreateMenu()
                }
            }

            setSupportActionBar(toolbar)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 && topBar != null) {
                setTranslucentStatus(true)
                var result = 0
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    result = resources.getDimensionPixelSize(resourceId)
                }
                val layoutParams = toolbar.layoutParams as RelativeLayout.LayoutParams
                layoutParams.topMargin += result
                toolbar.layoutParams = layoutParams

                val params = topBar.layoutParams
                params.height += result
                topBar.layoutParams = params
            }
        }
    }

    /**
     * 设置透明状态栏
     * 对4.4及以上版本有效
     *
     * @param translucentStatus
     */
    private fun setTranslucentStatus(translucentStatus: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }

        if (translucentStatus) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        //Android 7.0 状态栏背景灰色 bug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
                val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
                field.isAccessible = true
                field.setInt(window.decorView, Color.TRANSPARENT)
            } catch (e: Exception) {
                Log.e("BaseAppCompatActivity", e.message)
                e.printStackTrace()
            }
        }
    }

    /**
     * 调节状态栏文字颜色
     * @param dark 是否使用深色
     */
    protected fun adjustStatusBarTextColor(dark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                //状态栏文字颜色改为黑色
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                //状态栏文字颜色改为白色
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }
}
