package com.egrand.web.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.egrand.web.R
import com.egrand.web.dao.AppDatabase
import com.egrand.web.db.AppDbHelper
import com.egrand.web.utils.Utils
import kotlinx.android.synthetic.main.activity_db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.egrand.web.activity.UserActivity as UserActivity1
import kotlinx.android.synthetic.main.layout_toolbar.*

class DBActivity : BaseAppCompatActivity() {

    override val layoutRes: Int = R.layout.activity_db
    override fun useToolbar()= true
    override fun useMenu() = false
    private var versionCode ="";

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        ivBack.setOnClickListener {
            this.onBackPressed()
        }
        initialDB()
        initListeners()

    }

    /**
     * 按钮监听事件
     */
    private fun initListeners() {
        var utils = Utils()

        //实现点击后跳转到对应页面
        //调用startActivity方法需要在项目中添加Anko相应资源
        //startActivity<TabDemo1>()是Anko的语法,等价于java中startActivity(MainActivity.this, HttpAct.class)
        widgets_btn.setOnClickListener {
            versionCode = utils.getVersionCode(this@DBActivity);
            showToast("appVersionCode:$versionCode")
        }
//        json_btn?.setOnClickListener {startActivity<JsonAct>()}
//        http_btn?.setOnClickListener {startActivity<HttpAct>()}

        btnDb.setOnClickListener { onBtnInsertClick() }
        btnUser.setOnClickListener {
            val intent = Intent(this@DBActivity, UserActivity1::class.java)
            startActivity(intent) }
    }

    private lateinit var db: AppDatabase

    private fun initialDB() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val dbHelper = AppDbHelper.get(applicationContext)
                db = dbHelper.getDb()
//              db.userDao().insertAll(User(1, "gg", "ff"))
                val user = db.userDao().findByName("gg")
                Log.d("User", "${user.name}")
//                    val data: MutableList<App> = arrayListOf(
//                        App(1, "广东省党建", "", R.drawable.icon_1),
//                        App(2, "轻工党建", "", R.drawable.icon_64),
//                        App(3, "会务管理", "", R.drawable.icon_119),
//                        App(4, "干部考核", "", R.drawable.icon_179),
//                        App(5, "网上考核", "", R.drawable.icon_148),
//                        App(6, "通讯录", "", R.drawable.icon_14),
//                        App(7, "仓库管理", "", R.drawable.icon_7),
//                        App(8, "广海OA", "", R.drawable.icon_4),
//                        App(9, "深圳党建", "http://10.1.10.14:81/", R.drawable.icon_130),
//                        App(10, "长春党建", "http://10.1.10.14/", R.drawable.icon_108),
//                        App(11, "惠服务", "", R.drawable.icon_40),
//                        App(12, "工作日志", "", R.drawable.icon_124),
//                        App(13, "三重一大", "http://10.1.10.221:8000/", R.drawable.icon_77),
//                        App(14, "三重一大(公网)", "http://120.79.143.26/", R.drawable.icon_77),
//                        App(15, "Github", "https://github.com/", R.drawable.icon_44),
//                        App(0L, "开发测试", "", R.drawable.icon_58)
//                    )
//                    db.appDao().insertAll(data)
                var appSize = db.appDao().getAll().size
                Log.d("APP", "$appSize")
                println("appSize:$appSize")

            } catch (e: Exception) {
                Log.e("APP", e.message)
                showToast("数据已插入错误！$e.message")
            }
        }
    }

    fun onBtnInsertClick() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val dbHelper = AppDbHelper.get(applicationContext)
                val db = dbHelper.getDb()
                val list = db.userDao().getAll()
                var msg = ""
                for (user in list) {
                    msg += "{id:" + user.uid + ",name:" + user.name + "}"
                }
                showToast("list:$msg,size:${list.size}")
            } catch (e: Exception) {
                Log.e("APP", e.message)
                showToast("数据已插入错误！$ e.message")
            }
        }
//        //插入一条用户数据
//        var values = ContentValues()
//        values.put(DBHelper.UserTable.NAME, "admin")
//        values.put(DBHelper.UserTable.AGE, 21)
//        values.put(DBHelper.UserTable.SEX, "male")
//        //插入成功就返回记录的id否则返回-1；
//        var newId = db.writableDatabase.insert(
//            DBHelper.UserTable.T_NAME,
//            null,
//            values
//        );//当然还有其它更简单方法可以实现同样功能
//        var msg = if (newId > -1) ("成功，id=" + newId) else "失败！"
//        showToast("插入" + msg)
    }

    fun getUserData() {
//        //获取到所有的学生列表
//        var userList =
//            db.writableDatabase.select(DBHelper.UserTable.T_NAME).parseList { User(HashMap(it)) }
//        var msg = "";
//        for (user in userList) {
//            Log.d("DBActivity", "id is: " + user._id)
//            Log.d("DBActivity", "name is: " + user.name)
//            //  Log.d("MainActivity","version is: "+app.version)
//            msg += "{id:" + user._id + ",name:" + user.name + "}"
//        }
//        showToast("插入" + msg)
    }


}
