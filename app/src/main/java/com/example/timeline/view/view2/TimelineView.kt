package com.example.timeline.view.view2

import android.content.Context
import android.graphics.Canvas
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
                try{
                    timelineAttrs.shortTickSize = getDimensionPixelSize(R.styleable.Timeline_shortTickSize,TimelineAttrs.SHORT_TICK_SIZE)
                    timelineAttrs.longTickSize = getDimensionPixelSize(R.styleable.Timeline_longTickSize, TimelineAttrs.LONG_TICK_SIZE)
                    timelineAttrs.longTickDistance = getDimensionPixelSize(R.styleable.Timeline_longTickDistance, TimelineAttrs.LONG_TICK_DISTANCE)
                    timelineAttrs.timelineWidth = getDimensionPixelSize(R.styleable.Timeline_timelineWidth, TimelineAttrs.TIMELINE_WIDTH)
                }finally {
                    recycle()
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        if (manager == null) throw NullPointerException("Manager shouldn't be null")

        manager?.manage(canvas)

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (manager == null) throw NullPointerException("Manager shouldn't be null")

        return manager!!.onTouchEvent(event)
    }
}