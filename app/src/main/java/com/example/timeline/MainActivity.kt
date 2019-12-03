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
import org.threeten.bp.LocalDate

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)

        val list = mutableListOf<TimelineAsset>()
        list.add(
            TimelineAsset(
                list.size,
                DateTime(
                    2020, 12, 24
                ),
                null,
                null,
                "I am born. ",
                Color.parseColor("#64dd17"),
                image = android.R.drawable.menu_full_frame
            )
        )

        list.add(
            TimelineAsset(
                list.size,

                DateTime(2019, 12, 1),
                DateTime(2022, 12, 1),
                null,
                "Something is cooking. You can see it yourself in the future",
                Color.parseColor("#ff3d00"),
                android.R.drawable.menu_full_frame
            )
        )

        list.add(
            TimelineAsset(
                list.size,
                DateTime(
                    2026, 12, 1
                ),
                null,
                null,
                "I am noticed",
                Color.parseColor("#f57f17"),
                android.R.drawable.menu_full_frame
            )
        )

        list.add(
            TimelineAsset(
                list.size,
                DateTime(2045, 12, 24),
                DateTime(2055, 4, 3),
                null,
                "This is the future mee I am the most successful app right now",
                Color.parseColor("#455a64"),
                android.R.drawable.menu_full_frame
            )
        )

        list.add(
            TimelineAsset(
                list.size,
                DateTime(2040, 5, 17),
                DateTime(2050, 6, 16),
                null,
                "I am the one",
                Color.parseColor("#9d46ff"),
                android.R.drawable.menu_full_frame
            )
        )

        list.add(
            TimelineAsset(
                list.size,
                DateTime(2050, 8, 18),
                DateTime(2055, 6, 16),
                null,
                "I am above all"
            )
        )

        timelineView.timelineEntry =
            TimelineEntry().apply {
                timelineAssets = list
            }

        timelineView.setOnAssetClickListener(object : TimelineAssetClickListener {
            override fun onAssetClick(asset: TimelineAsset) {
//                startActivity(Intent(this@MainActivity,AssetActivity::class.java).apply{
//                    putExtra("asset_data", AssetContainer(asset.id, asset.description!!))
//                })

                timelineView.scrollToTimeline(3)
            }
        })
    }

}
