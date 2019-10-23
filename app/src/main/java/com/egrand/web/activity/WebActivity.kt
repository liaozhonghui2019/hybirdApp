package com.egrand.web.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.egrand.web.R
import com.egrand.web.entity.DialogMenuItem
import com.egrand.web.qrcode.CaptureActivity
import com.egrand.web.view.BaseFragmentDialog
import com.egrand.web.utils.Utils
import com.egrand.web.view.BottomDialog
import com.egrand.web.view.QRCodeDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.*


class WebActivity : BaseAppCompatActivity() {

    private var bottomDialog: BottomDialog? = null
    private var requestCode = 10000

    override val layoutRes = R.layout.activity_web

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        webView.requestFocus()
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.settings.setAppCacheEnabled(true)
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.javaScriptEnabled = true
        // WebView在安卓5.0之前默认允许其加载混合网络协议内容
        // 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置WebView允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        // WebView里的字体不随系统字体大小设置发生变化
        webView.settings.textZoom = 100
        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progress.progress = 0
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progress.progress = 100
                progress.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("tel:")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
                if (url.startsWith("sms:")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
                if (url.startsWith("http", true) || url.startsWith("https", true)) {
                    view.loadUrl(url)
                }
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progress.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                //防止显示 url
                if (title != null && !view.url.contains(title)) {
                    tvTitle.text = title
                }
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                val builder = AlertDialog.Builder(view.context)
                builder.setMessage(message).setCancelable(false).setPositiveButton("确定") { _, _ -> result.confirm() }
                val dialog = builder.create()
                dialog.show()
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Toast.makeText(this@WebActivity, consoleMessage?.message(), Toast.LENGTH_SHORT).show()
                return true
            }
        }

        val intent = this.intent
        val url = intent.getStringExtra("url")
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            val url = data?.getStringExtra("url")
            if (!TextUtils.isEmpty(url)) {
                this.webView.loadUrl(url)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateMenu() {
        if (bottomDialog == null) {
            val data: MutableList<DialogMenuItem> = arrayListOf(
                DialogMenuItem(1, "刷新", "", R.drawable.ic_refresh),
                DialogMenuItem(2, "关闭", "", R.drawable.ic_exit),
                DialogMenuItem(3, "添加网址", "", R.drawable.ic_add_url),
                DialogMenuItem(-1, "历史", "", R.drawable.ic_history),
                DialogMenuItem(-1, "收藏", "", R.drawable.ic_favorite),
                DialogMenuItem(-1, "设置", "", R.drawable.ic_settings),
                DialogMenuItem(7, "生成二维码", "", R.drawable.ic_generate),
                DialogMenuItem(8, "扫码", "", R.drawable.ic_scan_light),
                DialogMenuItem(-1, "更多工具", "", R.drawable.ic_more)
            )
            bottomDialog = BottomDialog(data)
            bottomDialog!!.setOnMenuItemClickListener(object : BottomDialog.MenuItemClickListener {
                override fun onClick(dialog: BottomDialog, menuItem: DialogMenuItem) {
                    when (menuItem.id) {
                        1L -> {
                            webView.reload()
                            dialog.dismiss()
                        }
                        2L -> {
                            dialog.dismiss()
                            this@WebActivity.finish()
                        }
						3L ->{
                                dialog.dismiss();
                                var url = this@WebActivity.webView.url
                                var utils = Utils()
                                utils.copy2Clipboard(this@WebActivity,url )
                                showToast("已复制到剪切板")
                            }
                        7L -> {
                            dialog.dismiss()
                            generate()
                        }
                        8L -> {
                            dialog.dismiss()
                            startActivityForResult(Intent(this@WebActivity, CaptureActivity::class.java), requestCode)
                        }
                        else -> {
                            dialog.dismiss()
                            showToast("敬请期待！")
                        }
                    }
                }
            })
        }
        var url: String? = null
        if (!TextUtils.isEmpty(webView.url)) {
            val uri = Uri.parse(webView.url)
            url = uri.host
        }

        bottomDialog!!.updateWebSource(url)
        bottomDialog!!.show(supportFragmentManager, "web-bottom-dialog")
    }

    override fun onStop() {
        //取消屏幕最亮
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        super.onStop()
    }

    /**
     * 设置当前窗口亮度
     *
     * @param brightness
     */
    private fun setWindowBrightness(brightness: Float) {
        val window = window
        val lp = window.attributes
        lp.screenBrightness = brightness
        window.attributes = lp
    }

    @SuppressLint("StaticFieldLeak")
    private fun generate() {
        if (TextUtils.isEmpty(this.webView.url)) {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("温馨提示")
            builder.setMessage("地址为空！")
            builder.create().show()
            return
        }
        object : AsyncTask<String?, Void, Bitmap>() {

            override fun doInBackground(vararg params: String?): Bitmap? {
                val qrCodeWriter = QRCodeWriter()
                val hints = HashMap<EncodeHintType, String>()
                hints[EncodeHintType.CHARACTER_SET] = "UTF8"
                try {
                    val height = 1000
                    val width = 1000
                    val encode = qrCodeWriter.encode(params[0], BarcodeFormat.QR_CODE, width, height, hints)

                    val pixels = IntArray(width * height)

                    for (i in 0 until height) {
                        for (j in 0 until width) {
                            if (encode.get(j, i)) {
                                pixels[i * width + j] = Color.BLACK
                            } else {
                                pixels[i * width + j] = 0x00ffffff
                            }
                        }
                    }
                    return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(bitmap: Bitmap) {
                //设置屏幕亮度最大
                setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL)
                val dialog = QRCodeDialog(bitmap)
                dialog.setDismissListener(object : BaseFragmentDialog.LocalDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        //取消屏幕最亮
                        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
                    }
                })
                dialog.show(supportFragmentManager, "QRCodeDialog")
            }
        }.execute(webView.url)
    }

    override fun onDestroy() {
        webView.removeAllViews()
        webView.loadUrl("about:blank")
        webView.destroy()
        super.onDestroy()
    }

}
