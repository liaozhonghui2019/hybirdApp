package com.egrand.web.adpter.abslistview

import android.content.Context
import com.egrand.web.adpter.abslistview.base.ItemViewDelegate

abstract class CommonAdapter<T>(context: Context, layoutId: Int, datas: List<T>) : MultiItemTypeAdapter<T>(context, datas) {

    init {
        addItemViewDelegate(object : ItemViewDelegate<T> {
            override val itemViewLayoutId: Int = layoutId

            override fun isForViewType(item: T, position: Int): Boolean {
                return true
            }

            override fun convert(holder: ViewHolder, item: T, position: Int) {
                this@CommonAdapter.convert(holder, item, position)
            }
        })
    }

    abstract override fun convert(viewHolder: ViewHolder, item: T, position: Int)

}
