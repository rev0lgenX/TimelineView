package com.example.timelinelib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatDrawableManager
import com.example.timelinelib.adapter.TimelineImageAdapter
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.listener.OnAssetBehaviourListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.example.timelinelib.view.TimelineRenderer
import com.google.android.material.floatingactionbutton.FloatingActionButton


class TimelineView(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    RelativeLayout(context, attributeSet, defStyle),
    OnAssetBehaviourListener {

    private val TAG = TimelineView::class.java.simpleName

    private val currentVisibleAssets = mutableMapOf<Int, TimelineAssetLocation>()
    private lateinit var tView: TimelineRenderer
    private var assistantTopPadding = 0
    private var assistantBottomPadding = 0

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


        attributeSet?.let {
            context.theme.obtainStyledAttributes(it, R.styleable.TimelineView, 0, 0).apply {

                assistantTopPadding = getDimensionPixelSize(
                    R.styleable.TimelineView_assistantPaddingTop, 0
                )

                assistantBottomPadding = getDimensionPixelSize(
                    R.styleable.TimelineView_assistantPaddingBottom, 0
                )

            }
        }

        tView.id = R.id.timelineRendererId
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isDuplicateParentStateEnabled = true
        addView(tView)
        addView(getUpAssistantView().apply {
            this.visibility = View.GONE
        })
        addView(getDownAssistantView().apply {
            this.visibility = View.GONE
        })

        tView.timelineAssetBehaviourListener = this


    }


    override fun showAssetAssistant() {
        if (currentVisibleAssets.isNotEmpty()) return

        tView.assetAboveScreen?.let {

            findViewById<LinearLayout>(R.id.upAssistantLayoutId)?.let { upperAssis ->
                upperAssis.visibility = View.VISIBLE
                upperAssis.setOnClickListener {_->
                    scrollToTimeline(it.id)
                    hideAssetAssistant()

                }
            }
        }


//                addView(
//                    getUpAssistantView().apply {
//                        setOnClickListener { _ ->
//                            scrollToTimeline(it.id)
//                            hideAssetAssistant()
//                        }
//                    }
//                )

        tView.assetBelowScreen?.let {
//            if (findViewById<LinearLayout>(R.id.downAssistantLayoutId) == null) {
                findViewById<LinearLayout>(R.id.downAssistantLayoutId)?.let { downAssis ->
                    downAssis.visibility = View.VISIBLE

                    downAssis.setOnClickListener {_->
                        scrollToTimeline(it.id)
                        hideAssetAssistant()
//
                    }
                }
//                addView(
//                    getDownAssistantView().let { lin ->
//                        lin.setOnClickListener { _ ->
//                            scrollToTimeline(it.id)
//                            hideAssetAssistant()
//                        }
//                        lin
//                    }
//                )

        }

    }


    override fun hideAssetAssistant() {
        findViewById<LinearLayout>(R.id.upAssistantLayoutId)?.visibility = View.GONE
        findViewById<LinearLayout>(R.id.downAssistantLayoutId)?.visibility = View.GONE
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
                                800,
                                200
                            ).let { params ->
                                params.topMargin = it.value.rectF.bottom.toInt()
                                params.leftMargin = width - params.width
                                params
                            }
                            iv.scaleType = ImageView.ScaleType.CENTER_CROP
                            iv.id = it.value.asset.id
                            it.value.asset.image?.let { iv.setImageResource(it) }
                            iv
                        }
                    })
            }
        }

        findViewById<TimelineRenderer>(R.id.timelineRendererId).bringToFront()

        findViewById<LinearLayout>(R.id.upAssistantLayoutId)?.bringToFront()
        findViewById<LinearLayout>(R.id.downAssistantLayoutId)?.bringToFront()
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


    private fun getUpAssistantView(): LinearLayout {
        return LinearLayout(context).let { lin ->
            lin.id = R.id.upAssistantLayoutId
            lin.layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(ALIGN_PARENT_TOP)
                    addRule(CENTER_HORIZONTAL)
                    setMargins(0, 50 + assistantTopPadding, 0, 0)
                }

            lin.isClickable = true
            lin.orientation = LinearLayout.VERTICAL

            FloatingActionButton(context).let { flo ->
                flo.id = R.id.upAssistantImageId
                flo.layoutParams =
                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                flo.size = FloatingActionButton.SIZE_MINI
                flo.compatElevation = 0f
                flo.setImageDrawable(
                    AppCompatDrawableManager.get().getDrawable(
                        context,
                        R.drawable.ic_up_arrow
                    )
                )
                lin.addView(flo)
            }

            lin
        }
    }


    private fun getDownAssistantView(): LinearLayout {
        return LinearLayout(context).let { lin ->
            lin.id = R.id.downAssistantLayoutId
            lin.layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(ALIGN_PARENT_BOTTOM)
                    addRule(CENTER_HORIZONTAL)
                    setMargins(0, 0, 0, 50 + assistantBottomPadding)
                }
            lin.isClickable = true
            lin.orientation = LinearLayout.VERTICAL

            FloatingActionButton(context).let { flo ->
                flo.id = R.id.downAssistantImageId
                flo.layoutParams =
                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                flo.size = FloatingActionButton.SIZE_MINI
                flo.compatElevation = 0f
//                flo.isClickable = true
                flo.setImageDrawable(
                    AppCompatDrawableManager.get().getDrawable(
                        context,
                        R.drawable.ic_down_arrow
                    )
                )
                lin.addView(flo)
            }

            lin
        }
    }

}
