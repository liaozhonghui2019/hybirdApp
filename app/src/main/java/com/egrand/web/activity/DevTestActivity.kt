package com.egrand.web.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egrand.web.R
import com.egrand.web.adpter.recyclerview.CommonAdapter
import com.egrand.web.adpter.recyclerview.MultiItemTypeAdapter
import com.egrand.web.adpter.recyclerview.ViewHolder
import com.egrand.web.constant.Constants
import com.egrand.web.entity.App
import kotlinx.android.synthetic.main.layout_toolbar.*

class DevTestActivity : BaseRecyclerViewActivity<App>() {

    override val layoutRes: Int = R.layout.activity_dev_test

    override fun useItemDecoration(): Boolean = false

    override fun useMenu(): Boolean = false

    override fun getLayoutManager(): RecyclerView.LayoutManager = GridLayoutManager(this, 4)

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun getAdapter(data: MutableList<App>, manager: FragmentManager): CommonAdapter<App> {

        adapter = object : CommonAdapter<App>(this, R.layout.item_menu, data) {

            override fun convert(holder: ViewHolder, item: App, position: Int) {
                holder.setText(R.id.tv_name, item.name)
                holder.setImageResource(R.id.iv_icon, item.icon)
            }
        }

        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return true
            }

            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                val app = data[position]
                if (!TextUtils.isEmpty(app.url)) {
                    val intent = Intent(this@DevTestActivity, WebActivity::class.java)
                    intent.putExtra("url", app.url)
                    startActivity(intent)
                } else {
                    showWarnDialog("尚未配置地址！")
                }
            }
        })

        return adapter
    }

    override fun loadPage(dir: Int) {
        var reqHost = "http://${Constants.SERVER_IP}";
        val data: MutableList<App> = arrayListOf(
            App(6, "公司开发-80", "$reqHost:80/", R.drawable.icon_99),
            App(8, "公司开发-8443", "$reqHost:8443/", R.drawable.icon_99),
            App(8, "公司开发-443", "$reqHost:443/", R.drawable.icon_99),
            App(1, "公司开发-8000", "$reqHost:8000/", R.drawable.icon_111),
            App(7, "公司开发-8080", "$reqHost:8080/", R.drawable.icon_87),
            App(2, "Hexo-4000", "http://192.168.31.230:4000/", R.drawable.icon_9),
            App(3, "H5测试-80", "http://192.168.31.230:80/", R.drawable.icon_79),
            App(3, "H5测试-8000", "http://192.168.31.230:8000/", R.drawable.icon_31),
            App(4, "H5测试-8080", "http://192.168.31.230:8080/", R.drawable.icon_78),
            App(5, "百度", "https://www.baidu.com/", R.drawable.icon_149)
        )
        onLoadDataFinish(data, dir, 1)
    }
}
