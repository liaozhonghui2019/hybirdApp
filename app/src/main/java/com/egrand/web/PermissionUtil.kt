package com.egrand.web

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

object PermissionUtil {

    /**
     * 跳转到系统应用设置页面
     *
     * @param activity
     */
    private fun toSettings(activity: Activity) {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            localIntent.data = Uri.fromParts("package", activity.packageName, null)
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.action = Intent.ACTION_VIEW
            localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.packageName)
        }
        activity.startActivity(localIntent)
    }

    /**
     * 判断是否有某个权限
     *
     * @param context
     * @param permission
     * @return
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 弹出对话框请求权限
     *
     * @param activity    页面
     * @param permissions 你要的权限
     * @param requestCode
     */
    fun requestPermissions(activity: Activity, permissions: Array<Permission>, requestCode: Int) {
        val deniedPermissions = getDeniedPermissions(activity, permissions)
        if (deniedPermissions != null) {
            ActivityCompat.requestPermissions(activity, deniedPermissions, requestCode)
        }
    }

    /**
     * 返回缺失的权限
     *
     * @param context     应用上下文
     * @param permissions 你要申请的权限
     * @return 返回缺少的权限，null 意味着没有缺少权限
     */
    private fun getDeniedPermissions(context: Context, permissions: Array<Permission>): Array<String>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val deniedPermissionList = ArrayList<String>()
            for (permission in permissions) {
                if (context.checkSelfPermission(permission.name) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permission.name)
                }
            }
            val size = deniedPermissionList.size
            if (size > 0) {
                return deniedPermissionList.toTypedArray()
            }
        }
        return null
    }

    fun requestPermission(activity: Activity, permission: Permission) {
        if (ContextCompat.checkSelfPermission(activity, permission.name) == PackageManager.PERMISSION_DENIED) { //判断是否已经赋予权限


            val builder = AlertDialog.Builder(activity)
            builder.setTitle("权限申请")
            builder.setCancelable(false)
            builder.setMessage(permission.desc)
            builder.setPositiveButton("去设置") { _, _ ->
                toSettings(activity)
                activity.finish()
            }
            builder.setNegativeButton("取消") { _, _ ->
                activity.finish()
            }
            builder.create().show()
        }
    }

    /**
     * 申请单个权限
     *
     * @param activity
     * @param permission
     * @param requestCode
     */
    fun requestPermission(activity: Activity, permission: Permission, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(activity, permission.name) == PackageManager.PERMISSION_DENIED) { //判断是否已经赋予权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.name)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再申请权限
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("温馨提示")
                builder.setCancelable(false)
                builder.setMessage(permission.desc)
                builder.setPositiveButton("确定") { _, _ ->
                    ActivityCompat.requestPermissions(activity, arrayOf(permission.name), requestCode)
                }
                builder.setNegativeButton("取消") { _, _ ->
                    activity.finish()
                }
                builder.create().show()
            } else {//用户点了拒绝且勾选了不再询问
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("温馨提示")
                builder.setCancelable(false)
                builder.setMessage(permission.desc)
                builder.setPositiveButton("去设置") { _, _ ->
                    toSettings(activity)
                    activity.finish()
                }
                builder.setNegativeButton("取消") { _, _ ->
                    activity.finish()
                }
                builder.create().show()
            }
        }
    }

    class Permission(var name: String, var desc: String)
}
