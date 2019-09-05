package com.example.timeline.view.view2

import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class DateTime {
    var dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault())!!
}