package com.example.timeline.view.view2

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import android.util.DisplayMetrics


class TimelineWorker(context: Context?, tListener: TickWorkerListener?, aListener: OnAssetVisibleListener?) {

    private val paint = Paint()
    private val textPaint = TextPaint()
    private val rectF = RectF()
    private val assetIndicatorWidth = 8f


    constructor() : this(null, null, null)

    private val TAG = TimelineWorker::class.java.simpleName

    private val tickWorkerListener: TickWorkerListener? = tListener
    private val assetVisibleListener: OnAssetVisibleListener? = aListener

    private val indicatorHeight = TimelineAttrs.convertDpToPixel(6f, context!!)


    fun work(
        context: Context,
        canvas: Canvas?,
        attrs: TimelineAttrs,
        height: Int,
        scale: Double,
        springDisplacement: Double,
        offset: Point,
        tracker: TimelineTracker
    ) {
        var currentScale = scale

        var smallScaleTickDistance = attrs.shortTickDistance * currentScale

        if (smallScaleTickDistance >= 2 * attrs.shortTickDistance) {
            if (tracker.timelineScaleType == TimelineTracker.TimelineType.DAY) {
                currentScale = 2.0
                smallScaleTickDistance = (2.0 * attrs.shortTickDistance)
                tickWorkerListener?.stopExpanding()
            } else {
                tracker.expandTimelineType()
                tickWorkerListener?.onScaleReset()
            }
        } else if (smallScaleTickDistance < 0.5 * attrs.shortTickDistance) {
            if (tracker.timelineScaleType == TimelineTracker.TimelineType.YEAR) {
                currentScale = 0.5
                smallScaleTickDistance = (0.5 * attrs.shortTickDistance)
                tickWorkerListener?.stopContracting()
            } else {
                tracker.collapseTimelineType()
                tickWorkerListener?.onScaleReset()
            }
        }

        val numTicks = (ceil(height / smallScaleTickDistance) + 2).toInt()

        val gutterWidth = attrs.gutterWidth
        val assetIndicatorLeft = gutterWidth + TimelineAttrs.convertDpToPixel(8f, context)
        val assetIndicatorRight = assetIndicatorLeft + TimelineAttrs.convertDpToPixel(assetIndicatorWidth, context)

        canvas?.drawRect(
            Rect(offset.x, offset.y, gutterWidth, height),
            Paint().apply {
                color = attrs.gutterColor
            })

        var tickOffset: Double
        var startingTickMarkValue: Double


        val y = tracker.arbitraryStart
        val yBottom = tracker.arbitraryStart + height / scale


        val dateTimeHintLeftPadding =
            5 * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT) + gutterWidth

        startingTickMarkValue = y - (y % attrs.shortTickDistance)

        tickOffset = -((y % attrs.shortTickDistance) * currentScale) - smallScaleTickDistance + 30

        var longTickCount = 0

        tracker.timelineEntry?.timelineAssets?.forEach {
            paint.reset()
            when (tracker.timelineScaleType) {
                TimelineTracker.TimelineType.YEAR -> {
                    it.yearStartTracker = it.yearStartPosition?.minus(startingTickMarkValue)
                    it.yearEndTracker = it.yearEndPosition?.minus(startingTickMarkValue)
                    indicator(
                        it.yearStartTracker,
                        it.yearEndTracker,
                        scale,
                        tickOffset,
                        context,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        it.backgroundColor,
                        smallScaleTickDistance.toFloat()
                    )

                }
                TimelineTracker.TimelineType.MONTH -> {
                    it.monthStartTracker = it.monthStartPosition?.minus(startingTickMarkValue)
                    it.monthEndTracker = it.monthEndPosition?.minus(startingTickMarkValue)
                    indicator(
                        it.monthStartTracker,
                        it.monthEndTracker,
                        scale,
                        tickOffset,
                        context,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        it.backgroundColor,
                        smallScaleTickDistance.toFloat()
                    )
                }
                TimelineTracker.TimelineType.DAY -> {
                    it.dayStartTracker = it.dayStartPosition?.minus(startingTickMarkValue)
                    it.dayEndTracker = it.dayEndPosition?.minus(startingTickMarkValue)
                    indicator(
                        it.dayStartTracker,
                        it.dayEndTracker,
                        scale,
                        tickOffset,
                        context,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        it.backgroundColor,
                        smallScaleTickDistance.toFloat()
                    )
                }
            }

        }


        for (i in 0 until numTicks) {

            tickOffset += smallScaleTickDistance

            var tt = round(startingTickMarkValue)
            tt = -tt

            val o = floor(tickOffset)

            if ((tt % attrs.longTickDistance).toInt() == 0) {
                longTickCount++
                drawTick(canvas, attrs.longTickSize, 2, attrs.tickColor, gutterWidth, o)
                drawLongTickText(tracker, tt, attrs, canvas, offset, o)

                tracker.timelineEntry?.timelineAssets?.forEach {
                    when (tracker.timelineScaleType) {
                        TimelineTracker.TimelineType.YEAR -> {
                            if (it.yearStartPosition == abs(tt)) {
                                drawTextAsset(assetIndicatorRight, context, it, o, springDisplacement, canvas, attrs)
                            }
                        }
                        TimelineTracker.TimelineType.MONTH -> {
                            if (it.monthStartPosition == abs(tt)) {
                                drawTextAsset(assetIndicatorRight, context, it, o, springDisplacement, canvas, attrs)
                            }
                        }
                        TimelineTracker.TimelineType.DAY -> {
                            if (it.dayStartPosition == abs(tt)) {
                                drawTextAsset(assetIndicatorRight, context, it, o, springDisplacement, canvas, attrs)
                            }
                        }
                    }
                }
            } else {
                drawTick(canvas, attrs.shortTickSize, 1, attrs.tickColor, gutterWidth, o)
            }

            startingTickMarkValue += attrs.shortTickDistance
        }


        when {
            tracker.timelineScaleType == TimelineTracker.TimelineType.MONTH -> {
                val topTime = tracker.getTime(y)
                val bottomTime = tracker.getTime(yBottom)
                drawTopBottomText(canvas, 0, dateTimeHintLeftPadding, topTime.year(), context)
                drawTopBottomText(canvas, height, dateTimeHintLeftPadding, bottomTime.year(), context)
            }

            tracker.timelineScaleType == TimelineTracker.TimelineType.DAY -> {

                val topTime = tracker.getTime(y)
                val bottomTime = tracker.getTime(yBottom)

                val topText = "${topTime.year()} ${topTime.month()}"
                val bottomText = "${bottomTime.year()} ${bottomTime.month()}"

                drawTopBottomText(canvas, 0, dateTimeHintLeftPadding, topText, context)
                drawTopBottomText(canvas, height, dateTimeHintLeftPadding, bottomText, context)
            }
        }


    }

    private fun drawTextAsset(
        assetIndicatorRight: Float,
        context: Context,
        it: TimelineAsset,
        o: Double,
        springDisplacement: Double,
        canvas: Canvas?,
        attrs: TimelineAttrs
    ) {
        paint.reset()

        rectF.apply {
            left = assetIndicatorRight + TimelineAttrs.convertDpToPixel(10f, context)
            right = left + it.staticLayout?.width!! + 30f

            top = ((o - 10 - (it.staticLayout?.height?.div(2)
                ?: 0)) + springDisplacement).toFloat()
            bottom = ((o + 10 + (it.staticLayout?.height?.div(2)
                ?: 0)) + springDisplacement).toFloat()
        }

        canvas?.drawRoundRect(
            rectF,
            attrs.textRectCorner, attrs.textRectCorner, paint.apply {
                color = it.backgroundColor
            }
        )

        canvas?.save()

        canvas?.translate(
            rectF.left + 20,
            (springDisplacement + o - it.staticLayout?.height?.div(2)!!).toFloat()
        )
        it.staticLayout?.draw(canvas)

        canvas?.restore()
    }

    private fun indicator(
        startTime: Double?,
        endTime: Double?,
        scale: Double,
        tickOffset: Double,
        context: Context,
        assetIndicatorLeft: Float,
        assetIndicatorRight: Float,
        canvas: Canvas?,
        backgroundColor: Int,
        smallScaleTickDistance: Float
    ) {
        startTime?.let { start ->
            rectF.apply {
                top = start.times(scale).toFloat() + tickOffset.toFloat() + smallScaleTickDistance
                bottom = endTime?.times(scale)?.toFloat()?.plus(tickOffset.toFloat())?.plus(smallScaleTickDistance )?: top + indicatorHeight
                left = assetIndicatorLeft
                right = assetIndicatorRight
            }
            canvas?.drawRoundRect(rectF, 10f, 10f, paint.apply {
                color = backgroundColor
            })
        }
    }

    private fun drawLongTickText(
        tracker: TimelineTracker,
        tt: Double,
        attrs: TimelineAttrs,
        canvas: Canvas?,
        offset: Point,
        o: Double
    ) {
        val timeText = tracker.getTimeInText(abs(tt)) ?: ""

        textPaint.apply {
            reset()
            textSize = attrs.timelineTextSize.toFloat()
            color = attrs.timelineTextColor
            canvas?.drawText(
                timeText,
                offset.x.toFloat() + attrs.gutterWidth - measureText(timeText) - attrs.shortTickSize,
                (offset.y + o - 8).toFloat(),
                this
            )
        }
    }


    private fun drawTopBottomText(canvas: Canvas?, height: Int, leftPad: Float, text: String, context: Context) {

        textPaint.apply {
            reset()

            textSize = TimelineAttrs.convertDpToPixel(16f, context)
            color = Color.WHITE

            val textHeight = fontMetrics.bottom - fontMetrics.top

            if (height == 0) {

                rectF.let {
                    it.left = leftPad + 60f
                    it.right = measureText(text) + it.left + 40
                    it.top = 30f
                    it.bottom = it.top + textHeight + 30f
                }

            } else {
                rectF.let {
                    it.left = leftPad + 60f
                    it.right = measureText(text) + it.left + 40
                    it.bottom = height - 30f
                    it.top = it.bottom - textHeight - 30f
                }
            }

            canvas?.drawRoundRect(
                rectF,
                10f,
                10f,
                paint.apply {
                    reset()
                    color = Color.parseColor("#63C7BA")
                }
            )

            canvas?.drawCircle(leftPad + 20f, rectF.top + (rectF.bottom - rectF.top) / 2, 10f, paint.apply {
                reset()
                color = Color.parseColor("#63C7BA")
            })

            canvas?.drawText(
                text,
                rectF.left + 20,
                rectF.top + (rectF.bottom - rectF.top) / 2 + (fontMetrics.bottom - fontMetrics.top) / 4,
                this
            )
        }
    }


    private fun drawTick(
        canvas: Canvas?,
        tickWidth: Int,
        tickHeight: Int,
        tickColor: Int,
        gutterWidth: Int,
        o: Double
    ) {
        canvas?.drawRect(
            Rect(
                (gutterWidth - tickWidth),
                o.toInt(), gutterWidth, o.toInt() + tickHeight
            ),
            paint.apply { color = tickColor })
    }

}