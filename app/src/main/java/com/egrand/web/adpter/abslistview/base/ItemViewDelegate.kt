package com.egrand.web.adpter.abslistview.base


import com.egrand.web.adpter.abslistview.ViewHolder

/**
 *
 */
interface ItemViewDelegate<T> {

    val itemViewLayoutId: Int

    fun isForViewType(item: T, position: Int): Boolean

    fun convert(holder: ViewHolder, item: T, position: Int)

}
