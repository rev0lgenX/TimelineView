package com.example.timelinelib.core.asset

import android.text.StaticLayout
import com.example.timelinelib.core.util.DateTime

//event holder
class TimelineAsset(
    var id:Int,
    start: DateTime?,
    end: DateTime?,
    var description: String?,
    var backgroundColor: Int?= null,
    var image: Int? = null
) {
    var eventStartDate: DateTime? = start
    var eventEndDate: DateTime? = end
    var staticLayout: StaticLayout? = null

    var yearStartPosition: Int? = null
    var monthStartPosition: Int? = null
    var dayStartPosition: Int? = null

    var yearEndPosition: Int? = null
    var monthEndPosition: Int? = null
    var dayEndPosition: Int? = null


    var yearStartTracker: Int? = null
    var yearEndTracker: Int? = null


    var monthStartTracker: Int? = null
    var monthEndTracker: Int? = null

    var dayStartTracker: Int? = null
    var dayEndTracker: Int? = null

    var paddingLeft: Int = 0
    var paddingRight: Int = 0
    var paddingTop: Int = 0
    var paddingBottom: Int = 0
}