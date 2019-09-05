package com.example.timeline.view.view2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat

class TimelineManager(private val context: Context) : TickWorkerListener
    , GestureDetector.OnGestureListener
    , ScaleGestureDetector.OnScaleGestureListener {


    private var lastFocalPoint: Float? = null
    private var lastGestureDetector: ScaleGestureDetector? = null
    private val TAG = TimelineManager::class.java.simpleName

    private fun invalidate() = view.invalidate()
    private val tickWorker = TickWorker(this)
    private val timelineTracker = TimelineTracker(TimelineEntry())
    private var scaleFactor = 1.0
    private var currentScale = 1.0
    private var tempScale = 1.0

    private var isScaling = false
    private var lastArbitraryStart = 0.0

    private var gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, this)
    private var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)


    var attrs: TimelineAttrs? = null
        set(value) {
            timelineTracker.attrs = value
            field = value
        }

    lateinit var view: TimelineView


    fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP) {
            isScaling = false
        }
        return true
    }

    fun manage(canvas: Canvas?) {
        tickWorker.work(canvas, attrs!!, view.height, scaleFactor, Point(0, 0), timelineTracker)
    }

    fun submitEntry(entry: TimelineEntry) {
        timelineTracker.timelineEntry = entry
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        lastArbitraryStart = timelineTracker.arbitraryStart
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        if (isScaling) return true

        val focalDiff = (p0?.y!! - p1?.y!!) / currentScale

        timelineTracker.arbitraryStart = lastArbitraryStart + focalDiff

        if (timelineTracker.arbitraryStart <= 0) {
            timelineTracker.arbitraryStart = 0.0
        }

        invalidate()
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        isScaling = true
        lastFocalPoint = p0?.focusY
        lastArbitraryStart = timelineTracker.arbitraryStart
        tempScale = p0?.scaleFactor?.toDouble() ?: 1.0
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        currentScale = scaleFactor
    }

    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        scaleFactor *= p0?.scaleFactor?.toDouble() ?: 1.0
        tempScale *= p0?.scaleFactor?.toDouble() ?: 1.0


        val focus = lastArbitraryStart + p0?.focusY!!
        val focalDiff = (lastArbitraryStart + lastFocalPoint!!) - focus

        timelineTracker.arbitraryStart = focus + (lastArbitraryStart - focus) / tempScale + focalDiff
        timelineTracker.focalDistance = focus
        timelineTracker.focalPoint = p0.focusY.toDouble()

        if (timelineTracker.arbitraryStart <= 0) {
            timelineTracker.arbitraryStart = 0.0
        }

        invalidate()
        return true
    }

    override fun onScaleReset() {
        scaleFactor = 1.0
        currentScale = 1.0
        tempScale = 1.0
        lastArbitraryStart = timelineTracker.arbitraryStart
    }

    override fun stopExpanding() {
        currentScale = 2.0
        scaleFactor = 2.0
    }

    override fun stopContracting() {
        currentScale = 0.5
        scaleFactor = 0.5
    }
}