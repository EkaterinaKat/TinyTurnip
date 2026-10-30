package com.example.tinyturnip.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * Container that reports horizontal swipes over its whole area, including over clickable children.
 * A gesture counts as a swipe once it moves past the touch slop and is mostly horizontal
 * (same rule as KOM's SwipeManager); vertical gestures are left to the children, e.g. a ScrollView.
 */
class SwipeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    /** Finger moved from left to right. */
    var onSwipeRight: (() -> Unit)? = null

    /** Finger moved from right to left. */
    var onSwipeLeft: (() -> Unit)? = null

    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var swiping = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                swiping = false
            }
            MotionEvent.ACTION_MOVE -> swiping = isHorizontalSwipe(ev)
        }
        // Once intercepted, the rest of the gesture goes to onTouchEvent and children get ACTION_CANCEL,
        // so a swipe that started on the date doesn't also open the date picker.
        return swiping
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> if (!swiping) swiping = isHorizontalSwipe(ev)
            MotionEvent.ACTION_UP -> if (swiping) {
                if (ev.x > downX) onSwipeRight?.invoke() else onSwipeLeft?.invoke()
                swiping = false
            }
            MotionEvent.ACTION_CANCEL -> swiping = false
        }
        // Keep receiving the gesture even when it started over an area no child handles.
        return true
    }

    private fun isHorizontalSwipe(ev: MotionEvent): Boolean {
        val xDelta = abs(ev.x - downX)
        val yDelta = abs(ev.y - downY)
        return xDelta > slop && xDelta / 2 > yDelta
    }
}
