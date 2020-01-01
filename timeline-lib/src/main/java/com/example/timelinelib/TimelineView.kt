package com.example.timelinelib

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.timelinelib.adapter.TimelineImageAdapter
import com.example.timelinelib.core.asset.TimelineAssetLocation
import com.example.timelinelib.core.asset.TimelineEntry
import com.example.timelinelib.core.util.dp
import com.example.timelinelib.listener.OnTimelineBehaviourListener
import com.example.timelinelib.listener.TimelineAssetClickListener
import com.example.timelinelib.view.TimelineEndListener
import com.example.timelinelib.view.TimelineRenderer
import com.google.android.material.floatingactionbutton.FloatingActionButton


typealias OnAssetVisibleListener = ((MutableMap<Int, TimelineAssetLocation>) -> Unit)?

class TimelineView(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    RelativeLayout(context, attributeSet, defStyle),
    OnTimelineBehaviourListener {

    private val TAG = TimelineView::class.java.simpleName

    private val currentVisibleAssets = mutableMapOf<Int, TimelineAssetLocation>()
    private lateinit var tView: TimelineRenderer
    private var assistantTopPadding = 0
    private var assistantBottomPadding = 0
    private val indicatorColor = ContextCompat.getColor(context, R.color.indicatorColor)
    private val animationVisibleTime = 200L
    private var childImage: View? = null
    private var assetBottomPosition = -100

    private val imageHeight = context.dp(180f).toInt()

    private lateinit var springAnimationX: SpringAnimation
    private lateinit var springAnimationY: SpringAnimation

    private lateinit var relativeLayout1: RelativeLayout

    /**
     * adapter if you  want to use another loader for image
     * TODO:// NOT TESTED
     *
     * */
    var timelineImageAdapter: TimelineImageAdapter? = null

    /**
     * listener which gives currently visible asset on the screen
     * */
    var assetVisibleListener: OnAssetVisibleListener = null

    /**
     * listener which is invoked when timeline reaches endtime of TimelineEntryClass
     * */
    var timelineEndListener:TimelineEndListener = null


    /**
     * timelineEntry which contains all the assets
     * */
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

        relativeLayout1 = RelativeLayout(context)
        relativeLayout1.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        relativeLayout1.addView(tView)

        addView(relativeLayout1)

        addView(getUpAssistantView().apply {
            this.visibility = View.GONE
        })

        addView(getDownAssistantView().apply {
            this.visibility = View.GONE
        })

        tView.timelineTimelineBehaviourListener = this
        tView.timelineEndListener = {
            timelineEndListener?.invoke(it)
        }
    }


    override fun showAssetAssistant() {
        if (currentVisibleAssets.isNotEmpty()) return

        handler.postDelayed({
            tView.assetAboveScreen?.let {

                findViewById<LinearLayout>(R.id.upAssistantLayoutId)?.let { upperAssis ->
                    if (upperAssis.visibility == View.VISIBLE) return@let


//                    ImageView(context).apply {
//                        layoutParams = LayoutParams(500, 500).let { param ->
//                            param.addRule(CENTER_IN_PARENT)
//                            param
//                        }
//                        this.setImageResource(android.R.drawable.ic_menu_report_image)
//                        this.alpha = 0f
//                        relativeLayout1.addView(this as View)
//
//                        ViewCompat.animate(this as View)
//                            .alpha(1f)
//                            .setDuration(1000)
//                            .start()
//
//                    }


                    upperAssis.alpha = 0f
                    upperAssis.translationY = -100f
                    upperAssis.visibility = View.VISIBLE
                    ViewCompat.animate(upperAssis)
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()

                    upperAssis.setOnClickListener { _ ->
                        scrollToTimeline(it.id)
                        hideAssetAssistant()
                    }

                }

                findViewById<TextView>(R.id.upAssistantTextViewId)?.let { tv ->
                    tv.text = it.title ?: it.description ?: ""
                }
            }

            tView.assetBelowScreen?.let {
                findViewById<LinearLayout>(R.id.downAssistantLayoutId)?.let { downAssis ->
                    if (downAssis.visibility == View.VISIBLE) return@let
                    downAssis.alpha = 0f
                    downAssis.visibility = View.VISIBLE
                    downAssis.translationY = 100f

                    ViewCompat.animate(downAssis)
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()

                    downAssis.setOnClickListener { _ ->
                        scrollToTimeline(it.id)
                        hideAssetAssistant()
                    }
                }

                findViewById<TextView>(R.id.downAssistantTextViewId)?.let { tv ->
                    tv.text = it.title ?: it.description ?: ""
                }
            }

        }, animationVisibleTime)


    }


    override fun hideAssetAssistant() {
        handler.removeCallbacksAndMessages(null)

        findViewById<LinearLayout>(R.id.upAssistantLayoutId)?.let { upperAssis ->
            upperAssis.visibility = View.GONE
        }

        findViewById<LinearLayout>(R.id.downAssistantLayoutId)?.let { downAssis ->
            downAssis.visibility = View.GONE
        }

    }

    override fun onAssetVisible(assetLocation: MutableMap<Int, TimelineAssetLocation>) {
        val removableAssets = mutableListOf<Int>()

        currentVisibleAssets.forEach {
            if (!assetLocation.containsKey(it.key)) {
                findViewById<View>(it.key)?.let {
                    relativeLayout1.removeView(it)
                }
                removableAssets.add(it.key)
            }
        }

        removableAssets.forEach {
            currentVisibleAssets.remove(it)
        }

        removableAssets.clear()


        //TODO: SOLVE OVERLAPPING IMAGES

        assetBottomPosition = -100

        assetLocation.values.sortedBy { it.rectF.top }.forEach { currentAssetMap ->
            val key = currentAssetMap.asset.id
            val rectF = currentAssetMap.rectF
            val assetView: View?

            if (currentVisibleAssets.containsKey(key)) {
                assetView = relativeLayout1.findViewById<View>(key)?.let { iv ->
                    (iv.layoutParams as LayoutParams).topMargin =
                        rectF.bottom.toInt() + 10
                    iv.requestLayout()
                    iv
                }

            } else {
                currentVisibleAssets[key] = currentAssetMap

                assetView =
                    timelineImageAdapter?.getImageContainer(
                        currentAssetMap.asset.image ?: 0
                    )?.let { vi ->
                        (vi.layoutParams as? LayoutParams)?.let { params ->
                            params.topMargin = rectF.bottom.toInt() + 10
                            params.leftMargin = width - params.width
                        }
                        vi
                    } ?: let { _ ->
                        ImageView(context).let { iv ->
                            iv.layoutParams = LayoutParams(
                                context.dp(200f).toInt(),
                                imageHeight
                            ).let { params ->
                                params.topMargin = rectF.bottom.toInt() + 10
                                params.addRule(ALIGN_PARENT_END)
                                params
                            }
                            iv.scaleType = ImageView.ScaleType.FIT_CENTER
                            iv.id = key
                            currentAssetMap.asset.image?.let { iv.setImageResource(it) }
                            iv
                        }
                    }

                assetView.alpha = 0f
                relativeLayout1.addView(assetView)

                childImage = assetView


                if (key != 0) {
                    ViewCompat.animate(assetView)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                } else {
                    assetView.let { iv ->
                        iv.scaleX = 0f
                        iv.scaleY = 0f
                        iv.alpha = 1f
                        springAnimationX = createSpringAnimation(
                            iv,
                            SpringAnimation.SCALE_X,
                            1f,
                            SpringForce.STIFFNESS_MEDIUM,
                            SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                        )

                        springAnimationY = createSpringAnimation(
                            iv,
                            SpringAnimation.SCALE_Y,
                            1f,
                            SpringForce.STIFFNESS_MEDIUM,
                            SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                        )

                        springAnimationX.start()
                        springAnimationY.start()
                    }

                }
            }


            if (assetView?.marginTop!! < assetBottomPosition) {
                assetView.visibility = View.GONE
            } else {
                assetView.visibility = View.VISIBLE
                assetBottomPosition = assetView.marginTop.plus(assetView.height)
            }
        }

        assetVisibleListener?.invoke(assetLocation)
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


    private fun getUpAssistantView(): LinearLayout {
        return if (!isInEditMode) {
            LinearLayout(context).let { lin ->
                lin.id = R.id.upAssistantLayoutId
                lin.layoutParams =
                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                        addRule(ALIGN_PARENT_TOP)
                        addRule(CENTER_HORIZONTAL)
                        setMargins(0, 50 + assistantTopPadding, 0, 0)
                    }

                lin.isClickable = true
                lin.isFocusable = true
                lin.orientation = LinearLayout.VERTICAL


                FloatingActionButton(context).let { flo ->
                    flo.id = R.id.upAssistantImageId
                    flo.layoutParams =
                        LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                        )
                            .apply {
                                this.setMargins(50)
                                this.gravity = Gravity.CENTER
                            }

                    flo.size = FloatingActionButton.SIZE_MINI
                    flo.compatElevation = 5f
                    flo.setImageDrawable(
                        AppCompatDrawableManager.get().getDrawable(
                            context,
                            R.drawable.ic_up_arrow
                        )
                    )
                    flo.supportBackgroundTintList = ColorStateList.valueOf(indicatorColor)
                    lin.addView(flo)

                }

                TextView(context).let { textView ->
                    textView.id = R.id.upAssistantTextViewId
                    textView.layoutParams =
                        LinearLayout.LayoutParams(
                            context.dp(200f).toInt(),
                            LayoutParams.WRAP_CONTENT
                        ).apply {
                            this.gravity = Gravity.CENTER
                        }
                    textView.maxLines = 2
                    textView.typeface = ResourcesCompat.getFont(context, R.font.open_sans_semi_bold)
                    textView.textSize = 16f
                    textView.gravity = Gravity.CENTER
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.setTextColor(indicatorColor)
                    lin.addView(textView)
                }

                lin
            }
        } else LinearLayout(context)
    }


    private fun getDownAssistantView(): LinearLayout {
        return if (!isInEditMode) {
            LinearLayout(context).let { lin ->
                lin.id = R.id.downAssistantLayoutId
                lin.layoutParams =
                    LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                        addRule(ALIGN_PARENT_BOTTOM)
                        addRule(CENTER_HORIZONTAL)
                        setMargins(0, 0, 0, 50 + assistantBottomPadding)
                    }
                lin.isClickable = true
                lin.orientation = LinearLayout.VERTICAL

                TextView(context).let { textView ->
                    textView.id = R.id.downAssistantTextViewId
                    textView.layoutParams =
                        LinearLayout.LayoutParams(
                            context.dp(200f).toInt(),
                            LayoutParams.WRAP_CONTENT
                        ).apply {
                            this.gravity = Gravity.CENTER
                        }

                    textView.maxLines = 2
                    textView.typeface = ResourcesCompat.getFont(context, R.font.open_sans_semi_bold)
                    textView.textSize = 16f
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.gravity = Gravity.CENTER
                    textView.setTextColor(indicatorColor)
                    lin.addView(textView)
                }

                FloatingActionButton(context).let { flo ->
                    flo.id = R.id.downAssistantImageId
                    flo.layoutParams =
                        LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                        )
                            .apply {
                                this.setMargins(50)
                                gravity = Gravity.CENTER
                            }
                    flo.size = FloatingActionButton.SIZE_MINI
                    flo.compatElevation = 5f
                    flo.supportBackgroundTintList = ColorStateList.valueOf(indicatorColor)
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
        } else LinearLayout(context)
    }


    fun createSpringAnimation(
        view: View,
        property: DynamicAnimation.ViewProperty,
        finalPosition: Float,
        stiffness: Float,
        dampingRatio: Float
    ): SpringAnimation {
        val animation = SpringAnimation(view, property)
        val spring = SpringForce(finalPosition)
        spring.stiffness = stiffness
        spring.dampingRatio = dampingRatio
        animation.spring = spring
        return animation
    }

}
