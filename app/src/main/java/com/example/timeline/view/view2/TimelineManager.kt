package com.example.timeline.view.view2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.*
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.absoluteValue

class TimelineManager(private val context: Context) :
    TickWorkerListener,
    OnAssetVisibleListener
    , GestureDetector.OnGestureListener
    , GestureDetector.OnDoubleTapListener
    , ScaleGestureDetector.OnScaleGestureListener {


    private val TAG = TimelineManager::class.java.simpleName

    private var lastFocalPoint: Float? = null
    private fun invalidate() {
        if (::view.isInitialized) ViewCompat.postInvalidateOnAnimation(view)
    }

    private val timelineWorker = TimelineWorker(context, this, this)
    private val timelineTracker = TimelineTracker()

    var timelineEntry: TimelineEntry? = null


    private var scaleFactor = 1.0
    private var currentScale = 1.0
    private var tempScale = 1.0


    private var isScaling = false
    private var lastArbitraryStart = 0.0

    private var lastY = 0f
    private var displaced = 0.0
    private var springDisplacement = 0.0

    private var yFling: FlingAnimation? = null
    private var ySpring: SpringAnimation? = null


    private var gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, this).apply {
        setOnDoubleTapListener(this@TimelineManager)
    }

    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    private lateinit var scaleAnimator: ValueAnimator

    var attrs: TimelineAttrs? = null
        set(setVal) {
            timelineTracker.attrs = setVal
            field = setVal

            timelineEntry?.let { value ->
                if (value.startTime?.dateTime == null) throw NullPointerException("TimelineEntry Null")
                timelineTracker.timelineEntry = value


                val textWidth = TimelineAttrs.convertDpToPixel(200f, context).toInt()


                value.timelineAssets?.forEach {
                    if (it.eventStartDate != null) {
                        it.yearStartPosition = ChronoUnit.YEARS.between(
                            value.startTime?.dateTime, it.eventStartDate?.dateTime
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }

                        it.monthStartPosition = ChronoUnit.MONTHS.between(
                            value.startTime?.dateTime?.withDayOfMonth(1), it.eventStartDate?.dateTime?.withDayOfMonth(1)
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }
                        it.dayStartPosition = ChronoUnit.DAYS.between(
                            value.startTime?.dateTime, it.eventStartDate?.dateTime
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }
                    }

                    if (it.eventEndDate != null) {

                        it.yearEndPosition = ChronoUnit.YEARS.between(
                            value.startTime?.dateTime, it.eventEndDate?.dateTime
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }
                        it.monthEndPosition = ChronoUnit.MONTHS.between(
                            value.startTime?.dateTime?.withDayOfMonth(1), it.eventEndDate?.dateTime?.withDayOfMonth(1)
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }
                        it.dayEndPosition = ChronoUnit.DAYS.between(
                            value.startTime?.dateTime, it.eventEndDate?.dateTime
                        ).let { diff ->
                            diff.plus(if (diff != 0L) 1 else 0) * attrs?.longTickDistance!!.toDouble()
                        }
                    }

                    it.description?.let { str ->

                        TextPaint().apply {
                            textSize = attrs?.textSize!!
                            color = attrs?.timelineTextColor!!
                            val width = measureText(str)

                            it.staticLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                StaticLayout(
                                    str,
                                    this,
                                    if (width <= textWidth) width.toInt() else textWidth,
                                    Layout.Alignment.ALIGN_NORMAL,
                                    1.0f,
                                    0f,
                                    false
                                )
                            } else {
                                StaticLayout.Builder.obtain(
                                    str,
                                    0,
                                    str.lastIndex + 1,
                                    this,
                                    if (width <= textWidth) width.toInt() else textWidth
                                ).build()
                            }
                        }


                    }
                }

            }
            invalidate()

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

    override fun onAssetVisible(y: Double, asset: TimelineAsset) {

    }

    fun manage(canvas: Canvas?) {
        timelineWorker.work(
            context,
            canvas,
            attrs!!,
            view.height,
            scaleFactor,
            springDisplacement,
            Point(0, 0),
            timelineTracker
        )
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
        stopFlingAnimation()
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

        timelineTracker.focalDistance = focus

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
        if (stopTimeline()) stopFlingAnimation()

        invalidate()
    }

    private val ySpringAnimationUpdate = DynamicAnimation.OnAnimationUpdateListener { _, newY, _ ->
        springDisplacement = newY.toDouble()
        invalidate()
    }

    private val ySpringAnimationEnd = DynamicAnimation.OnAnimationEndListener { animation, canceled, _, velocity ->
        if (!canceled && velocity.absoluteValue > 0) {
            startYAnimation(-velocity)
        }

        if (canceled || !animation.isRunning()) {
            springDisplacement = 0.0
            lastY = 0f
            displaced = 0.0
        }
    }


    private val yAnimationEnd = DynamicAnimation.OnAnimationEndListener { animation, canceled, _, velocity ->
        if (!canceled && velocity.absoluteValue > 0) {
            startYAnimation(-velocity)
        }

        if (canceled || !animation.isRunning()) {

            ySpring = createSpringAnimation(view.scrollY.toFloat(), displaced.div(2).toFloat()).apply {
                addUpdateListener(ySpringAnimationUpdate)
                addEndListener(ySpringAnimationEnd)
                start()
            }

            lastY = 0f
            displaced = 0.0
        }
    }

    private fun startYAnimation(vY: Float) {
        stopSpringAnimation()
        yFling = createFlingAnimation(view.scrollY.toFloat(), vY).apply {
            addUpdateListener(yAnimationUpdate)
            addEndListener(yAnimationEnd)
            start()
        }
    }

    private fun createFlingAnimation(
        startValue: Float,
        startVelocity: Float
    ): FlingAnimation {
        return FlingAnimation(FloatValueHolder(startValue))
            .setStartVelocity(startVelocity)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setFriction(0.7f)
    }

    private fun createSpringAnimation(startValue: Float, diff: Float): SpringAnimation {
        return SpringAnimation(FloatValueHolder(diff - startValue)).apply {
            spring = SpringForce()
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.finalPosition = startValue
        }
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

    private fun stopFlingAnimation() {
        yFling?.cancel()
        yFling = null
    }

    private fun stopSpringAnimation() {
        if (ySpring?.canSkipToEnd() == true) ySpring?.skipToEnd()
        ySpring = null
    }

}