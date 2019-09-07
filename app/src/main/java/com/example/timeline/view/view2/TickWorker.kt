package com.example.timeline.view.view2

import android.graphics.*
import android.text.TextPaint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

class TickWorker(listener: TickWorkerListener?) {

    constructor():this(null)

    private val TAG = TickWorker::class.java.simpleName
    private val mListener: TickWorkerListener? = listener

    fun work(
        canvas: Canvas?,
        attrs: TimelineAttrs,
        height: Int,
        scale: Double,
        offset: Point,
        tracker: TimelineTracker
    ) {

        var currentScale = scale

        var smallScaleTickDistance = attrs.shortTickDistance * currentScale

        if (smallScaleTickDistance >= 2 * attrs.shortTickDistance) {
            if (tracker.timelineScaleType == TimelineTracker.TimelineType.DAY) {
                currentScale = 2.0
                smallScaleTickDistance = (2.0 * attrs.shortTickDistance)
                mListener?.stopExpanding()
            } else {
                tracker.expandTimelineType()
                mListener?.onScaleReset()
            }
        } else if (smallScaleTickDistance < 0.5 * attrs.shortTickDistance) {
            if (tracker.timelineScaleType == TimelineTracker.TimelineType.YEAR) {
                currentScale = 0.5
                smallScaleTickDistance = (0.5 * attrs.shortTickDistance)
                mListener?.stopContracting()
            } else {
                tracker.collapseTimelineType()
                mListener?.onScaleReset()
            }
        }

        val numTicks = (ceil(height / smallScaleTickDistance) + 2).toInt()

        val gutterWidth = attrs.gutterWidth

        canvas?.drawRect(
            Rect(offset.x, offset.y, gutterWidth, height),
            Paint().apply {
                color = attrs.gutterColor
            })

        var tickOffset: Double
        var startingTickMarkValue: Double


        val y = tracker.arbitraryStart
        startingTickMarkValue = y - (y % attrs.shortTickDistance)

        tickOffset = -((y % attrs.shortTickDistance) * currentScale) - smallScaleTickDistance + 10

        var longTickCount = 0

        for (i in 0 until numTicks) {

            tickOffset += smallScaleTickDistance

            var tt = round(startingTickMarkValue)
            tt = -tt

            val o = floor(tickOffset)

            if ((tt % attrs.longTickDistance).toInt() == 0) {
                canvas?.drawRect(
                    Rect(
                        offset.x + gutterWidth - attrs.longTickSize,
                        (offset.y + o).toInt(), gutterWidth, (offset.y + o).toInt() + 2
                    ),
                    Paint().apply { color = attrs.tickColor })

                TextPaint().apply {
                    textSize = attrs.timelineTextSize.toFloat()
                    color = attrs.timelineTextColor
                    canvas?.drawText(
                        tracker.getTime(abs(tt)) ?: "",
//                        abs(tt).toString(),
                        offset.x.toFloat(),
                        (offset.y + o + this.fontMetrics.descent - this.fontMetrics.ascent + 5).toFloat(),
                        this
                    )
                    longTickCount++
                }

            } else {
                canvas?.drawRect(
                    Rect(
                        (offset.x + gutterWidth - attrs.shortTickSize),
                        (offset.y + o).toInt(), gutterWidth, (offset.y + o).toInt() + 2
                    ),
                    Paint().apply { color = attrs.tickColor })
            }

            startingTickMarkValue += attrs.shortTickDistance
        }


    }

}