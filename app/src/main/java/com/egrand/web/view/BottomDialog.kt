package com.egrand.web.view

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.egrand.web.R
import com.egrand.web.adpter.recyclerview.CommonAdapter
import com.egrand.web.adpter.recyclerview.MultiItemTypeAdapter
import com.egrand.web.adpter.recyclerview.ViewHolder
import com.egrand.web.entity.DialogMenuItem
import kotlinx.android.synthetic.main.dialog_bottom.*


/**
 *  ico on 2019/09/21.
 */
class BottomDialog(private var data: MutableList<DialogMenuItem>) : BaseFragmentDialog() {

    private lateinit var adapter: CommonAdapter<DialogMenuItem>
    private var mListener: MenuItemClickListener? = null
    private var source: String? = null
    override val layoutRes: Int = R.layout.dialog_bottom
    override fun windowAnimations() = R.style.FragmentDialogPull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerView.layoutManager = GridLayoutManager(activity, 5)
        (this.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        adapter = getAdapter(data)
        this.recyclerView.adapter = adapter
    }

    private fun getAdapter(data: MutableList<DialogMenuItem>): CommonAdapter<DialogMenuItem> {

        adapter = object : CommonAdapter<DialogMenuItem>(context!!, R.layout.item_menu_dialog, data) {

            override fun convert(holder: ViewHolder, item: DialogMenuItem, position: Int) {
                holder.setText(R.id.tv_name, item.name)
                holder.setImageResource(R.id.iv_icon, item.icon)
            }
        }

        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean = true

            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                val menuItem = data[position]
                mListener?.onClick(this@BottomDialog, menuItem)
            }
        })

        return adapter
    }

    interface MenuItemClickListener {
        fun onClick(dialog: BottomDialog, menuItem: DialogMenuItem)
    }

    fun setOnMenuItemClickListener(listener: MenuItemClickListener) {
        this.mListener = listener
    }

    fun updateWebSource(source: String?) {
        this.source = source
    }

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(source)) {
            this.tvUrl?.visibility = View.VISIBLE
            this.tvUrl?.text = "网页由 $source 提供"
        } else {
            this.tvUrl?.visibility = View.GONE
            this.tvUrl?.text = ""
        }
    }

    override fun onStart() {
        super.onStart()
        // 下面这些设置必须在此方法(onStart())中才有效
        val window = dialog?.window
        // 如果不设置这句代码, 那么弹框就会与四边都有一定的距离
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        // 设置动画
        window?.attributes?.gravity = Gravity.BOTTOM
        // 如果不设置宽度,那么即使你在布局中设置宽度为 match_parent 也不会起作用
        window?.attributes?.width = resources.displayMetrics.widthPixels
        window?.attributes = window?.attributes
    }

}
