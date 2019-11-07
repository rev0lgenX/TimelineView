package com.example.timelinelib.core.asset

import com.example.timelinelib.core.util.DateTime


//all entry info will go here
class TimelineEntry {
    var startTime: DateTime? = DateTime()
        set(value) {
            if(value == null) throw NullPointerException("DateTime Null")
            endTime = DateTime().apply {
                dateTime = value.dateTime.plusYears(120)!!
            }
            field = value
        }
    var endTime: DateTime? = null
    var timelineAssets:List<TimelineAsset>? = null
}