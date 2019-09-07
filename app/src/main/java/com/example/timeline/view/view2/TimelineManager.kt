package com.example.timeline.view.view2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import kotlin.math.absoluteValue

class TimelineManager(private val context: Context) : TickWorkerListener
    , GestureDetector.OnGestureListener
    , GestureDetector.OnDoubleTapListener
    , ScaleGestureDetector.OnScaleGestureListener {


    private var lastFocalPoint: Float? = null
    private val TAG = TimelineManager::class.java.simpleName

    private fun invalidate() = ViewCompat.postInvalidateOnAnimation(view)
    private val tickWorker = TickWorker(this)
    private val timelineTracker = TimelineTracker(TimelineEntry())
    private var scaleFactor = 1.0
    private var currentScale = 1.0
    private var tempScale = 1.0

    private var isScaling = false
    private var lastArbitraryStart = 0.0

    private var lastY = 0f
    private var displaced = 0.0

    private var yFling: FlingAnimation? = null


    private var gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, this).apply {
        setOnDoubleTapListener(this@TimelineManager)
    }
    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    private lateinit var scaleAnimator: ValueAnimator


    var attrs: TimelineAttrs? = null
        set(value) {
            timelineTracker.attrs = value
            field = value
        }

    lateinit var view: TimelineView

    fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP) {
            isScaling = false
        }
        return true
    }

    fun manage(canvas: Canvas?) {
        tickWorker.work(canvas, attrs!!, view.height, scaleFactor, Point(0, 0), timelineTracker)
    }

    fun submitEntry(entry: TimelineEntry) {
        timelineTracker.timelineEntry = entry
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(p0: MotionEvent?): Boolean {
        stopScaleAnimation()
        scaleAnimator = ValueAnimator.ofFloat(scaleFactor.toFloat(), (scaleFactor * 1.2).toFloat())
            .apply {
                addUpdateListener { animator ->
                    (animator.animatedValue as Float).let {
                        scaleFactor = it.toDouble()
                        tempScale = it.toDouble()

                        val focus = timelineTracker.arbitraryStart + p0?.y!! / currentScale

                        timelineTracker.arbitraryStart = focus - p0.y / tempScale
                        timelineTracker.focalDistance = focus
                        timelineTracker.focalPoint = p0.y.toDouble()

                        currentScale = scaleFactor
                        invalidate()
                    }
                }
                duration = 300
                start()
            }

        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {

        return false
    }

    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        stopFlingAnimations()
        lastArbitraryStart = timelineTracker.arbitraryStart
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, vX: Float, vY: Float): Boolean {
        startYAnimation(vY / 2)
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        if (isScaling) return true

        val focalDiff = (p0?.y!! - p1?.y!!) / currentScale

        timelineTracker.arbitraryStart = lastArbitraryStart + focalDiff

        if (timelineTracker.arbitraryStart <= 0) {
            timelineTracker.arbitraryStart = 0.0
        }

        invalidate()
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }


    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        isScaling = true
        lastFocalPoint = p0?.focusY
        lastArbitraryStart = timelineTracker.arbitraryStart
        tempScale = p0?.scaleFactor?.toDouble() ?: 1.0
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        currentScale = scaleFactor
    }

    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        scaleFactor *= p0?.scaleFactor?.toDouble() ?: 1.0
        tempScale *= p0?.scaleFactor?.toDouble() ?: 1.0


        val focus = lastArbitraryStart + p0?.focusY!! / currentScale
        val focalDiff = (lastArbitraryStart + lastFocalPoint?.div(currentScale)?.toFloat()!!) - focus

        timelineTracker.arbitraryStart = focus + (lastArbitraryStart - focus) / tempScale + focalDiff
        timelineTracker.focalDistance = timelineTracker.arbitraryStart + p0.focusY.div(tempScale)
        timelineTracker.focalPoint = p0.focusY.toDouble()

        stopTimeline()
        invalidate()
        return true
    }

    override fun onScaleReset() {
        stopScaleAnimation()
        scaleFactor = 1.0
        currentScale = 1.0
        tempScale = 1.0
        lastArbitraryStart = timelineTracker.arbitraryStart
    }


    override fun stopExpanding() {
        stopScaleAnimation()
        currentScale = 2.0
        scaleFactor = 2.0
    }

    override fun stopContracting() {
        currentScale = 0.5
        scaleFactor = 0.5
    }

    private val yAnimationUpdate = DynamicAnimation.OnAnimationUpdateListener { animation, newY, velocity ->
        displaced = (newY - lastY).toDouble()
        lastY = newY
        timelineTracker.arbitraryStart -= displaced
        if (stopTimeline()) stopFlingAnimations()
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

    private fun startYAnimation(vY: Float) {
        yFling = createAnimation(view.scrollY.toFloat(), vY).apply {
            addUpdateListener(yAnimationUpdate)
            addEndListener(yAnimationEnd)
            start()
        }
    }

    private fun createAnimation(
        startValue: Float,
        startVelocity: Float
    ): FlingAnimation {
        return FlingAnimation(FloatValueHolder(startValue))
            .setStartVelocity(startVelocity)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setFriction(0.7f)
    }

    private fun stopTimeline(): Boolean {
        if (timelineTracker.arbitraryStart <= 0.0) {
            timelineTracker.arbitraryStart = 0.0
            return true
        }
        return false
    }

    private fun stopScaleAnimation() {
        if (::scaleAnimator.isInitialized) scaleAnimator.cancel()
    }

    private fun stopFlingAnimations() {
        yFling?.cancel()
        yFling = null
    }

}