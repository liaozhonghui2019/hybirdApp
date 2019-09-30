package com.egrand.web.adpter.recyclerview.wrapper

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.egrand.web.adpter.recyclerview.ViewHolder
import com.egrand.web.adpter.recyclerview.utils.WrapperUtils

/**
 *
 */
class LoadMoreWrapper<T>(private val mInnerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mLoadMoreView: View? = null
    private var mLoadMoreLayoutId: Int = 0
    private var mOnLoadMoreListener: OnLoadMoreListener? = null

    private fun hasLoadMore(): Boolean {
        return mLoadMoreView != null || mLoadMoreLayoutId != 0
    }

    private fun isShowLoadMore(position: Int): Boolean {
        return hasLoadMore() && position >= mInnerAdapter.itemCount
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowLoadMore(position)) {
            ITEM_TYPE_LOAD_MORE
        } else mInnerAdapter.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_LOAD_MORE) {
            return if (mLoadMoreView != null) {
                ViewHolder.createViewHolder(parent.context, mLoadMoreView!!)
            } else {
                ViewHolder.createViewHolder(parent.context, parent, mLoadMoreLayoutId)
            }
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isShowLoadMore(position)) {
            if (mOnLoadMoreListener != null) {
                mOnLoadMoreListener!!.onLoadMoreRequested()
            }
            return
        }
        mInnerAdapter.onBindViewHolder(holder, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        WrapperUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, object : WrapperUtils.SpanSizeCallback {
            override fun getSpanSize(layoutManager: GridLayoutManager, oldLookup: GridLayoutManager.SpanSizeLookup, position: Int): Int {
                return if (isShowLoadMore(position)) {
                    layoutManager.spanCount
                } else oldLookup.getSpanSize(position)
            }
        })
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        mInnerAdapter.onViewAttachedToWindow(holder)

        if (isShowLoadMore(holder.layoutPosition)) {
            setFullSpan(holder)
        }
    }

    private fun setFullSpan(holder: RecyclerView.ViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
    }

    override fun getItemCount(): Int {
        return mInnerAdapter.itemCount + if (hasLoadMore()) 1 else 0
    }

    fun setOnLoadMoreListener(loadMoreListener: OnLoadMoreListener?): LoadMoreWrapper<*> {
        if (loadMoreListener != null) {
            mOnLoadMoreListener = loadMoreListener
        }
        return this
    }

    fun setLoadMoreView(loadMoreView: View): LoadMoreWrapper<*> {
        mLoadMoreView = loadMoreView
        return this
    }

    fun setLoadMoreView(layoutId: Int): LoadMoreWrapper<*> {
        mLoadMoreLayoutId = layoutId
        return this
    }

    interface OnLoadMoreListener {
        fun onLoadMoreRequested()
    }

    companion object {
        const val ITEM_TYPE_LOAD_MORE = Integer.MAX_VALUE - 2
    }
}
