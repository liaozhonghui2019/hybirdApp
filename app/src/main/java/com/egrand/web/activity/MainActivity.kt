package com.egrand.web.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egrand.web.R
import com.egrand.web.adpter.recyclerview.CommonAdapter
import com.egrand.web.adpter.recyclerview.MultiItemTypeAdapter
import com.egrand.web.adpter.recyclerview.ViewHolder
import com.egrand.web.entity.App
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseRecyclerViewActivity<App>() {

    override val layoutRes: Int = R.layout.activity_main

    override fun useItemDecoration(): Boolean = false

    override fun showBack(): Boolean = false

    override fun useMenu(): Boolean = false

    override fun useBack(): Boolean = false

    override fun getLayoutManager(): RecyclerView.LayoutManager = GridLayoutManager(this, 4)

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        title = "应用中心"

        /*GlobalScope.launch(Dispatchers.Main) {
            try {
                val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "hybird-db").build()
                db.userDao().insertAll(User(1, "gg", "ff"))
                val user = db.userDao().findByName("gg", "ff")
                Log.d("User", "${user.firstName}")
                val data: MutableList<App> = arrayListOf(
                    App(1, "广东省党建", "", R.drawable.icon_1),
                    App(2, "轻工党建", "", R.drawable.icon_64),
                    App(3, "会务管理", "", R.drawable.icon_119),
                    App(4, "干部考核", "", R.drawable.icon_179),
                    App(5, "网上考核", "", R.drawable.icon_148),
                    App(6, "通讯录", "", R.drawable.icon_14),
                    App(7, "仓库管理", "", R.drawable.icon_7),
                    App(8, "广海OA", "", R.drawable.icon_4),
                    App(9, "深圳党建", "http://10.1.10.14:81/", R.drawable.icon_130),
                    App(99, "长春党建", "http://10.1.10.14/", R.drawable.icon_108),
                    App(10, "惠服务", "", R.drawable.icon_40),
                    App(11, "工作日志", "", R.drawable.icon_124),
                    App(99, "三重一大", "http://10.1.10.221:8000/", R.drawable.icon_77),
                    App(99, "三重一大(公网)", "http://120.79.143.26/", R.drawable.icon_77),
                    App(12, "Github", "https://github.com/", R.drawable.icon_44),
                    App(0L, "开发测试", "", R.drawable.icon_58)
                )
                db.appDao().insertAll(data)
                Log.d("APP", "${db.appDao().getAll().size}")
            } catch (e: Exception) {
                showToast("数据已插入！")
            }
        }*/
    }

    override fun getAdapter(data: MutableList<App>, manager: FragmentManager): CommonAdapter<App> {

        adapter = object : CommonAdapter<App>(this, R.layout.item_menu, data) {

            override fun convert(holder: ViewHolder, item: App, position: Int) {
                holder.setText(R.id.tv_name, item.name)
                holder.setImageResource(R.id.iv_icon, item.icon)
            }
        }

        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean = true

            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                val app = data[position]
                if (!TextUtils.isEmpty(app.url)) {
                    val intent = Intent(this@MainActivity, WebActivity::class.java)
                    intent.putExtra("url", app.url)
                    startActivity(intent)
                } else if (app.id == 0L) {
                    val intent = Intent(this@MainActivity, DevTestActivity::class.java)
                    startActivity(intent)
                } else if (app.id == 1L) {
                    val intent = Intent(this@MainActivity, DBActivity::class.java)
                    startActivity(intent)
                } else if (app.id == 99L) {
                    val intent = Intent(this@MainActivity, ListViewActivity::class.java)
                    startActivity(intent)
                } else {
                    showWarnDialog("尚未配置地址！")
                }
            }
        })

        return adapter
    }

    override fun loadPage(dir: Int) {
        val data: MutableList<App> = arrayListOf(
//            App(1, "广东省党建", "", R.drawable.icon_1),
//            App(2, "轻工党建", "", R.drawable.icon_64),
//            App(3, "会务管理", "", R.drawable.icon_119),
//            App(4, "干部考核", "", R.drawable.icon_179),
//            App(5, "网上考核", "", R.drawable.icon_148),
//            App(6, "通讯录", "", R.drawable.icon_14),
//            App(7, "仓库管理", "", R.drawable.icon_7),
//            App(8, "广海OA", "", R.drawable.icon_4),
            App(9, "深圳党建", "http://10.1.10.14:81/", R.drawable.icon_130),
            App(99, "长春党建", "http://10.1.10.14/", R.drawable.icon_108),
       //     App(10, "惠服务", "", R.drawable.icon_40),
            App(11, "工作日志", "", R.drawable.icon_124),
            App(99, "三重一大", "http://10.1.10.221:8000/", R.drawable.icon_77),
            App(99, "三重一大(8443)", "https://10.1.10.221:8443/", R.drawable.icon_77),
            App(99, "三重一大(公网)", "http://120.79.143.26/", R.drawable.icon_77),
            App(12, "Github", "https://github.com/", R.drawable.icon_44),
            App(0, "开发测试", "", R.drawable.icon_58),
            App(99, "ListView", "", R.drawable.icon_58),
            App(1L, "数据库测试", "", R.drawable.icon_58)
        )
        onLoadDataFinish(data, dir, 1)
    }

}
