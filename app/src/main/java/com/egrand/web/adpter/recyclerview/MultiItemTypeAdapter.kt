package com.egrand.web.adpter.recyclerview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.egrand.web.adpter.recyclerview.base.ItemViewDelegate
import com.egrand.web.adpter.recyclerview.base.ItemViewDelegateManager

/**
 *
 */
open class MultiItemTypeAdapter<T>(protected open var mContext: Context, var datas: List<T>) : RecyclerView.Adapter<ViewHolder>() {

    protected open var mItemViewDelegateManager: ItemViewDelegateManager<T> = ItemViewDelegateManager()
    protected open var mOnItemClickListener: OnItemClickListener? = null

    override fun getItemViewType(position: Int): Int {
        return if (!useItemViewDelegateManager()) super.getItemViewType(position) else mItemViewDelegateManager.getItemViewType(datas[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType)
        val layoutId = itemViewDelegate!!.itemViewLayoutId
        val holder = ViewHolder.createViewHolder(mContext, parent, layoutId)
        onViewHolderCreated(holder, holder.convertView)
        setListener(parent, holder, viewType)
        return holder
    }

    protected open fun onViewHolderCreated(holder: ViewHolder, itemView: View) {

    }

    fun convert(holder: ViewHolder, t: T) {
        mItemViewDelegateManager.convert(holder, t, holder.adapterPosition)
    }

    protected open fun isEnabled(viewType: Int): Boolean {
        return true
    }


    protected open fun setListener(parent: ViewGroup, viewHolder: ViewHolder, viewType: Int) {
        if (!isEnabled(viewType)) return
        viewHolder.convertView.setOnClickListener { v ->
            if (mOnItemClickListener != null) {
                val position = viewHolder.adapterPosition
                mOnItemClickListener!!.onItemClick(v, viewHolder, position)
            }
        }

        viewHolder.convertView.setOnLongClickListener(View.OnLongClickListener { v ->
            if (mOnItemClickListener != null) {
                val position = viewHolder.adapterPosition
                return@OnLongClickListener mOnItemClickListener!!.onItemLongClick(v, viewHolder, position)
            }
            false
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        convert(holder, datas[position])
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun addItemViewDelegate(itemViewDelegate: ItemViewDelegate<T>): MultiItemTypeAdapter<*> {
        mItemViewDelegateManager.addDelegate(itemViewDelegate)
        return this
    }

    fun addItemViewDelegate(viewType: Int, itemViewDelegate: ItemViewDelegate<T>): MultiItemTypeAdapter<*> {
        mItemViewDelegateManager.addDelegate(viewType, itemViewDelegate)
        return this
    }

    protected open fun useItemViewDelegateManager(): Boolean {
        return mItemViewDelegateManager.itemViewDelegateCount > 0
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int)

        fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean
    }
}
