package com.example.timelinelib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.example.timelinelib.view.TimelineRenderer

class TimelineView(context: Context, attributeSet: AttributeSet?, defStyle:Int=0) : ViewGroup(context, attributeSet, defStyle){

    private lateinit var tView: TimelineRenderer


    constructor(context: Context):this(context, null){
        tView = TimelineRenderer(context)
    }

    constructor(context: Context, attributeSet: AttributeSet):this(context, attributeSet, 0){
        tView = TimelineRenderer(context, attributeSet)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

}
