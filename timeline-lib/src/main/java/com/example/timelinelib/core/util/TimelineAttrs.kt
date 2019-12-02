package com.example.timelinelib.core.util

import androidx.annotation.ColorInt

/**
 * @param longTickDistance
 * @param shortTickDistance
 * @param shortTickSize timeline short tick size
 * @param longTickSize timeline long tick size
 * @param timelineTextSize timeline tick text size
 * @param textSize timeline description size
 * @param gutterWidth timeline gutter width
 * @param indicatorTextSize timeline indication text size
 * @param tickColor timeline tick color
 * @param gutterColor timeline gutter color
 * @param timelineTextColor timeline text color
 * @param timelineTextBackgroundColor timeline description text background color
 * @param indicatorColor timeline indicator color
 * @param textRectCorner timeline text rect corner dimen
 */

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
    @ColorInt var timelineTextBackgroundColor:Int,
    @ColorInt var indicatorColor:Int,
    var textRectCorner: Float
)