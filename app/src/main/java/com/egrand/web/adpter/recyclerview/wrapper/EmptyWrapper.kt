package com.egrand.web.adpter.recyclerview.wrapper

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egrand.web.adpter.recyclerview.ViewHolder
import com.egrand.web.adpter.recyclerview.utils.WrapperUtils

/**
 *
 */
class EmptyWrapper<T>(private val mInnerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var mEmptyView: View
    private var mEmptyLayoutId: Int = 0
    private val isEmpty: Boolean = mInnerAdapter.itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (isEmpty) {
            return ViewHolder.createViewHolder(parent.context, mEmptyView)
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        WrapperUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, object : WrapperUtils.SpanSizeCallback {
            override fun getSpanSize(layoutManager: GridLayoutManager, oldLookup: GridLayoutManager.SpanSizeLookup, position: Int): Int {
                return if (isEmpty) {
                    layoutManager.spanCount
                } else {
                    oldLookup.getSpanSize(position)
                }
            }
        })
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        mInnerAdapter.onViewAttachedToWindow(holder)
        if (isEmpty) {
            WrapperUtils.setFullSpan(holder)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isEmpty) {
            ITEM_TYPE_EMPTY
        } else mInnerAdapter.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isEmpty) {
            return
        }
        mInnerAdapter.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return if (isEmpty) 1 else mInnerAdapter.itemCount
    }


    fun setEmptyView(emptyView: View) {
        mEmptyView = emptyView
    }

    fun setEmptyView(layoutId: Int) {
        mEmptyLayoutId = layoutId
    }

    companion object {
        const val ITEM_TYPE_EMPTY = Integer.MAX_VALUE - 1
    }

}
