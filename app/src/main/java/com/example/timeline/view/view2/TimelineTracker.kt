package com.example.timeline.view.view2

import android.util.Log
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
            if (value?.startTime?.dateTime == null) throw NullPointerException("TimelineEntry Null")

            startTime = value.startTime?.dateTime!!
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


    fun getTime(i: Double): String? =
        startTime.let {
            when (timelineScaleType) {
                TimelineType.YEAR -> it.plusYears((i / attrs?.longTickDistance!!).toLong()).year.toString()
                TimelineType.MONTH -> it.plusMonths((i / attrs?.longTickDistance!!).toLong())?.let {
                    it.month.name + it.year.toString()
                }

                TimelineType.DAY -> it.plusDays((i / attrs?.longTickDistance!!).toLong())?.let {
                    it.dayOfMonth.toString() + it.month.name + it?.year.toString()
                }
            }
        }

    fun changeStartTime(i: Long): ZonedDateTime? = when (timelineScaleType) {
        TimelineType.YEAR -> startTime.plusYears(i)
        TimelineType.MONTH -> startTime.plusMonths(i)
        TimelineType.DAY -> startTime.plusDays(i)
    }


}