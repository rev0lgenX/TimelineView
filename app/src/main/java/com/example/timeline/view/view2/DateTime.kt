package com.example.timeline.view.view2

import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class DateTime() {
    var dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault())!!
    var year: Int = -1
    var month: Int = -1
    var day: Int = -1

    constructor(zonedDateTime: ZonedDateTime) : this() {
        this.dateTime = zonedDateTime
        year = zonedDateTime.year
        month = zonedDateTime.month.ordinal
        day = zonedDateTime.dayOfMonth
    }

    fun year(): String = if (year < 0) "" else year.toString()
    fun month(): String = if (month < 0) "" else Month.values()[month].toString()
    fun day(): String = if (day < 0) "" else day.toString()


}