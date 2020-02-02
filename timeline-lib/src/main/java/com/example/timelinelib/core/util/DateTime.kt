package com.example.timelinelib.core.util

import org.threeten.bp.*

class DateTime(var years: Int, var months: Int, var days: Int) : Comparable<DateTime> {

    var localDate: LocalDate = LocalDate.now()

    init {
        localDate = LocalDate.of(years, months, days)
    }

    constructor() : this(LocalDate.now())

    constructor(localDate: LocalDate) : this(
        localDate.year,
        localDate.month.value,
        localDate.dayOfMonth
    ) {
        this.localDate = localDate
    }


    fun year() = years.toString()
    fun month() = Month.values()[months - 1]
    fun day() = days

    override fun compareTo(other: DateTime): Int {
        return this.localDate.compareTo(other.localDate)
    }
}