package com.example.timeline

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.timelinelib.core.asset.TimelineAsset
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.DateTime
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)

        val list = mutableListOf<TimelineAsset>()
        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault())!!
            }, null, "I am born. ", Color.parseColor("#64dd17"), null)
        )

        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(2)!!
            }, DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(4)!!
            }, "Something is cooking. You can see it yourself in the future", Color.parseColor("#ff3d00"), null)
        )

        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(6)!!
            }, null, "I am noticed", Color.parseColor("#f57f17"), null)
        )

        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(8)!!
            }, DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(15)!!
            }, "This is the future mee I am the most successful app right now", Color.parseColor("#455a64"), null)
        )

        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(20)!!
            }, DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(45)!!
            }, "I am the one", Color.parseColor("#9d46ff"), null)
        )

        list.add(
            TimelineAsset(list.size,DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(20)!!
            }, DateTime().apply {
                dateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusYears(45)!!
            }, "I am above all")
        )

        timelineView.timelineEntry =
            TimelineEntry().apply {
                timelineAssets = list
            }

//        timelineView.timelineAssetClickListener = object :TimelineAssetClickListener{
//            override fun onAssetClick(asset: TimelineAsset) {
//                startActivity(Intent(this@MainActivity,AssetActivity::class.java).apply{
//                    putExtra("asset_data", AssetContainer(asset.id, asset.description!!))
//                })
//            }

//        }
    }

}
