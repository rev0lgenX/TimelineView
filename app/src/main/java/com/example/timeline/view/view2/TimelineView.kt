package com.example.timeline.view.view2

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.timeline.R
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import android.text.Layout


class TimelineView(context: Context, private val attributeSet: AttributeSet?) : View(context, attributeSet) {


    private val TAG = TimelineView::class.java.simpleName
    private var timelineAttrs: TimelineAttrs = TimelineAttrs(context)
    val rect = RectF()

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
                                getDimensionPixelSize(R.styleable.Timeline_textSize, it.TEXT_SIZE.toInt()).toFloat()
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
            TimelineWorker().work(context, canvas, timelineAttrs, height, 1.0,0.0, Point(0, 0), TimelineTracker())
        }

//
//        rect.left = 100f
//        rect.right = 200f
//        rect.top = 100f - 10f
//        rect.bottom = rect.top + staticLayout.height + 10f
//
//        canvas?.drawRoundRect(
//            rect,
//            timelineAttrs.textRectCorner, timelineAttrs.textRectCorner, Paint().apply {
//                color = Color.CYAN
//            }
//        )
//
//
//        canvas?.save()
//        canvas?.translate(150f, 150f)
//        staticLayout.draw(canvas)
//        canvas?.restore()


        manager!!.manage(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (manager == null) throw NullPointerException("Manager shouldn't be null")

        return manager!!.onTouchEvent(event)
    }


}