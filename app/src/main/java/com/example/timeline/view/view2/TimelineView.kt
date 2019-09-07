package com.example.timeline.view.view2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.timeline.R
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

class TimelineView(context: Context, private val attributeSet: AttributeSet?) : View(context, attributeSet) {


    private val TAG = TimelineView::class.java.simpleName
    private var timelineAttrs: TimelineAttrs = TimelineAttrs()

    var manager: TimelineManager? = null
        set(value) {
            if (value == null) {
                throw IllegalArgumentException("Manager shouldn't be null")
            } else {
                field = value
                value.view = this
                value.attrs = timelineAttrs
                invalidate()
            }
        }

    constructor(context: Context) : this(context, null)


    init {
        attributeSet?.let {
            context.theme.obtainStyledAttributes(it, R.styleable.Timeline, 0, 0).apply {
                try {
                    TimelineAttrs.let {
                        timelineAttrs.apply {
                            shortTickSize =
                                getDimensionPixelSize(R.styleable.Timeline_shortTickSize, it.SHORT_TICK_SIZE)
                            longTickSize =
                                getDimensionPixelSize(R.styleable.Timeline_longTickSize, it.LONG_TICK_SIZE)
                            longTickDistance =
                                getDimensionPixelSize(R.styleable.Timeline_longTickDistance, it.LONG_TICK_DISTANCE)
                            timelineTextSize =
                                getDimensionPixelSize(R.styleable.Timeline_timelineTextSize, it.TIME_LINE_TEXT_SIZE)
                            textSize =
                                getDimensionPixelSize(R.styleable.Timeline_textSize, it.TEXT_SIZE)
                            gutterWidth =
                                getDimensionPixelSize(R.styleable.Timeline_gutterWidth, it.GUTTER_WIDTH)
                            gutterColor =
                                getColor(R.styleable.Timeline_gutterColor, it.GUTTER_COLOR)
                            tickColor =
                                getColor(R.styleable.Timeline_tickColor, it.TICK_COLOR)
                            timelineTextColor =
                                getColor(R.styleable.Timeline_timelineTextColor, it.TIMELINE_TEXT_COLOR)
                        }
                    }

                } finally {
                    recycle()
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (manager == null) {
            TickWorker().work(canvas, timelineAttrs, height, 1.0, Point(0,0), TimelineTracker())
        }

        manager!!.manage(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (manager == null) throw NullPointerException("Manager shouldn't be null")

        return manager!!.onTouchEvent(event)
    }
}