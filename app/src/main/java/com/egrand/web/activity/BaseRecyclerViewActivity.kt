package com.egrand.web.activity

import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.egrand.web.adpter.recyclerview.CommonAdapter
import com.egrand.web.constant.Constants
import com.egrand.web.view.SpacingDecoration
import com.scwang.smartrefresh.layout.internal.ProgressDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*

abstract class BaseRecyclerViewActivity<T> : BaseAppCompatActivity() {

    protected open var data: MutableList<T> = ArrayList()
    protected open var mPage = 0
    protected open lateinit var adapter: CommonAdapter<T>
    private lateinit var mProgressDrawable: ProgressDrawable

    protected abstract fun getAdapter(data: MutableList<T>, manager: FragmentManager): CommonAdapter<T>

    protected abstract fun loadPage(dir: Int)

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        mProgressDrawable = ProgressDrawable()
        mProgressDrawable.setColor(-0x99999a)
        ivLoadingIcon.animate().interpolator = LinearInterpolator()
        ivLoadingIcon.setImageDrawable(mProgressDrawable)
        mProgressDrawable.start()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        loadingView.visibility = View.VISIBLE
        mPage = 0
        data.clear()

        recyclerView.layoutManager = this.getLayoutManager()
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        if (this.useItemDecoration()) {
            recyclerView.addItemDecoration(this.getItemDecoration())
        }
        adapter = getAdapter(data, supportFragmentManager)
        recyclerView.adapter = adapter

        refreshLayout.setEnableAutoLoadMore(true)//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener {
            refreshLayout.finishLoadMore(false)
            mPage = 0
            data.clear()
            loadPage(Constants.DOWN)
        }
        refreshLayout.setOnLoadMoreListener { loadPage(Constants.UP) }
        loadPage(Constants.DOWN)
    }

    protected open fun useItemDecoration(): Boolean = true

    protected open fun getItemDecoration(): RecyclerView.ItemDecoration = SpacingDecoration(10, 10, true)

    protected open fun getLayoutManager(): RecyclerView.LayoutManager = GridLayoutManager(this, 2)

    protected open fun onLoadDataFinish(data: MutableList<T>, dir: Int, totalPages: Int) {
        loadingView.visibility = View.GONE
        mPage++
        this.data.addAll(data)
        if (dir == Constants.DOWN) {
            refreshLayout.finishRefresh()
        }
        if (dir == Constants.UP) {
            refreshLayout.finishLoadMore()
        }

        emptyView.visibility = if (this.data.size == 0) View.VISIBLE else View.GONE

        if (mPage >= totalPages) {
            refreshLayout.finishLoadMore(true)
        }
        adapter.notifyDataSetChanged()
    }

    protected open fun onLoadDataError(dir: Int, throwable: Throwable) {
        loadingView.visibility = View.GONE
        if (dir == Constants.DOWN) {
            refreshLayout.finishRefresh()
        }
        if (dir == Constants.UP) {
            refreshLayout.finishLoadMore()
        }
    }

    override fun onDestroy() {
        mProgressDrawable.stop()
        super.onDestroy()
    }
}
