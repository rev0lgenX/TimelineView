package com.example.timelinelib.core

import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.core.util.TimelineAttrs
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

class TimelineTracker {

    constructor()

    private val TAG = TimelineTracker::class.java.simpleName

    constructor(timelineEntry: TimelineEntry) {
        this.timelineEntry = timelineEntry
    }

    enum class TimelineType(private val value: Int) {
        YEAR(0), MONTH(1), DAY(2);
    }

    var attrs: TimelineAttrs? = null

    var timelineEntry: TimelineEntry? = null
        set(value) {
            startTime = value?.startTime?.dateTime!!
            field = value
        }

    var timelineScaleType = TimelineType.YEAR        //tracking year month day hrs
    var arbitraryStart = 0.0               //tracking for pixel
    var focalDistance = 0.0
    var focalPoint = 0.0

    lateinit var startTime: ZonedDateTime


    fun expandTimelineType() {
        timelineScaleType = TimelineType.values()[timelineScaleType.ordinal + 1]

        when (timelineScaleType) {
            TimelineType.MONTH -> {
                arbitraryStart =
                    (ChronoUnit.MONTHS.between(
                        startTime.toLocalDate(),
                        startTime.plusYears((focalDistance / attrs?.longTickDistance!!).toLong())?.toLocalDate()
                    ) * attrs?.longTickDistance!!).toDouble()
            }
            TimelineType.DAY -> {
                arbitraryStart =
                    (ChronoUnit.DAYS.between(
                        startTime.toLocalDate(),
                        startTime.plusMonths((focalDistance / attrs?.longTickDistance!!).toLong())?.toLocalDate()
                    ) * attrs?.longTickDistance!!).toDouble() - focalPoint
            }
        }
    }

    fun collapseTimelineType() {
        timelineScaleType = TimelineType.values()[timelineScaleType.ordinal - 1]

        when (timelineScaleType) {
            TimelineType.YEAR -> {
                arbitraryStart =
                    (ChronoUnit.YEARS.between(
                        startTime.toLocalDate(),
                        startTime.plusMonths((focalDistance / attrs?.longTickDistance!!).toLong())?.toLocalDate()
                    ) * attrs?.longTickDistance!!).toDouble() - focalPoint
            }
            TimelineType.MONTH -> {
                arbitraryStart =
                    (ChronoUnit.MONTHS.between(
                        startTime.toLocalDate(),
                        startTime.plusDays((focalDistance / attrs?.longTickDistance!!).toLong())?.toLocalDate()
                    ) * attrs?.longTickDistance!!).toDouble() - focalPoint
            }
        }
    }


    fun getTime(i: Double): DateTime =
        startTime.let {
            when (timelineScaleType) {
                TimelineType.YEAR -> DateTime(it.plusYears((i / attrs?.longTickDistance!!).toLong()))
                TimelineType.MONTH -> DateTime(it.plusMonths((i / attrs?.longTickDistance!!).toLong()))
                TimelineType.DAY -> DateTime(it.plusDays((i / attrs?.longTickDistance!!).toLong()))
            }
        }

    fun getTimeInText(i: Double): String? =
        startTime.let {
            when (timelineScaleType) {
                TimelineType.YEAR -> it.plusYears((i / attrs?.longTickDistance!!).toLong()).year.toString()
                TimelineType.MONTH -> it.plusMonths((i / attrs?.longTickDistance!!).toLong())?.month?.name?.substring(0, 3)
                TimelineType.DAY -> it.plusDays((i / attrs?.longTickDistance!!).toLong())?.dayOfMonth?.toString()
            }
        }


}