package com.example.timeline.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import java.nio.file.Files.size


class CustomView(context: Context, attr: AttributeSet) : View(context, attr)
    , GestureDetector.OnGestureListener
    , ScaleGestureDetector.OnScaleGestureListener {


    private val TAG = CustomView::class.java.simpleName


    private var isScaling = false

    private var ticks = Ticks()
    private val point = Point(0, 0)
    private val rect = Rect(20, 20, 40, 40)
    private val paint = Paint()

    private var start = 0.0
    private var stop = 0.0
    private var translation = 10.0
    private var scaleFactor = 1.0
    private var ticksScale = 1.0

    private var arbitraryStart = 2000.0
    private var arbitraryEnd = 4000.0

    private var lastArbitraryStart = 0.0
    private var lastArbitraryEnd = 0.0


    private var gestureDetector: GestureDetectorCompat
    private var scaleGestureDetector: ScaleGestureDetector

    init {
        paint.color = Color.CYAN
        gestureDetector = GestureDetectorCompat(context, this)
        scaleGestureDetector = ScaleGestureDetector(context, this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        ticks.onDraw(context, canvas!!, point, translation, ticksScale, height)
        canvas.drawRect(rect, paint)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        lastArbitraryStart = arbitraryStart
        lastArbitraryEnd = arbitraryEnd
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {

        if(isScaling) return false

        val scale = (lastArbitraryEnd - lastArbitraryStart) / height

        val focus = lastArbitraryStart + p1?.y?.times(scale)!!

        val focalDiff = (lastArbitraryStart + p0?.y!! * scale) - focus


        start  = (lastArbitraryStart)  + focalDiff
        stop = (lastArbitraryEnd )  + focalDiff

        val scale1 = height / (stop - start)


        arbitraryStart = start
        arbitraryEnd = stop

        translation = (- start * scale1)

        invalidate()

        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }


    //scaling
    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        Log.d("scaleBegin", p0?.scaleFactor?.toString())

        isScaling = true
        scaleFactor = p0?.scaleFactor?.toDouble() ?: 1.0

        lastArbitraryStart = start
        lastArbitraryEnd = stop
        return true
    }


    override fun onScale(p0: ScaleGestureDetector?): Boolean {

        scaleFactor *= p0?.scaleFactor?.toDouble() ?: 1.0

        val scale = (lastArbitraryEnd - lastArbitraryStart) / height

        val focus = lastArbitraryStart + p0?.focusY?.times(scale)!!

        val focalDiff = (lastArbitraryStart + p0.focusY * scale) - focus

        Log.d(TAG, "focusY" + p0.focusY.toString())

        start  = focus + (lastArbitraryStart - focus) / scaleFactor + focalDiff
        stop = focus + (lastArbitraryEnd - focus) / scaleFactor + focalDiff

        val scale1 = height / (stop - start)

        translation = (- start * scale1)

        arbitraryStart = start
        arbitraryEnd=stop

        ticksScale = scale1
//
        Log.d(TAG,"tickScale:"+ scale1.toString())
//        Log.d(TAG,"ticktranslation:"+ translation.toString())
//        Log.d(TAG,"onscale Scale:"+ scale.toString())
//        Log.d(TAG,"focus:"+ focus.toString())
//        Log.d(TAG,"start:"+ start.toString())
//        Log.d(TAG,"end"+ end.toString())
//        Log.d(TAG,"foculdiff"+ focalDiff.toString())

        invalidate()

        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        isScaling = false
        Log.d("scaleStop", p0?.scaleFactor?.toString())

    }


}