package com.example.timelinelib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.timelinelib.adapter.TimelineImageAdapter
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.listener.OnAssetVisibleListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.example.timelinelib.view.TimelineRenderer

class TimelineView(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    RelativeLayout(context, attributeSet, defStyle),
    OnAssetVisibleListener {

    private val TAG = TimelineView::class.java.simpleName

    private val currentVisibleAssets = mutableMapOf<Int, TimelineAssetLocation>()
    private lateinit var tView: TimelineRenderer

    var timelineImageAdapter: TimelineImageAdapter? = null

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
        tView.timelineAssetVisibleListener = this
    }

    override fun onAssetVisible(assetLocation: MutableMap<Int, TimelineAssetLocation>) {
        val removableAssets = mutableListOf<Int>()

        currentVisibleAssets.forEach {
            if (!assetLocation.containsKey(it.key)) {
                findViewById<View>(it.key)?.let { removeView(it) }
                removableAssets.add(it.key)
            }
        }

        removableAssets.forEach {
            currentVisibleAssets.remove(it)
        }

        removableAssets.clear()

        assetLocation.forEach {
            if (currentVisibleAssets.containsKey(it.key)) {
                findViewById<View>(it.key)?.let { iv ->
                    (iv.layoutParams as LayoutParams).topMargin = it.value.rectF.bottom.toInt() + 10
                iv.requestLayout()
                }
            } else {
                currentVisibleAssets[it.key] = it.value

                addView(
                    timelineImageAdapter?.getImageContainer(
                        it.value.asset.image ?: 0
                    )?.let { vi ->
                        (vi.layoutParams as? LayoutParams)?.let { params ->
                            params.topMargin = it.value.rectF.bottom.toInt() + 10
                            params.leftMargin = width - params.width
                        }
                        vi
                    } ?: let { _ ->
                        ImageView(context).let { iv ->
                            iv.layoutParams = LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                300
                            ).let { params ->
                                params.topMargin = it.value.rectF.bottom.toInt() + 10
                                params.leftMargin = width - params.width
                                params
                            }
                            iv.scaleType = ImageView.ScaleType.CENTER_CROP
                            iv.id = it.value.asset.id
                            iv.setImageResource(it.value.asset.image!!)
                            iv
                        }
                    })
            }
        }

        findViewById<TimelineRenderer>(R.id.timelineRendererId).bringToFront()
    }


    /**
     * Provide asset assetId to scroll to the given asset position
     * @params assetId
     *
     * */
    fun scrollToTimeline(assetId: Int) {
        tView.scrollToTimeline(assetId)
    }

    /**
     * Register callback to be invoked when timeline asset text is clicked.
     * @param assetClickListener
     */
    fun setOnAssetClickListener(assetClickListener: TimelineAssetClickListener) {
        tView.timelineAssetClickListener = assetClickListener
    }

    /**
     * Unregister assetClickListener callback
     */
    fun removeAssetClickListener() {
        tView.timelineAssetClickListener = null
    }

}
