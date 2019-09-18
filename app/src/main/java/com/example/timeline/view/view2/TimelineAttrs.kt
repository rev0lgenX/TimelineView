package com.example.timeline.view.view2

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics

class TimelineAttrs(private val context: Context){
    var longTickDistance = LONG_TICK_DISTANCE
        set(value) {
            shortTickDistance = value/4
            field = value
        }
    var shortTickDistance = longTickDistance/4

    var shortTickSize = SHORT_TICK_SIZE
    var longTickSize = LONG_TICK_SIZE
    var timelineTextSize = TIME_LINE_TEXT_SIZE
    var textSize = TEXT_SIZE
    var gutterWidth = GUTTER_WIDTH
    var tickColor = TICK_COLOR
    var gutterColor = GUTTER_COLOR
    var timelineTextColor = TIMELINE_TEXT_COLOR
    var textRectCorner = convertDpToPixel(8f, context)


    companion object{
        const val LONG_TICK_SIZE = 40
        const val SHORT_TICK_SIZE = 20
        const val LONG_TICK_DISTANCE = 160
        const val TIME_LINE_TEXT_SIZE = 20
        const val TEXT_SIZE = 20f
        const val GUTTER_WIDTH = 100
        const val TICK_COLOR = Color.WHITE
        const val GUTTER_COLOR = Color.CYAN
        const val TIMELINE_TEXT_COLOR = Color.WHITE
        fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun convertPixelsToDp(px: Float, context: Context): Float {
            return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}