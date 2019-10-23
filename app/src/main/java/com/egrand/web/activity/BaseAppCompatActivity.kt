package com.egrand.web.activity


import android.annotation.TargetApi
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

    protected open fun showBack(): Boolean = true

    protected open fun useToolbar(): Boolean = true

    protected open fun useBack(): Boolean = true

    protected open fun useMenu(): Boolean = true

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

    protected open fun initToolbar() {
        if (this.useToolbar()) {
            toolbar.tvTitle.text = title
            if (this.useBack()) toolbar.ivBack.visibility = View.VISIBLE
            if (this.useMenu()) toolbar.ivMenu.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
//            if (this.showBack()) {
//                supportActionBar?.setDisplayHomeAsUpEnabled(true)
//                supportActionBar?.setDisplayShowHomeEnabled(true)
//                supportActionBar?.setHomeButtonEnabled(true)
//                toolbar.setNavigationOnClickListener { finish() }
//            }
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
     * @param on
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatus(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
                val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
                field.isAccessible = true
                field.setInt(window.decorView, Color.TRANSPARENT) //改为透明
            } catch (e: Exception) {
                Log.e("BaseAppCompatActivity", e.message)
                e.printStackTrace()
            }
        }
        val winParams = window.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        window.attributes = winParams
    }
}
