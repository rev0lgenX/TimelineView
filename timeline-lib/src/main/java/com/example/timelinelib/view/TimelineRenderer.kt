package com.example.timelinelib.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.dynamicanimation.animation.*
import com.example.timelinelib.R
import com.example.timelinelib.core.TimelineTracker
import com.example.timelinelib.core.TimelineWorker
import com.example.timelinelib.core.asset.TimelineAsset
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.TimelineAttrs
import com.example.timelinelib.listener.OnAssetVisibleListener
import com.example.timelinelib.listener.TickWorkerListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.absoluteValue

open class TimelineRenderer(context: Context, attributeSet: AttributeSet?, defStyle: Int = 0) :
    View(context, attributeSet, defStyle)
    , TickWorkerListener
    , OnAssetVisibleListener
    , GestureDetector.OnGestureListener
    , GestureDetector.OnDoubleTapListener
    , ScaleGestureDetector.OnScaleGestureListener {

    private val TAG = TimelineRenderer::class.java.simpleName

    var timelineAssetClickListener:TimelineAssetClickListener? = null

    private val timelineWorker = TimelineWorker(context, this, this){
        timelineAssetClickListener?.onAssetClick(it)
    }

    private val timelineTracker = TimelineTracker()
    private var timelineAttrs: TimelineAttrs? = null
        set(value) {
            field = value
            timelineTracker.attrs = value
        }


    var timelineEntry: TimelineEntry? = null
        set(value) {
            field = value
            if (value?.startTime?.dateTime == null) throw NullPointerException("TimelineEntry Null")

            timelineTracker.timelineEntry = value

            val textWidth = context.resources.getDimension(R.dimen.timelineTextWidth).toInt()

            value.timelineAssets?.forEach {

                if (it.eventStartDate != null) {
                    it.yearStartPosition = ChronoUnit.YEARS.between(
                        value.startTime?.dateTime, it.eventStartDate?.dateTime
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }

                    it.monthStartPosition = ChronoUnit.MONTHS.between(
                        value.startTime?.dateTime?.withDayOfMonth(1),
                        it.eventStartDate?.dateTime?.withDayOfMonth(1)
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }

                    it.dayStartPosition = ChronoUnit.DAYS.between(
                        value.startTime?.dateTime, it.eventStartDate?.dateTime
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }
                }

                if (it.eventEndDate != null) {

                    it.yearEndPosition = ChronoUnit.YEARS.between(
                        value.startTime?.dateTime, it.eventEndDate?.dateTime
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }
                    it.monthEndPosition = ChronoUnit.MONTHS.between(
                        value.startTime?.dateTime?.withDayOfMonth(1),
                        it.eventEndDate?.dateTime?.withDayOfMonth(1)
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }
                    it.dayEndPosition = ChronoUnit.DAYS.between(
                        value.startTime?.dateTime, it.eventEndDate?.dateTime
                    ).let { diff ->
                        diff.plus(if (diff != 0L) 1 else 0).times(timelineAttrs?.longTickDistance!!)
                            .toInt()
                    }
                }


                it.description?.let { str ->

                    TextPaint().apply {
                        textSize = timelineAttrs?.textSize!!
                        color = timelineAttrs?.timelineTextColor!!
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

            val indicatorWidth = context.resources.getDimension(R.dimen.indicatorWidth)

            value.timelineAssets?.forEachIndexed { index, asset ->
                val idx = index + 1
                if (idx < value.timelineAssets?.size!!) {
                    value.timelineAssets?.subList(idx, value.timelineAssets?.size!!)
                        ?.forEach { subasset ->
                            if (asset.eventEndDate != null) {
                                if (IntRange(
                                        asset.yearStartPosition!!,
                                        asset.yearEndPosition!!
                                    ).contains(subasset.yearStartPosition)
                                ) {
                                    subasset.paddingLeft += indicatorWidth.plus(5).toInt()
                                }

                                if (IntRange(asset.yearStartPosition!!, asset.yearStartPosition?.plus(asset.staticLayout?.height!!)!!)
                                        .contains(subasset.yearStartPosition)
                                ) {
                                    subasset.paddingTop += asset.staticLayout?.height?.plus(20)!!
                                }
                            } else {
                                if (asset.yearStartPosition == subasset.yearStartPosition) {
                                    subasset.paddingLeft += indicatorWidth.toInt()
                                    subasset.paddingTop += asset.staticLayout?.height!!
                                }
                            }
                        }

                }
            }

            invalidate()
        }

    private val offset = Point(0, 0)

    private var lastFocalPoint: Float? = null

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


    private var gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(context, this).apply {
            setOnDoubleTapListener(this@TimelineRenderer)
        }

    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    private lateinit var scaleAnimator: ValueAnimator


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        attributeSet?.let {
            context.theme.obtainStyledAttributes(it, R.styleable.TimelineRenderer, 0, 0).apply {
                timelineAttrs = TimelineAttrs(
                    shortTickSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineRenderer_shortTickSize,
                        context.resources.getDimension(R.dimen.shortTickSize).toInt()
                    ),
                    longTickSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineRenderer_longTickSize,
                        context.resources.getDimension(R.dimen.longTickSize).toInt()
                    ),
                    longTickDistance = 240,
//                    getDimensionPixelSize(
//                        R.styleable.TimelineStyle_longTickDistance,
//                        context.resources.getDimension(R.dimen.longTickDistance).toInt()
//                    ),
                    shortTickDistance = 60,
//                    getDimensionPixelSize(
//                        R.styleable.TimelineStyle_longTickDistance,
//                        context.resources.getDimension(R.dimen.longTickDistance).toInt()
//                    ).div(4),

                    timelineTextSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineRenderer_timelineTextSize,
                        context.resources.getDimension(R.dimen.gutterTextSize).toInt()
                    ),
                    textSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineRenderer_textSize,
                        context.resources.getDimension(R.dimen.textSize).toInt()
                    ).toFloat(),
                    gutterWidth =
                    getDimensionPixelSize(
                        R.styleable.TimelineRenderer_gutterWidth,
                        context.resources.getDimension(R.dimen.gutterWidth).toInt()
                    ),
                    gutterColor =
                    getColor(
                        R.styleable.TimelineRenderer_gutterColor,
                        ContextCompat.getColor(context, R.color.gutterColor)
                    ),
                    tickColor =
                    getColor(
                        R.styleable.TimelineRenderer_tickColor,
                        ContextCompat.getColor(context, R.color.tickColor)

                    ),
                    timelineTextColor =
                    getColor(
                        R.styleable.TimelineRenderer_timelineTextColor,
                        ContextCompat.getColor(context, R.color.timelineTextColor)

                    ),
                    indicatorColor = getColor(
                        R.styleable.TimelineRenderer_indicatorColor,
                        ContextCompat.getColor(context, R.color.timelineTextColor)
                    ),
                    indicatorTextSize = getDimension(
                        R.styleable.TimelineRenderer_indicatorTextSize,
                        context.resources.getDimension(R.dimen.indicatorTextSize)
                    ),
                    timelineTextBackgroundColor = getColor(
                        R.styleable.TimelineRenderer_timelineTextBackgroundColor,
                        TypedValue().apply {
                            context.theme.resolveAttribute(R.attr.colorAccent, this, true)
                        }.data
                    )
                )
                recycle()

            }
        }
    }




    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        timelineWorker.work(
            context,
            canvas,
            timelineAttrs!!,
            height,
            scaleFactor,
            springDisplacement,
            offset,
            timelineTracker
        )
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP) {
            isScaling = false
        }
        return true
    }



    override fun onAssetVisible(y: Double, asset: TimelineAsset) {

    }


    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        timelineWorker.onSingleTap(p0!!)
        return true
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
        return false
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
        val focalDiff =
            (lastArbitraryStart + lastFocalPoint?.div(currentScale)?.toFloat()!!) - focus

        timelineTracker.arbitraryStart =
            focus + (lastArbitraryStart - focus) / tempScale + focalDiff
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

    private val yAnimationUpdate =
        DynamicAnimation.OnAnimationUpdateListener { animation, newY, velocity ->
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

    private val ySpringAnimationEnd =
        DynamicAnimation.OnAnimationEndListener { animation, canceled, _, velocity ->
            if (!canceled && velocity.absoluteValue > 0) {
                startYAnimation(-velocity)
            }

            if (canceled || !animation.isRunning()) {
                springDisplacement = 0.0
                lastY = 0f
                displaced = 0.0
            }
        }


    private val yAnimationEnd =
        DynamicAnimation.OnAnimationEndListener { animation, canceled, _, velocity ->
            if (!canceled && velocity.absoluteValue > 0) {
                startYAnimation(-velocity)
            }

            if (canceled || !animation.isRunning()) {

                ySpring =
                    createSpringAnimation(scrollY.toFloat(), displaced.div(2).toFloat()).apply {
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
        yFling = createFlingAnimation(scrollY.toFloat(), vY).apply {
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