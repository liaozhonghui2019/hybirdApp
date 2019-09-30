package com.egrand.web.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast

class Utils {
    /**
     * show toast in activity
     */
    fun Activity.toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 获取版本号VersionCode
     */
    fun getVersionCode(context: Context): String {
        val packageManager = context.packageManager
        val packageInfo: PackageInfo
        var versionCode = ""
        try {
            packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            versionCode = packageInfo.versionName + ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode;
    }

    /**
     * 实现复制到粘贴板功能
     */
    fun copy2Clipboard(context: Context?, text: String?) {
        if (context == null || text == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= 11) {
            var clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager
            val clipData = ClipData.newPlainText(null, text)
            clipboardManager.setPrimaryClip(clipData)
        } else {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as android.text.ClipboardManager
            clipboardManager.text = text
        }
    }

}
