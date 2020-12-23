package tpcreative.co.qrscanner.common.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class DividerItemDecoration(context: Context?, orientation: Int) : ItemDecoration() {
    private var mDivider: Drawable? = null
    private var mOrientation = 0
    private var mMarginTop = 0
    private var mMarginBottom = 0
    private var mMarginLeft = 0
    private var mMarginRight = 0
    fun setDrawable(mResource: Drawable?) {
        mDivider = mResource
    }

    fun getDivider(): Drawable? {
        return mDivider
    }

    fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)) { "invalid orientation" }
        mOrientation = orientation
    }

    override fun onDraw(canvas: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        val dividerLeft = parent.getPaddingLeft()
        val dividerRight = parent.getWidth() - parent.getPaddingRight()
        val childCount = parent.getChildCount()
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + mDivider.getIntrinsicHeight()
            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            mDivider.draw(canvas)
        }
    }

    fun drawVertical(c: Canvas?, parent: RecyclerView?) {
        val left = parent.getPaddingLeft()
        val right = parent.getWidth() - parent.getPaddingRight()
        val childCount = parent.getChildCount()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.getIntrinsicHeight()
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    fun drawHorizontal(c: Canvas?, parent: RecyclerView?) {
        val top = parent.getPaddingTop()
        val bottom = parent.getHeight() - parent.getPaddingBottom()
        val childCount = parent.getChildCount()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + mDivider.getIntrinsicHeight()
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight())
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0)
        }
    }

    fun setMarginTop(marginTop: Int) {
        mMarginTop = marginTop
    }

    fun getMarginTop(): Int {
        return mMarginTop
    }

    fun setMarginBottom(marginBottom: Int) {
        mMarginBottom = marginBottom
    }

    fun getMarginBottom(): Int {
        return mMarginBottom
    }

    fun setMarginLeft(mMarginLeft: Int) {
        this.mMarginLeft = mMarginLeft
    }

    fun getMarginLeft(): Int {
        return mMarginLeft
    }

    fun setMarginRight(marginRight: Int) {
        mMarginRight = marginRight
    }

    fun getMarginRight(): Int {
        return mMarginRight
    }

    companion object {
        private val ATTRS: IntArray? = intArrayOf(
                android.R.attr.listDivider
        )
        val TAG = DividerItemDecoration::class.java.simpleName
        const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        if (mDivider == null) {
            mDivider = a.getDrawable(0)
        }
        a.recycle()
        setOrientation(orientation)
    }
}