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
import android.util.Log
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
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.TimelineAttrs
import com.example.timelinelib.listener.OnTimelineBehaviourListener
import com.example.timelinelib.listener.TickWorkerListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.absoluteValue

typealias TimelineEndListener = ((Boolean) -> Unit)?

class TimelineRenderer(context: Context, attributeSet: AttributeSet?, defStyle: Int = 0) :
    View(context, attributeSet, defStyle)
    , TickWorkerListener
    , OnTimelineBehaviourListener
    , GestureDetector.OnGestureListener
    , GestureDetector.OnDoubleTapListener
    , ScaleGestureDetector.OnScaleGestureListener {

    private val TAG = TimelineRenderer::class.java.simpleName

    var timelineAssetClickListener: TimelineAssetClickListener? = null
    var timelineTimelineBehaviourListener: OnTimelineBehaviourListener? = null
    var timelineEndListener: TimelineEndListener = null
    private var oldArbitraryStartValue = 0.0

    val assetAboveScreen: TimelineAsset?
        get() {
            return timelineWorker.assetAboveScreen
        }

    val assetBelowScreen: TimelineAsset?
        get() {
            return timelineWorker.assetBelowScreen
        }


    private val timelineWorker = TimelineWorker(context, this, this) {
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
            if (value?.startTime?.localDate == null) throw NullPointerException("TimelineEntry Null")

            timelineTracker.timelineEntry = value

            val textWidth = context.resources.getDimension(R.dimen.timelineTextWidth).toInt()
            value.timelineAssets?.forEach {
                if (it.eventStartDate != null) {
                    it.yearStartPosition = ChronoUnit.YEARS.between(
                        value.startTime!!.localDate.withMonth(1),
                        it.eventStartDate!!.localDate.withMonth(1)
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()

                    it.monthStartPosition = ChronoUnit.MONTHS.between(
                        value.startTime?.localDate?.withDayOfMonth(1),
                        it.eventStartDate?.localDate?.withDayOfMonth(1)
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()

                    it.dayStartPosition = ChronoUnit.DAYS.between(
                        value.startTime?.localDate, it.eventStartDate?.localDate
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()

                }

                if (it.eventEndDate != null) {

                    it.yearEndPosition = ChronoUnit.YEARS.between(
                        value.startTime!!.localDate.withMonth(1),
                        it.eventEndDate!!.localDate.withMonth(1)
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()

                    it.monthEndPosition = ChronoUnit.MONTHS.between(
                        value.startTime?.localDate?.withDayOfMonth(1),
                        it.eventEndDate?.localDate?.withDayOfMonth(1)
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()

                    it.dayEndPosition = ChronoUnit.DAYS.between(
                        value.startTime?.localDate, it.eventEndDate?.localDate
                    ).times(timelineAttrs?.longTickDistance!!)
                        .toInt()
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
            val indicatorRadius = context.resources.getDimension(R.dimen.indicatorRadius)
            val textPadding = context.resources.getDimension(R.dimen.textPadding)

            value.timelineAssets = value.timelineAssets?.sortedWith(compareBy { it.eventStartDate })

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
//                                    subasset.paddingLeft = indicatorWidth
//                                        .plus(asset.paddingLeft)
//                                        .plus(indicatorRadius.times(2))
//                                        .toInt()

                                    asset.childAssetsForPadding.add(subasset)
                                    asset.updateChildAssetPadding(indicatorWidth, indicatorRadius.times(2))
                                }

                                if (IntRange(
                                        asset.yearStartPosition!!,
                                        asset.yearStartPosition?.plus(asset.staticLayout?.height!!)?.plus(
                                            2 * textPadding
                                        )?.toInt()!!
                                    )
                                        .contains(subasset.yearStartPosition)
                                ) {
                                    subasset.paddingTop += asset.staticLayout?.height?.plus(2 * textPadding)?.plus(
                                        10
                                    )?.toInt()!!
                                }

                            } else {
                                if (subasset.eventEndDate != null) {
                                    if (IntRange(
                                            subasset.yearStartPosition!!,
                                            subasset.yearEndPosition!!
                                        )
                                            .contains(asset.yearStartPosition)
                                    ) {
//                                        asset.paddingLeft = indicatorWidth
//                                            .plus(subasset.paddingLeft)
//                                            .plus(indicatorRadius.times(2))
//                                            .toInt()

                                        subasset.childAssetsForPadding.add(asset)
                                        subasset.updateChildAssetPadding(indicatorWidth, indicatorRadius.times(2))

                                    }
                                }

                                if (asset.yearStartPosition == subasset.yearStartPosition) {
//                                    subasset.paddingLeft = indicatorWidth
//                                        .plus(asset.paddingLeft)
//                                        .plus(indicatorRadius.times(2))
//                                        .toInt()

                                    asset.childAssetsForPadding.add(subasset)
                                    asset.updateChildAssetPadding(indicatorWidth, indicatorRadius.times(2))
                                    subasset.paddingTop += asset.staticLayout?.height?.plus(2 * textPadding)?.toInt()!!
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
    private var isScrolling = false
    private var lastArbitraryStart = 0.0

    private var lastY = 0f
    private var displaced = 0.0
    private var springDisplacement = 0.0

    private var yFling: FlingAnimation? = null
    private var ySpring: SpringAnimation? = null
    private var scrollToAnimation: ValueAnimator? = null

    private var gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(context, this).apply {
            setOnDoubleTapListener(this@TimelineRenderer)
        }

    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    private lateinit var scaleAnimator: ValueAnimator


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        attributeSet?.let {
            context.theme.obtainStyledAttributes(it, R.styleable.TimelineView, 0, 0).apply {
                timelineAttrs = TimelineAttrs(
                    shortTickSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineView_shortTickSize,
                        context.resources.getDimension(R.dimen.shortTickSize).toInt()
                    ),
                    longTickSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineView_longTickSize,
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
                        R.styleable.TimelineView_timelineTextSize,
                        context.resources.getDimension(R.dimen.gutterTextSize).toInt()
                    ),
                    textSize =
                    getDimensionPixelSize(
                        R.styleable.TimelineView_textSize,
                        context.resources.getDimension(R.dimen.textSize).toInt()
                    ).toFloat(),
                    gutterWidth =
                    getDimensionPixelSize(
                        R.styleable.TimelineView_gutterWidth,
                        context.resources.getDimension(R.dimen.gutterWidth).toInt()
                    ),
                    gutterColor =
                    getColor(
                        R.styleable.TimelineView_gutterColor,
                        ContextCompat.getColor(context, R.color.gutterColor)
                    ),
                    tickColor =
                    getColor(
                        R.styleable.TimelineView_tickColor,
                        ContextCompat.getColor(context, R.color.tickColor)

                    ),
                    timelineTextColor =
                    getColor(
                        R.styleable.TimelineView_timelineTextColor,
                        ContextCompat.getColor(context, R.color.timelineTextColor)

                    ),
                    indicatorColor = getColor(
                        R.styleable.TimelineView_indicatorColor,
                        ContextCompat.getColor(context, R.color.timelineTextColor)
                    ),
                    indicatorTextSize = getDimension(
                        R.styleable.TimelineView_indicatorTextSize,
                        context.resources.getDimension(R.dimen.indicatorTextSize)
                    ),
                    timelineTextBackgroundColor = getColor(
                        R.styleable.TimelineView_timelineTextBackgroundColor,
                        TypedValue().apply {
                            context.theme.resolveAttribute(R.attr.colorAccent, this, true)
                        }.data
                    ),
                    textRectCorner = getDimension(
                        R.styleable.TimelineView_textRectCorner,
                        context.resources.getDimension(R.dimen.textRectCorner)
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
            isScrolling = false
            if (yFling == null)
                showAssetAssistant()
        }

        return true
    }


    fun isInMotion(): Boolean = yFling != null || isScaling

    override fun showAssetAssistant() {
        if (isScaling || yFling != null) {
            return
        }

        timelineTimelineBehaviourListener?.showAssetAssistant()
    }

    override fun hideAssetAssistant() {
        timelineTimelineBehaviourListener?.hideAssetAssistant()
    }


    override fun onAssetVisible(assetLocation: MutableMap<Int, TimelineAssetLocation>) {
        timelineTimelineBehaviourListener?.onAssetVisible(assetLocation)
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
        stopScrollToAnimation()
        lastArbitraryStart = timelineTracker.arbitraryStart
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, vX: Float, vY: Float): Boolean {
        hideAssetAssistant()
        startYAnimation(vY / 2)
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        if (isScaling) return true

        isScrolling = true
        hideAssetAssistant()

        val focalDiff = (p0?.y!! - p1?.y!!) / currentScale

        oldArbitraryStartValue = timelineTracker.arbitraryStart
        timelineTracker.arbitraryStart = lastArbitraryStart + focalDiff

        shouldStopTopToDownScroll()

        if (shouldStopDownToTopScroll()) {

            if (timelineTracker.arbitraryStart > oldArbitraryStartValue) {
                timelineTracker.arbitraryStart = oldArbitraryStartValue
                Log.d(TAG, "stopped")
            }
        }

        invalidate()
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }


    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        isScaling = true
        hideAssetAssistant()
        lastFocalPoint = p0?.focusY
        lastArbitraryStart = timelineTracker.arbitraryStart
        tempScale = p0?.scaleFactor?.toDouble() ?: 1.0
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        currentScale = scaleFactor
    }

    //pixel to number  divide by scale;
    //number to pixel multiply by scale;
    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        scaleFactor *= p0?.scaleFactor?.toDouble() ?: 1.0
        tempScale *= p0?.scaleFactor?.toDouble() ?: 1.0


        val focus = lastArbitraryStart + p0?.focusY!! / currentScale
        val focalDiff =
            (lastArbitraryStart + lastFocalPoint?.div(currentScale)?.toFloat()!!) - focus


        oldArbitraryStartValue = timelineTracker.arbitraryStart

        timelineTracker.arbitraryStart =
            focus + (lastArbitraryStart - focus) / tempScale + focalDiff

        timelineTracker.focalDistance = focus
        timelineTracker.focalPoint = p0.focusY.toDouble()

        shouldStopTopToDownScroll()

        if (shouldStopDownToTopScroll()) {
            if (timelineTracker.arbitraryStart > oldArbitraryStartValue) {
                timelineTracker.arbitraryStart = oldArbitraryStartValue
            }
        }

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
        if (!isInEditMode) {
            DynamicAnimation.OnAnimationUpdateListener { animation, newY, velocity ->
                displaced = (newY - lastY).toDouble()
                lastY = newY
                timelineTracker.arbitraryStart -= displaced

                if (shouldStopTopToDownScroll()) {
                    stopFlingAnimation()
                }

                if (shouldStopDownToTopScroll()) {
                    stopFlingAnimation()
                }

                invalidate()
            }
        } else null

    private val ySpringAnimationUpdate =
        if (!isInEditMode) {
            DynamicAnimation.OnAnimationUpdateListener { _, newY, _ ->
                springDisplacement = newY.toDouble()
                invalidate()
            }
        } else null

    private val ySpringAnimationEnd =
        if (!isInEditMode) {
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
        } else null


    private val yAnimationEnd =
        if (!isInEditMode) {
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
                    yFling = null
                    lastArbitraryStart = timelineTracker.arbitraryStart
                    showAssetAssistant()
                }
            }
        } else null

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

    private fun shouldStopTopToDownScroll() = if (timelineTracker.arbitraryStart <= 0.0) {
        timelineTracker.arbitraryStart = 0.0
        true
    } else false

    private fun shouldStopDownToTopScroll(): Boolean {
        val endReached = when (timelineTracker.timelineScaleType) {
            TimelineTracker.TimelineType.YEAR -> timelineTracker.arbitraryEnd >= timelineTracker.timeEndPositionYear!!
            TimelineTracker.TimelineType.MONTH -> timelineTracker.arbitraryEnd >= timelineTracker.timeEndPositionMonth!!
            TimelineTracker.TimelineType.DAY -> timelineTracker.arbitraryEnd >= timelineTracker.timeEndPositionDay!!
        }
        timelineEndListener?.invoke(endReached)
        return endReached
    }

    private fun stopScaleAnimation() {
        if (::scaleAnimator.isInitialized) scaleAnimator.cancel()
    }

    private fun stopFlingAnimation() {
        yFling?.cancel()
        yFling = null
    }

    private fun stopScrollToAnimation() {
        scrollToAnimation?.cancel()
        scrollToAnimation = null
    }

    private fun stopSpringAnimation() {
        if (ySpring?.canSkipToEnd() == true) ySpring?.skipToEnd()
        ySpring = null
    }


    fun scrollToTimeline(id: Int) {
        timelineEntry?.timelineAssets?.first { it.id == id }?.let { asset ->
            when (timelineTracker.timelineScaleType) {
                TimelineTracker.TimelineType.YEAR -> {
                    scrollToAnimation = ValueAnimator.ofInt(
                        0,
                        asset.yearStartTracker?.times(currentScale)?.minus(height / 2)?.toInt()!!
                    )
                }
                TimelineTracker.TimelineType.MONTH -> {
                    scrollToAnimation = ValueAnimator.ofInt(
                        0,
                        asset.monthStartTracker?.times(currentScale)?.minus(height / 2)?.toInt()!!
                    )
                }
                TimelineTracker.TimelineType.DAY -> {
                    scrollToAnimation = ValueAnimator.ofInt(
                        0,
                        asset.dayStartTracker?.times(currentScale)?.minus(height / 2)?.toInt()!!
                    )
                }

            }

            scrollToAnimation?.apply {
                addUpdateListener { animation ->
                    (animation.animatedValue as Int).let { value ->

                        timelineTracker.arbitraryStart =
                            lastArbitraryStart + value.div(currentScale)

                        if (timelineTracker.arbitraryStart <= 0) {
                            timelineTracker.arbitraryStart = 0.0
                        }

                        invalidate()
                    }
                }
                duration = 500
                start()
            }
        }
    }


}