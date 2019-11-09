package com.example.timeline.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class CustomViewGroup(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attributeSet) {

    constructor(context: Context):this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?):this(context, attributeSet, 0)

    init {
        addView(ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setImageResource(android.R.drawable.alert_dark_frame)
            scaleType = ImageView.ScaleType.CENTER_CROP
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        Log.d("ViewGroup", childCount.toString())
        getChildAt(0).let {
            val width = it.measuredWidth
            val height = it.measuredHeight
            setMeasuredDimension(
                View.resolveSizeAndState(width, widthMeasureSpec, 0),
                View.resolveSizeAndState(height, heightMeasureSpec, 0)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChildAt(0).let {
            Log.d("ustomviewgroup", "width $width height $height, measuredWidth ${it.measuredWidth} measuredheight ${it.measuredHeight}")
            it.layout(width - it.measuredWidth , height - it.measuredHeight, width, it.measuredHeight)
        }
    }

}