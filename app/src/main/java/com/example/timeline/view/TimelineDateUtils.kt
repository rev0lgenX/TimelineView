package com.example.timeline.view

class TimelineDateUtils {
    var year = -1
    var month = -1
        set(value) {
            if (value in 1..12) field = value else -1
        }

    var day = -1

    var hrs = -1
        set(value) {
            if (value in 0..23) field = value else -1
        }

    var min = -1
        set(value) {
            if (value in 0..59) field = value else -1
        }

    var sec = -1
        set(value) {
            if (value in 0..59) field = value else -1
        }

    constructor()
    constructor(year: Int, month: Int, day: Int, hrs: Int, min: Int, sec: Int) {
        this.year = year
        this.month = month
        this.day = day
        this.hrs = hrs
        this.min = min
        this.sec = sec
    }
}