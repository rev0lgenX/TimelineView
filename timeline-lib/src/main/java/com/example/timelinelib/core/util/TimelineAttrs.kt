package com.example.timelinelib.core.util

import androidx.annotation.ColorInt


data class TimelineAttrs(
    var longTickDistance: Int,
    var shortTickDistance: Int,
    var shortTickSize: Int,
    var longTickSize: Int,
    var timelineTextSize: Int,
    var textSize: Float,
    var gutterWidth: Int,
    var indicatorTextSize:Float,
    @ColorInt var tickColor: Int,
    @ColorInt var gutterColor: Int,
    @ColorInt  var timelineTextColor: Int,
    @ColorInt var indicatorColor:Int,
    var textRectCorner: Float = 8f
)