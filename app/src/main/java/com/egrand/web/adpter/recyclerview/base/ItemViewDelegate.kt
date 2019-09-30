package com.egrand.web.adpter.recyclerview.base

import com.egrand.web.adpter.recyclerview.ViewHolder

/**
 *
 */
interface ItemViewDelegate<T> {

    val itemViewLayoutId: Int

    fun isForViewType(item: T, position: Int): Boolean

    fun convert(holder: ViewHolder, item: T, position: Int)

}
