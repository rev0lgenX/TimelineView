package com.example.timeline.view.view2

class TimelineAttrs{
    var longTickDistance = LONG_TICK_DISTANCE
        set(value) {
            shortTickDistance = value/4
            field = value
        }
    var shortTickDistance = longTickDistance/4
    var timelineWidth = TIMELINE_WIDTH

    var shortTickSize = SHORT_TICK_SIZE
    var longTickSize = LONG_TICK_SIZE

    companion object{
        const val LONG_TICK_SIZE = 40
        const val SHORT_TICK_SIZE = 20
        const val LONG_TICK_DISTANCE = 160
        const val TIMELINE_WIDTH = 100
    }
}