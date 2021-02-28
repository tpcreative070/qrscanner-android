package tpcreative.co.qrscanner.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.MotionEventCompat
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {
    var mStartDragX = 0f
    var mOnSwipeOutListener: OnSwipeOutListener? = null

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    fun setOnSwipeOutListener(listener: OnSwipeOutListener?) {
        mOnSwipeOutListener = listener
    }

    private fun onSwipeOutAtStart() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener?.onSwipeOutAtStart()
        }
    }

    private fun onSwipeMove() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener?.onSwipeMove()
        }
    }

    private fun onSwipeOutAtEnd() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener?.onSwipeOutAtEnd()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action?.and(MotionEventCompat.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> mStartDragX = ev.x
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (currentItem == 0 || currentItem == (adapter?.count ?:0) - 1) {
            val action = ev?.action
            val x = ev?.x
            if (action != null) {
                when (action and MotionEventCompat.ACTION_MASK) {
                    MotionEvent.ACTION_MOVE -> onSwipeMove()
                    MotionEvent.ACTION_UP -> {
                        if (currentItem == 0 && (x ?:0F) > mStartDragX) {
                            onSwipeOutAtStart()
                        }
                        if (currentItem == (adapter?.count ?:0) - 1 && (x ?:0F) < mStartDragX) {
                            onSwipeOutAtEnd()
                        }
                    }
                }
            }
        } else {
            mStartDragX = 0f
        }
        return super.onTouchEvent(ev)
    }

    interface OnSwipeOutListener {
        fun onSwipeOutAtStart()
        fun onSwipeOutAtEnd()
        fun onSwipeMove()
    }
}