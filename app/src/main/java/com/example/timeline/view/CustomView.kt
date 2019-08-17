package com.example.timeline.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import kotlin.math.absoluteValue


class CustomView(context: Context, attr: AttributeSet) : View(context, attr)
    , GestureDetector.OnGestureListener
    , ScaleGestureDetector.OnScaleGestureListener {


    private val TAG = CustomView::class.java.simpleName


    private var isScaling = false

    private var ticks = Ticks()
    private val point = Point(0, 0)
    private val rect = Rect(20, 20, 40, 40)
    private val paint = Paint()

    private var arbitraryStart = 2000.0
    private var arbitraryEnd = 4000.0

    private var scaleFactor = 1.0
    private var ticksScale = 1.0

    private var lastArbitraryStart = 0.0
    private var lastArbitraryEnd = 0.0
    private var displaced = 0.0
    private var lastY = 0f

    private var gestureDetector: GestureDetectorCompat
    private var scaleGestureDetector: ScaleGestureDetector

    private var mScroller: Scroller

    private var yFling: FlingAnimation? = null

    init {
        paint.color = Color.CYAN
        mScroller = Scroller(context)
        gestureDetector = GestureDetectorCompat(context, this)
        scaleGestureDetector = ScaleGestureDetector(context, this)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        ticksScale = height / (arbitraryEnd - arbitraryStart)

        ticks.onDraw(context, canvas!!, point, -arbitraryStart * ticksScale, ticksScale, height)


        canvas.drawRect(rect, paint)
    }


    private fun createAnimation(
        startValue: Float,
        startVelocity: Float,
        maxValue: Float,
        minValue: Float
    ): FlingAnimation {
        return FlingAnimation(FloatValueHolder(startValue))
            .setStartVelocity(startVelocity)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setFriction(0.7f)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {

    }


    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    private fun stopAnimations() {
        yFling?.cancel()
        yFling = null
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        stopAnimations()

        lastArbitraryStart = arbitraryStart
        lastArbitraryEnd = arbitraryEnd
        return true
    }

    private val yAnimationUpdate = DynamicAnimation.OnAnimationUpdateListener { animation, newY, velocity ->
        displaced = (newY - lastY).toDouble()

        lastY = newY

        arbitraryStart -= displaced
        arbitraryEnd -= displaced

        Log.d(TAG, "velocity $velocity displaced: $displaced newY $newY")
        rect.top += displaced.toInt()
        invalidate()
    }

    private val yAnimationEnd = DynamicAnimation.OnAnimationEndListener { animation, canceled, _, velocity ->
        if (!canceled && velocity.absoluteValue > 0) {
            startYAnimation(-velocity)
        }
        if (canceled || !animation.isRunning()) {
            lastY = 0f
            displaced = 0.0
        }
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, vX: Float, vY: Float): Boolean {
        startYAnimation(vY/2)
        return true
    }

    private fun startYAnimation(vY: Float) {
        yFling = createAnimation(scrollY.toFloat(), vY, height.toFloat(), -height.toFloat()).apply {
            addUpdateListener(yAnimationUpdate)
            addEndListener(yAnimationEnd)
            start()
        }
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {

        if (isScaling) return false

        val scale = (lastArbitraryEnd - lastArbitraryStart) / height

        val focus = lastArbitraryStart + p1?.y?.times(scale)!!

        val focalDiff = (lastArbitraryStart + p0?.y!! * scale) - focus

        arbitraryStart = (lastArbitraryStart) + focalDiff
        arbitraryEnd = (lastArbitraryEnd) + focalDiff

        invalidate()

        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }


    //scaling
    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        isScaling = true
        scaleFactor = p0?.scaleFactor?.toDouble() ?: 1.0
        lastArbitraryStart = arbitraryStart
        lastArbitraryEnd = arbitraryEnd
        return true
    }


    override fun onScale(p0: ScaleGestureDetector?): Boolean {

        scaleFactor *= p0?.scaleFactor?.toDouble() ?: 1.0

        val scale = (lastArbitraryEnd - lastArbitraryStart) / height

        val focus = lastArbitraryStart + p0?.focusY?.times(scale)!!

        val focalDiff = (lastArbitraryStart + p0.focusY * scale) - focus

        arbitraryStart = focus + (lastArbitraryStart - focus) / scaleFactor + focalDiff
        arbitraryEnd = focus + (lastArbitraryEnd - focus) / scaleFactor + focalDiff

        invalidate()

        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        lastArbitraryStart = arbitraryStart
        lastArbitraryEnd = arbitraryEnd
        isScaling = false
    }


}