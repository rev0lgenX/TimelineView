package com.example.timelinelib.core

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.timelinelib.R
import com.example.timelinelib.core.asset.TimelineAsset
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.util.TimelineAttrs
import com.example.timelinelib.listener.OnTimelineBehaviourListener
import com.example.timelinelib.listener.TickWorkerListener
import kotlin.math.*


typealias AssetClickListener = ((TimelineAsset) -> Unit)?


class TimelineWorker(
    var context: Context,
    tListener: TickWorkerListener?,
    aListener: OnTimelineBehaviourListener?,
    private var assetClickListener: AssetClickListener = null
) {

    private val TAG = TimelineWorker::class.java.simpleName
    private val paint = Paint()
    private val textPaint = TextPaint()
    private val rectF = RectF()

    private val tickWorkerListener: TickWorkerListener? = tListener
    private val timelineBehaviourListener: OnTimelineBehaviourListener? = aListener


    private val indicatorHeight = context.resources.getDimension(R.dimen.indicatorHeight)
    private val indicatorWidth = context.resources.getDimension(R.dimen.indicatorWidth)
    private val indicatorRadius = context.resources.getDimension(R.dimen.indicatorRadius)
    private val indicatorPaddingLeft = context.resources.getDimension(R.dimen.indicatorPaddingLeft)
    private val textPadding = context.resources.getDimension(R.dimen.textPadding)
    private var view: View? = null

    var assetAboveScreen: TimelineAsset? = null
    var assetBelowScreen: TimelineAsset? = null
    private val handler: Handler = Handler()

    private val checkIndicatorRunnable = Runnable {
        //        checkIndicatorLeftPadding(tracker)
    }

    private val invalidate = view?.invalidate()


    private val visibleAssetLocation = mutableMapOf<Int, TimelineAssetLocation>()

    private fun removeIndicatorRunnable() = handler.removeCallbacks(checkIndicatorRunnable)
    private fun checkIndicatorRunnable() = handler.postDelayed(checkIndicatorRunnable, 2000)

    fun onSingleTap(event: MotionEvent) {
        val x = event.x
        val y = event.y

        visibleAssetLocation.forEach {
            if (it.value.rectF.contains(x, y)) {
                assetClickListener?.invoke(it.value.asset)
                return
            }
        }
    }

    fun work(
        view: View,
        canvas: Canvas?,
        attrs: TimelineAttrs,
        height: Int,
        scale: Double,
        springDisplacement: Double,
        offset: Point,
        tracker: TimelineTracker
    ) {
        visibleAssetLocation.clear()
        val context = view.context
        this.view = view
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
        val assetIndicatorLeft = gutterWidth + indicatorPaddingLeft
        val assetIndicatorRight = assetIndicatorLeft + indicatorWidth

        canvas?.drawRect(
            Rect(offset.x, offset.y, gutterWidth, height),
            Paint().apply {
                setShadowLayer(
                    8f,
                    0f,
                    0f,
                    ContextCompat.getColor(context, R.color.gutterShadowColor)
                )
                color = attrs.gutterColor
            })

        var tickOffset: Double
        var startingTickMarkValue: Double


        val y = tracker.arbitraryStart
        val yBottom = tracker.arbitraryStart + height / scale
        val dateTimeHintLeftPadding =
            5 * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT) + gutterWidth

        startingTickMarkValue = y - (y % attrs.shortTickDistance)

        val endingTickMarkValue =
            startingTickMarkValue.plus(numTicks.times(attrs.shortTickDistance))

        tickOffset = -((y % attrs.shortTickDistance) * currentScale) - smallScaleTickDistance

        var longTickCount = 0

        //reset value
        assetAboveScreen = null
        assetBelowScreen = null


        tracker.timelineEntry?.timelineAssets?.forEach { asset ->
            paint.reset()
            when (tracker.timelineScaleType) {
                TimelineTracker.TimelineType.YEAR -> {
                    asset.yearStartTracker =
                        asset.yearStartPosition.minus(startingTickMarkValue)
                            .times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()

                    asset.yearEndTracker =
                        asset.yearEndPosition.minus(startingTickMarkValue).times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()

                    if (asset.yearStartPosition < startingTickMarkValue) {
                        assetAboveScreen?.let {
                            if (assetAboveScreen?.yearStartPosition?.minus(startingTickMarkValue)?.absoluteValue!!
                                > asset.yearStartPosition.minus(startingTickMarkValue).absoluteValue
                            ) {
                                assetAboveScreen = asset
                            }
                        } ?: let {
                            assetAboveScreen = asset
                        }
                    }

                    if (asset.yearStartPosition > endingTickMarkValue) {
                        assetBelowScreen?.let {
                            if (assetBelowScreen?.yearStartPosition?.minus(endingTickMarkValue)?.absoluteValue!! >
                                asset.yearStartPosition.minus(endingTickMarkValue).absoluteValue
                            ) {
                                assetBelowScreen = asset
                            }
                        } ?: let {
                            assetBelowScreen = asset
                        }
                    }

                    indicator(
                        asset,
                        asset.yearStartTracker,
                        asset.yearEndTracker,
                        scale,
                        tickOffset,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        asset.backgroundColor ?: attrs.timelineTextBackgroundColor,
                        smallScaleTickDistance.toFloat()
                    )

                }

                TimelineTracker.TimelineType.MONTH -> {
                    asset.monthStartTracker =
                        asset.monthStartPosition.minus(startingTickMarkValue)
                            .times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()
                    asset.monthEndTracker =
                        asset.monthEndPosition.minus(startingTickMarkValue)
                            .times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()

                    indicator(
                        asset,
                        asset.monthStartTracker,
                        asset.monthEndTracker,
                        scale,
                        tickOffset,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        asset.backgroundColor ?: attrs.timelineTextBackgroundColor,
                        smallScaleTickDistance.toFloat()
                    )


                    if (asset.monthStartPosition < startingTickMarkValue) {
                        assetAboveScreen?.let {
                            if (assetAboveScreen?.monthStartPosition?.minus(startingTickMarkValue)?.absoluteValue!!
                                > asset.monthStartPosition.minus(startingTickMarkValue).absoluteValue
                            ) {
                                assetAboveScreen = asset
                            }
                        } ?: let {
                            assetAboveScreen = asset
                        }
                    }

                    if (asset.monthStartPosition > endingTickMarkValue) {
                        assetBelowScreen?.let {
                            if (assetBelowScreen?.monthStartPosition?.minus(endingTickMarkValue)?.absoluteValue!! >
                                asset.monthStartPosition.minus(endingTickMarkValue).absoluteValue
                            ) {
                                assetBelowScreen = asset
                            }
                        } ?: let {
                            assetBelowScreen = asset
                        }
                    }

                }

                TimelineTracker.TimelineType.DAY -> {
                    asset.dayStartTracker =
                        asset.dayStartPosition.minus(startingTickMarkValue)
                            .times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()

                    asset.dayEndTracker =
                        asset.dayEndPosition.minus(startingTickMarkValue)
                            .times(currentScale)
                            .plus(tickOffset).plus(smallScaleTickDistance).toInt()
                    indicator(
                        asset,
                        asset.dayStartTracker,
                        asset.dayEndTracker,
                        scale,
                        tickOffset,
                        assetIndicatorLeft,
                        assetIndicatorRight,
                        canvas,
                        asset.backgroundColor ?: attrs.timelineTextBackgroundColor,
                        smallScaleTickDistance.toFloat()
                    )

                    if (asset.dayStartPosition < startingTickMarkValue) {
                        assetAboveScreen?.let {
                            if (assetAboveScreen?.dayStartPosition?.minus(startingTickMarkValue)?.absoluteValue!!
                                > asset.dayStartPosition.minus(startingTickMarkValue).absoluteValue
                            ) {
                                assetAboveScreen = asset
                            }
                        } ?: let {
                            assetAboveScreen = asset
                        }
                    }

                    if (asset.dayStartPosition > endingTickMarkValue) {
                        assetBelowScreen?.let {
                            if (assetBelowScreen?.dayStartPosition?.minus(endingTickMarkValue)?.absoluteValue!! >
                                asset.dayStartPosition.minus(endingTickMarkValue).absoluteValue
                            ) {
                                assetBelowScreen = asset
                            }
                        } ?: let {
                            assetBelowScreen = asset
                        }
                    }

                }
            }

        }

        var tt = 0.0

        for (i in 0 until numTicks) {

            tickOffset += smallScaleTickDistance
            tt = round(startingTickMarkValue)
            tt = -tt

            val o = floor(tickOffset)

            if ((tt % attrs.longTickDistance).toInt() == 0) {
                longTickCount++
                drawTick(canvas, attrs.longTickSize, 2, attrs.tickColor, gutterWidth, o)
                drawLongTickText(tracker, tt, attrs, canvas, offset, o)

                tracker.timelineEntry?.timelineAssets?.forEach {
                    when (tracker.timelineScaleType) {

                        TimelineTracker.TimelineType.YEAR -> {

                            if (it.yearStartPosition.toDouble() == abs(tt)) {
                                drawTextAsset(
                                    0,
                                    assetIndicatorRight,
                                    it,
                                    o,
                                    springDisplacement,
                                    canvas,
                                    attrs
                                )
                            }
                        }

                        TimelineTracker.TimelineType.MONTH -> {
                            if (it.monthStartPosition.toDouble() == abs(tt)) {
                                drawTextAsset(
                                    1,
                                    assetIndicatorRight,
                                    it,
                                    o,
                                    springDisplacement,
                                    canvas,
                                    attrs
                                )
                            }
                        }

                        TimelineTracker.TimelineType.DAY -> {
                            if (it.dayStartPosition.toDouble() == abs(tt)) {
                                drawTextAsset(
                                    2,
                                    assetIndicatorRight,
                                    it,
                                    o,
                                    springDisplacement,
                                    canvas,
                                    attrs
                                )
                            }
                        }
                    }
                }
            } else {
                drawTick(canvas, attrs.shortTickSize, 2, attrs.tickColor, gutterWidth, o)
            }
            startingTickMarkValue += attrs.shortTickDistance
        }

        tracker.arbitraryEnd = -tt


        when {
            tracker.timelineScaleType == TimelineTracker.TimelineType.MONTH -> {
                val topTime = tracker.getTime(y)!!
                val bottomTime = tracker.getTime(yBottom)!!
                drawTopBottomText(
                    canvas,
                    0,
                    dateTimeHintLeftPadding,
                    topTime.year(),
                    attrs.indicatorTextSize
                )
                drawTopBottomText(
                    canvas,
                    height,
                    dateTimeHintLeftPadding,
                    bottomTime.year(),
                    attrs.indicatorTextSize
                )
            }

            tracker.timelineScaleType == TimelineTracker.TimelineType.DAY -> {

                val topTime = tracker.getTime(y)!!
                val bottomTime = tracker.getTime(yBottom)!!

                val topText = "${topTime.year()} ${topTime.month()}"
                val bottomText = "${bottomTime.year()} ${bottomTime.month()}"

                drawTopBottomText(
                    canvas,
                    0,
                    dateTimeHintLeftPadding,
                    topText,
                    attrs.indicatorTextSize
                )

                drawTopBottomText(
                    canvas,
                    height,
                    dateTimeHintLeftPadding,
                    bottomText,
                    attrs.indicatorTextSize
                )
            }
        }

        timelineBehaviourListener?.onAssetVisible(visibleAssetLocation)

//        checkIndicatorLeftPadding(height, tracker)
    }


    private fun checkIndicatorLeftPadding(height: Int, tracker: TimelineTracker) {
        tracker.timelineEntry!!.timelineAssets!!.forEach {
            if (it.yearStartTracker in 1 until height) {
                //show timeline
                showTimelineIndicator(it)
            } else if (it.yearEndTracker in 1 until height) {
                //show timeline
                showTimelineIndicator(it)
            } else if (it.yearStartTracker < 0 && it.yearEndTracker > height) {
                //hide timeline
                hideTimelineIndicator(it)
            }
        }
    }

    private fun hideTimelineIndicator(asset: TimelineAsset) {
        if (asset.totalChildSize() > 2) {
            asset.hideChildPadding(asset.padding(context))
            invalidate
        }
    }

    private fun showTimelineIndicator(asset: TimelineAsset){
        asset.showChildPadding()
        invalidate
    }

    private fun drawTextAsset(
        dateType: Int,
        assetIndicatorRight: Float,
        asset: TimelineAsset,
        o: Double,
        springDisplacement: Double,
        canvas: Canvas?,
        attrs: TimelineAttrs
    ) {
        paint.reset()

        rectF.apply {
            left = assetIndicatorRight + textPadding + asset.paddingLeftTracker
            right = left + 2 * textPadding + asset.staticLayout?.width!!
            top =
                ((o - textPadding - asset.staticLayout?.height?.div(2)!!).plus(springDisplacement)).plus(
                    if (dateType == 0) asset.paddingTop else dateType
                ).toFloat()
            bottom =
                (top.plus(asset.staticLayout?.height!!).plus(springDisplacement).plus(2 * textPadding)).toFloat()
        }



        visibleAssetLocation.forEach {
            if (it.value.rectF.bottom > rectF.top) {
                return
            }
        }

        visibleAssetLocation[asset.id] = TimelineAssetLocation(RectF(rectF), asset)

        canvas?.drawRoundRect(rectF, attrs.textRectCorner, attrs.textRectCorner, paint.apply {
            color = asset.backgroundColor ?: attrs.timelineTextBackgroundColor
        })

        canvas?.save()
        canvas?.translate(
            rectF.left + textPadding,
            rectF.top + textPadding
        )
        asset.staticLayout?.draw(canvas)
        canvas?.restore()
    }

    private fun indicator(
        asset: TimelineAsset,
        startTime: Int?,
        endTime: Int?,
        scale: Double,
        tickOffset: Double,
        assetIndicatorLeft: Float,
        assetIndicatorRight: Float,
        canvas: Canvas?,
        backgroundColor: Int,
        smallScaleTickDistance: Float
    ) {
        startTime?.let { start ->
            rectF.apply {
                top = start.toFloat()
                bottom = endTime?.takeIf { it > top.plus(indicatorHeight) }?.toFloat() ?: top
                left = assetIndicatorLeft + asset.paddingLeftTracker
                right = assetIndicatorRight + asset.paddingLeftTracker
            }

            paint.color = backgroundColor

            canvas?.drawCircle(rectF.centerX(), rectF.top, indicatorRadius, paint)
            canvas?.drawCircle(rectF.centerX(), rectF.bottom, indicatorRadius, paint)
            canvas?.drawRoundRect(rectF, 10f, 10f, paint)
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
            typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
            canvas?.drawText(
                timeText,
                offset.x.toFloat() + attrs.gutterWidth - measureText(timeText) - attrs.shortTickSize,
                (offset.y + o - 8).toFloat(),
                this
            )
        }
    }


    private fun drawTopBottomText(
        canvas: Canvas?,
        height: Int,
        leftPad: Float,
        text: String,
        indicatorTextSize: Float
    ) {

        textPaint.apply {
            reset()
            textSize = indicatorTextSize
            color = Color.WHITE
            this.typeface = ResourcesCompat.getFont(context, R.font.open_sans_semi_bold)

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

            canvas?.drawCircle(
                leftPad + 20f,
                rectF.top + (rectF.bottom - rectF.top) / 2,
                10f,
                paint.apply {
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