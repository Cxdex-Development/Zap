package de.theskyscout.zap.listener

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs


class SwipeListener(
    val context: Context
) : View.OnTouchListener {
    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    var gestureDetector: GestureDetector

    var onSwipeLeft: () -> Unit = {}
    var onSwipeRight: () -> Unit = {}
    var onSwipeUp: () -> Unit = {}
    var onSwipeDown: () -> Unit = {}

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val distanceX = e2.x - e1!!.x
                val distanceY = e2.y - e1.y
                if (abs(distanceX.toDouble()) > abs(distanceY.toDouble()) && abs(distanceX.toDouble()) > SWIPE_DISTANCE_THRESHOLD && abs(
                        velocityX.toDouble()
                    ) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (distanceX > 0) onSwipeRight() else onSwipeLeft()
                    return true
                } else if (abs(distanceY.toDouble()) > SWIPE_DISTANCE_THRESHOLD && abs(velocityY.toDouble()) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceY > 0) onSwipeDown() else onSwipeUp()
                    return true
                }
                return false
            }
        })
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false
        return gestureDetector.onTouchEvent(event)
    }
}