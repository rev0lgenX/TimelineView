package com.example.timelinelib.core

import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.core.util.TimelineAttrs
import org.threeten.bp.LocalDate
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
            field = value
            startTime = value?.startTime?.localDate!!

            if (value.endTime == null) return

            timeEndPositionYear =
                ChronoUnit.YEARS.between(
                    value.startTime!!.localDate, value.endTime!!.localDate
                ).times(attrs?.longTickDistance!!)
                    .toInt()

            timeEndPositionMonth =
                ChronoUnit.MONTHS.between(
                    value.startTime!!.localDate.withDayOfMonth(1),
                    value.endTime!!.localDate.withDayOfMonth(1)
                ).times(attrs?.longTickDistance!!)
                    .toInt()

            timeEndPositionDay = ChronoUnit.DAYS.between(
                value.startTime!!.localDate,
                value.endTime!!.localDate
            ).times(attrs?.longTickDistance!!)
                .toInt()

        }

    var timelineScaleType = TimelineType.YEAR        //tracking year month day hrs
    var arbitraryStart = 0.0               //tracking pixel
    var arbitraryEnd = 0.0
    var focalDistance = 0.0
    var focalPoint = 0.0

    var startTime: LocalDate? = null

    var timeEndPositionYear: Int? = null
    var timeEndPositionMonth: Int? = null
    var timeEndPositionDay: Int? = null


    fun expandTimelineType() {
        timelineScaleType = TimelineType.values()[timelineScaleType.ordinal + 1]

        when (timelineScaleType) {
            TimelineType.MONTH -> {
                arbitraryStart =
                    (ChronoUnit.MONTHS.between(
                        startTime,
                        startTime?.plusYears((focalDistance / attrs?.longTickDistance!!).toLong())
                    ) * attrs?.longTickDistance!!).toDouble()
            }
            TimelineType.DAY -> {
                arbitraryStart =
                    (ChronoUnit.DAYS.between(
                        startTime,
                        startTime?.plusMonths((focalDistance / attrs?.longTickDistance!!).toLong())
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
                        startTime,
                        startTime?.plusMonths((focalDistance / attrs?.longTickDistance!!).toLong())
                    ) * attrs?.longTickDistance!!).toDouble() - focalPoint
            }
            TimelineType.MONTH -> {
                arbitraryStart =
                    (ChronoUnit.MONTHS.between(
                        startTime,
                        startTime?.plusDays((focalDistance / attrs?.longTickDistance!!).toLong())
                    ) * attrs?.longTickDistance!!).toDouble() - focalPoint
            }
        }
    }


    fun getTime(i: Double): DateTime? =
        startTime?.let {
            when (timelineScaleType) {
                TimelineType.YEAR -> DateTime(it.plusYears((i / attrs?.longTickDistance!!).toLong()))
                TimelineType.MONTH -> DateTime(it.plusMonths((i / attrs?.longTickDistance!!).toLong()))
                TimelineType.DAY -> DateTime(it.plusDays((i / attrs?.longTickDistance!!).toLong()))
            }
        }

    fun getTimeInText(i: Double): String? =
        startTime?.let {
            when (timelineScaleType) {
                TimelineType.YEAR -> it.plusYears((i / attrs?.longTickDistance!!).toLong()).year.toString()
                TimelineType.MONTH -> it.plusMonths((i / attrs?.longTickDistance!!).toLong())?.month?.name?.substring(
                    0,
                    3
                )
                TimelineType.DAY -> it.plusDays((i / attrs?.longTickDistance!!).toLong())?.dayOfMonth?.toString()
            }
        }


}