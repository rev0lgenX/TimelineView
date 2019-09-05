package com.example.timeline.view.data

class Timeline{
    var dob = -1
    var timelineInfo = mutableListOf<TimelineInfo>()


    public fun addTimelineInfo(list:List<TimelineInfo>){
        timelineInfo.addAll(list)
    }

    public fun clearInfo(){
        timelineInfo.clear()
    }

}