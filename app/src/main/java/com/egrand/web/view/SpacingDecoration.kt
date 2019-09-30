package com.egrand.web.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpacingDecoration(hSpacing: Int, vSpacing: Int, setMargin: Boolean) : RecyclerView.ItemDecoration() {

    private var mHorizontalSpacing = 5
    private var mVerticalSpacing = 5
    private var isSetMargin = true

    init {
        isSetMargin = setMargin
        mHorizontalSpacing = hSpacing
        mVerticalSpacing = vSpacing
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val isSetMarginLeftAndRight = this.isSetMargin
        val bottomOffset = mVerticalSpacing
        var leftOffset = 0
        var rightOffset = 0

        val lp = view.layoutParams as RecyclerView.LayoutParams
        if (parent.layoutManager is GridLayoutManager) {
            val lm = parent.layoutManager as GridLayoutManager?
            val gridLp = lp as GridLayoutManager.LayoutParams

            if (gridLp.spanSize == lm!!.spanCount) {
                // Current item is occupied the whole row
                // We just need to care about margin left and right now
                if (isSetMarginLeftAndRight) {
                    leftOffset = mHorizontalSpacing
                    rightOffset = mHorizontalSpacing
                }
            } else {
                // Current item isn't occupied the whole row
                if (gridLp.spanIndex > 0) {
                    // Set space between channels in one row
                    leftOffset = mHorizontalSpacing
                } else if (gridLp.spanIndex == 0 && isSetMarginLeftAndRight) {
                    // Set left margin of a row
                    leftOffset = mHorizontalSpacing
                }
                if (gridLp.spanIndex == lm.spanCount - gridLp.spanSize && isSetMarginLeftAndRight) {
                    // Set right margin of a row
                    rightOffset = mHorizontalSpacing
                }
            }
        }
        outRect.set(leftOffset, 0, rightOffset, bottomOffset)
    }
}
