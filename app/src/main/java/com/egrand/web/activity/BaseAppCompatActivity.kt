package com.egrand.web.activity


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseAppCompatActivity : AppCompatActivity() {

    /**
     * 获取布局文件资源id
     *
     * @return 布局文件资源id
     */
    protected abstract val layoutRes: Int

    protected open fun showBack(): Boolean = true

    protected open fun useToolbar(): Boolean = true

    /**
     * [.onCreate]执行完成后立马执行该方法,子类可以复写该方法做一些数据绑定操作
     */
    protected open fun onCreated(savedInstanceState: Bundle?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.layoutRes)
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

    protected open fun initToolbar() {
        if (this.useToolbar()) {
            setSupportActionBar(toolbar)
//            if (this.showBack()) {
//                supportActionBar?.setDisplayHomeAsUpEnabled(true)
//                supportActionBar?.setDisplayShowHomeEnabled(true)
//                supportActionBar?.setHomeButtonEnabled(true)
//                toolbar.setNavigationOnClickListener { finish() }
//            }
        }
    }
}
