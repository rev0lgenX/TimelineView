package com.example.timeline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.timeline.view.TimelineDateUtils
import com.example.timeline.view.data.Timeline
import com.example.timeline.view.data.TimelineInfo
import com.example.timeline.view.view2.TimelineManager
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)


        val timeline = Timeline().apply {
            dob = 20190102
        }

        val timelineInfo = mutableListOf<TimelineInfo>().apply {
            add(
                TimelineInfo(
                    startDate = TimelineDateUtils(2018, 12, 12, 12, 12, 12),
                    endDate = TimelineDateUtils(2019, 12, 12, 12, 12, 12),
                    message = "marriage",
                    resource = android.R.drawable.alert_dark_frame
                )
            )
        }

        timeline.timelineInfo = timelineInfo

//        customView.timeline = timeline

        timelineView.manager = TimelineManager(this)


    }
}
