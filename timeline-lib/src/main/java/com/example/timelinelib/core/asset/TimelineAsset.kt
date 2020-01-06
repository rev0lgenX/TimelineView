package com.example.timelinelib.core.asset

import android.content.Context
import android.text.StaticLayout
import android.util.Log
import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.core.util.dip

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

    private val TAG: String = TimelineAsset::class.java.simpleName
    var eventStartDate: DateTime? = start
    var eventEndDate: DateTime? = end
    var staticLayout: StaticLayout? = null

    var yearStartPosition = 0
    var monthStartPosition = 0
    var dayStartPosition = 0

    var yearEndPosition = 0
    var monthEndPosition = 0
    var dayEndPosition = 0


    var yearStartTracker = 0 //in pixel
    var yearEndTracker = 0


    var monthStartTracker = 0
    var monthEndTracker = 0

    var dayStartTracker = 0
    var dayEndTracker = 0

    var paddingLeft = 0
        set(value) {
            paddingLeftTracker = value
            field = value
        }

    var paddingLeftTracker = 0
    val childAssetsForPadding by lazy { mutableListOf<TimelineAsset>() }

    fun totalChildSize() = childAssetsForPadding.distinctBy { it.paddingLeft }.size

    fun addChildAssetPadding(context: Context) {
        childAssetsForPadding.forEach { subasset ->
            subasset.paddingLeft = paddingLeft.plus(padding(context))
        }
    }

    fun padding(context: Context) = context.dip(2.2f).plus(context.dip(4).times(2))

    fun hideChildPadding(padding:Int) {
        childAssetsForPadding.forEach { subasset ->
            subasset.paddingLeftTracker = subasset.paddingLeft - padding
        }
    }

    fun showChildPadding() {
        childAssetsForPadding.forEach { subasset ->
            subasset.paddingLeftTracker = subasset.paddingLeft
        }
    }

    var paddingRight: Int = 0
    var paddingBottom: Int = 0
    var paddingTop: Int = 0

}