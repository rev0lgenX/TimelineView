package com.example.timelinelib.core.asset

import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.exception.DateTimeException
import org.threeten.bp.LocalDate


/**
 * @author mee pushwant rai rev0lgenX
 *
 */

//all entry info will go here
class TimelineEntry {

    /**
    * beginning of timeline
     */
    var startTime: DateTime? = DateTime(LocalDate.now())
        set(value) {
            if(value == null) throw DateTimeException("DateTime Null")
            field = value
            endTime = DateTime(value.localDate.plusYears(120))
        }

    var birthTime:DateTime? = null

    /**
     * ending of timeline
    */
    //TODO:// need to check for stop infinite scrolling
    var endTime: DateTime? = null

    /**
     * container for all asset between specified start time and end time;
     */
    var timelineAssets:List<TimelineAsset>? = null
}