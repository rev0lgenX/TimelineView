package com.example.timelinelib.core.asset

import android.text.StaticLayout
import com.example.timelinelib.core.util.DateTime

//event holder
class TimelineAsset(
    var id: Int,
    start: DateTime?,
    end: DateTime?,
    var title: String? = null,
    var description: String?,
    var backgroundColor: Int? = null,
    var image: Int? = null
) {

    var eventStartDate: DateTime? = start
    var eventEndDate: DateTime? = end
    var staticLayout: StaticLayout? = null

    var yearStartPosition = 0
    var monthStartPosition = 0
    var dayStartPosition = 0

    var yearEndPosition = 0
    var monthEndPosition = 0
    var dayEndPosition = 0


    var yearStartTracker = 0
    var yearEndTracker = 0


    var monthStartTracker = 0
    var monthEndTracker = 0

    var dayStartTracker = 0
    var dayEndTracker =0

    var paddingLeft = 0
        set(value) {
            paddingLeftTracker = value
            field = value
        }

    val childAssetsForPadding by lazy { mutableListOf<TimelineAsset>() }


    fun updateChildAssetPadding(indicatorWidth: Float, times: Float) {
        childAssetsForPadding.forEach { subasset ->
            subasset.paddingLeft = indicatorWidth
                .plus(paddingLeft)
                .plus(times)
                .toInt()
        }
    }

    var paddingRight: Int = 0
    var paddingBottom: Int = 0
    var paddingTop: Int = 0

    var paddingLeftTracker = 0
}