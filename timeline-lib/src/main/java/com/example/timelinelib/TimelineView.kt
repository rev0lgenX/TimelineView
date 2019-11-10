package com.example.timelinelib

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.listener.OnAssetVisibleListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.example.timelinelib.view.TimelineRenderer

class TimelineView(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    RelativeLayout(context, attributeSet, defStyle),
    OnAssetVisibleListener {

    private val TAG = TimelineView::class.java.simpleName
    //    private val currentVisibleAssets = mutableListOf<TimelineAssetLocation>()
    private val currentVisibleAssets = mutableMapOf<Int, TimelineAssetLocation>()
    private lateinit var tView: TimelineRenderer
    var timelineEntry: TimelineEntry? = null
        set(value) {
            field = value
            tView.timelineEntry = value
        }


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {
        tView = TimelineRenderer(context, attributeSet).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        tView.id = R.id.timelineRendererId

        addView(tView)
//        addView(ImageView(context).apply {
//            layoutParams = LayoutParams(200, 200).let {
//                it.topMargin = 100
//                it
//            }
//            setImageResource(android.R.drawable.alert_dark_frame)
//            scaleType = ImageView.ScaleType.CENTER_CROP
//
//        })
//
//        tView.timelineAssetClickListener = object :TimelineAssetClickListener{
//            override fun onAssetClick(asset: TimelineAsset) {
//                (getChildAt(1).layoutParams as LayoutParams).let {
//                    it.leftMargin = 100
//                    getChildAt(1).requestLayout()
//                }
//                findViewById<TimelineRenderer>(R.id.timelineRendererId).bringToFront()
//            }
//        }

        tView.timelineAssetVisibleListener = this
    }

    override fun onAssetVisible(assetLocation: MutableMap<Int, TimelineAssetLocation>) {

        val removableAssets = mutableListOf<Int>()
        currentVisibleAssets.forEach {
            if (!assetLocation.containsKey(it.key)) {
                findViewById<ImageView>(it.key)?.let { removeView(it) }
                removableAssets.add(it.key)
            }
        }

        removableAssets.forEach {
            currentVisibleAssets.remove(it)
        }

        removableAssets.clear()

        assetLocation.forEach {
            if (currentVisibleAssets.containsKey(it.key)) {
                findViewById<ImageView>(it.key)?.let { iv ->
                    (iv.layoutParams as LayoutParams).let { params ->
                        params.topMargin = it.value.rectF.top.toInt()
                    }
                    iv.requestLayout()
                }
            } else {
                currentVisibleAssets[it.key] = it.value
                addView(ImageView(context).let { iv ->
                    iv.layoutParams = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    ).let { params ->
                        params.topMargin = it.value.rectF.top.toInt()
                        params.leftMargin = width - 200
                        params
                    }
                    iv.id = it.value.asset.id
                    iv.setImageResource(android.R.drawable.alert_dark_frame)
                    iv
                })
            }
        }

        findViewById<TimelineRenderer>(R.id.timelineRendererId).bringToFront()
    }

    fun setOnAssetClickListener(assetClickListener: TimelineAssetClickListener) {
        tView.timelineAssetClickListener = assetClickListener
    }

    fun removeAssetClickListener() {
        tView.timelineAssetClickListener = null
    }

}
