package com.egrand.web.adpter.abslistview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.egrand.web.adpter.abslistview.base.ItemViewDelegate
import com.egrand.web.adpter.abslistview.base.ItemViewDelegateManager

open class MultiItemTypeAdapter<T>(protected open var mContext: Context, protected open var mDatas: List<T>) : BaseAdapter() {

    private val mItemViewDelegateManager: ItemViewDelegateManager<T> = ItemViewDelegateManager()

    fun addItemViewDelegate(itemViewDelegate: ItemViewDelegate<T>): MultiItemTypeAdapter<*> {
        mItemViewDelegateManager.addDelegate(itemViewDelegate)
        return this
    }

    private fun useItemViewDelegateManager(): Boolean {
        return mItemViewDelegateManager.itemViewDelegateCount > 0
    }

    override fun getViewTypeCount(): Int {
        return if (useItemViewDelegateManager()) mItemViewDelegateManager.itemViewDelegateCount else super.getViewTypeCount()
    }

    override fun getItemViewType(position: Int): Int {
        return if (useItemViewDelegateManager()) {
            mItemViewDelegateManager.getItemViewType(mDatas[position], position)
        } else super.getItemViewType(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemViewDelegate =
            mItemViewDelegateManager.getItemViewDelegate(mDatas[position], position)
        val layoutId = itemViewDelegate.itemViewLayoutId
        val viewHolder: ViewHolder?
        if (convertView == null) {
            val itemView = LayoutInflater.from(mContext).inflate(layoutId, parent, false)
            viewHolder = ViewHolder(mContext, itemView, parent, position)
            viewHolder.layoutId = layoutId
            onViewHolderCreated(viewHolder, viewHolder.convertView)
        } else {
            viewHolder = convertView.tag as ViewHolder
            viewHolder.itemPosition = position
        }


        convert(viewHolder, getItem(position), position)
        return viewHolder.convertView
    }

    protected open fun convert(viewHolder: ViewHolder, item: T, position: Int) {
        mItemViewDelegateManager.convert(viewHolder, item, position)
    }

    protected open fun onViewHolderCreated(holder: ViewHolder, itemView: View) {}

    override fun getCount(): Int {
        return mDatas.size
    }

    override fun getItem(position: Int): T {
        return mDatas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}
