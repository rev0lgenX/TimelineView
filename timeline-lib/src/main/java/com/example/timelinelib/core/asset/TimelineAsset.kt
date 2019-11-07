package com.example.timelinelib.core.asset

import android.graphics.Color
import android.text.StaticLayout
import com.example.timelinelib.core.util.DateTime

//event holder
class TimelineAsset(start: DateTime?, end: DateTime?, description: String?, color: Int?, image: Int?) {
    var eventStartDate: DateTime? = start
    var eventEndDate: DateTime? = end

    var staticLayout: StaticLayout? = null

    var description: String? = description


    var backgroundColor: Int = color ?: Color.YELLOW
    var backgroundImage: Int = image ?: -1

    var yearStartPosition: Double? = null

    var monthStartPosition: Double? = null

    var dayStartPosition: Double? = null

    var yearEndPosition: Double? = null

    var monthEndPosition: Double? = null

    var dayEndPosition: Double? = null



    var yearStartTracker: Double? = null
    var yearEndTracker: Double? = null


    var monthStartTracker: Double? = null
    var monthEndTracker: Double? = null

    var dayStartTracker: Double? = null
    var dayEndTracker: Double? = null

}