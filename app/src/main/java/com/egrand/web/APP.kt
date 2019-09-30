package com.egrand.web

import android.app.Application

/**
* description：定义全局的Application
* 作者：lzh
* 时间：2019-09-02 15;30
*/
class APP : Application() {
    companion object {
        //定义Application对象
        private  var instance: Application? = null;

        //获取Application对象的方法
        fun instance() = instance!!
    }

    override fun onCreate() {
        super.onCreate()
        //实例化对象
        instance = this
    }
    //最后：不要忘记在AndroidManfest.xml中注册该APP
}
