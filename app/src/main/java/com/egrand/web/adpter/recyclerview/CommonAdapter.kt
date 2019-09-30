package com.egrand.web.adpter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import com.egrand.web.adpter.recyclerview.base.ItemViewDelegate

/**
 *
 */
abstract class CommonAdapter<T>(final override var mContext: Context, protected var mLayoutId: Int, protected open var mDatas: List<T>) : MultiItemTypeAdapter<T>(mContext, mDatas) {
    protected var mInflater: LayoutInflater = LayoutInflater.from(mContext)

    init {
        addItemViewDelegate(object : ItemViewDelegate<T> {
            override val itemViewLayoutId: Int = mLayoutId

            override fun isForViewType(item: T, position: Int): Boolean {
                return true
            }

            override fun convert(holder: ViewHolder, item: T, position: Int) {
                this@CommonAdapter.convert(holder, item, position)
            }
        })
    }

    protected abstract fun convert(holder: ViewHolder, item: T, position: Int)

}
