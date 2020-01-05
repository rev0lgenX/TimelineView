package com.example.timeline

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.timelinelib.core.asset.TimelineAsset
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)

        initDummy()

        timelineView.setOnAssetClickListener(object : TimelineAssetClickListener {
            override fun onAssetClick(asset: TimelineAsset) {
//                startActivity(Intent(this@MainActivity,AssetActivity::class.java).apply{
//                    putExtra("asset_data", AssetContainer(asset.id, asset.description!!))
//                })

                timelineView.scrollToTimeline(3)
            }
        })
    }

    private fun initDummy() {
        var i = 0

        timelineView.timelineEntry = TimelineEntry().apply {
            birthTime = DateTime(1996, 12, 12)
            startTime = DateTime(birthTime?.localDate?.minusYears(2)!!)
            endTime = DateTime(startTime!!.localDate.plusYears(40))

            timelineAssets = listOf(
                TimelineAsset(
                    i++,
                    DateTime(birthTime?.years!!, birthTime?.months!!, birthTime?.days!!),
                    null,
                    "Birth",
                    "Beginning of new life",
                    Color.parseColor("#64dd17"),
                    null
                ),
                TimelineAsset(
                    i++,
                    DateTime(2000, 12, 24),
                    DateTime(2013, 4, 24),
                    null,
                    "Small health problem",
                    Color.parseColor("#64dd17"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2005, 4, 24),
                    DateTime(2017, 12, 24),
                    null,
                    "Small Accident might occur ",
                    Color.parseColor("#ff3d00"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2002, 12, 24),
                    DateTime(2004, 12, 24),
                    null,
                    "Successful in your study",
                    Color.parseColor("#455a64"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2003, 12, 24),
                    DateTime(2016, 12, 24),
                    null,
                    "travel abroad",
                    Color.parseColor("#f44336"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2013, 12, 24),
                    null,
                    null,
                    "Something will happen",
                    Color.parseColor("#4a148c"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2006, 12, 24),
                    null,
                    null,
                    "Something will happen",
                    Color.parseColor("#4a148c"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2015, 12, 24),
                    null,
                    null,
                    "Something will happen",
                    Color.parseColor("#4a148c"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2005, 12, 24),
                    DateTime(2020, 12, 24),
                    null,
                    "Something will happen",
                    Color.parseColor("#4a148c"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2016, 12, 24),
                    null,
                    null,
                    "Beginning of career",
                    Color.parseColor("#9d46ff"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2022, 12, 24),
                    null,
                    null,
                    "You might find your future partner",
                    Color.parseColor("#455a64"),
                    android.R.drawable.ic_menu_report_image
                ),
                TimelineAsset(
                    i++,
                    DateTime(2010, 12, 24),
                    null,
                    null,
                    "Something will happen",
                    Color.parseColor("#4a148c"),
                    android.R.drawable.ic_menu_report_image
                )
            )
        }
    }


}
