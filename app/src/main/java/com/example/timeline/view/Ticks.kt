package com.example.timeline.view

import android.content.Context
import kotlin.math.ceil
import android.graphics.*
import android.util.Log
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round


class Ticks {

    //static const double Margin = 20.0;
    //  static const double Width = 40.0;
    //  static const double LabelPadLeft = 5.0;
    //  static const double LabelPadRight = 1.0;
    //  static const int TickDistance = 16;
    //  static const int TextTickDistance = 64;
    //  static const double TickSize = 15.0;
    //  static const double SmallTickSize = 5.0;

    private val margin: Double = 20.0
    private val width: Double = 20.0
    private val labelPadLeft = 5.0
    private val labelPadRight = 1.0
    private val tickDistance = 40
    private val textTickDistance = 160
    private val tickSize = 40
    private val smallTickSize = 20.0
    private val gutterWidth = 100


    //  void paint(PaintingContext context, Offset offset, double translation,
    //      double scale, double height, Timeline timeline) {
    fun onDraw(context:Context,canvas: Canvas, offset: Point, translation: Double, scale: Double, height: Int) {

        val bottom = height

        var tickDist = tickDistance.toDouble()
        var textTickDist = textTickDistance.toDouble()

        var scaledTickDistance = tickDist * scale

//        if (scaledTickDistance > 2 * tickDistance) {
//            while (scaledTickDistance > 2 * tickDistance && tickDist >= 2.0) {
//                scaledTickDistance /= 2.0
//                tickDist /= 2.0
//                textTickDist /= 2.0
//            }
//        } else {
//            while (scaledTickDistance < tickDistance) {
//                scaledTickDistance *= 2.0
//                tickDist *= 2.0
//                textTickDist *= 2.0
//            }
//        }

        val numTicks = (ceil(height / scaledTickDistance) + 2).toInt()


        if (scaledTickDistance > textTickDistance) {
            textTickDist = tickDist
        }

        var tickOffset: Double
        var startingTickMarkValue: Double

        val y = (translation - bottom) / scale

        startingTickMarkValue = y - (y % tickDist)


        tickOffset = - (y % tickDistance) * scale - scaledTickDistance

        tickOffset -= scaledTickDistance

        Log.d("Ticks", startingTickMarkValue.toString())

        startingTickMarkValue -= tickDist

        canvas.drawRect(
            Rect(offset.x, offset.y, gutterWidth, height),
            Paint().apply {
                color = Color.argb(200, 67  , 174, 152)
            })

        for (i in 0 until numTicks) {

            tickOffset += scaledTickDistance

            var tt = round(startingTickMarkValue)

            tt = -tt

            val o = floor(tickOffset)

            if ((tt % textTickDist).toInt() == 0) {
                canvas.drawRect(
                    Rect(
                        offset.x + gutterWidth - tickSize,
                        (offset.y + height - o).toInt(), gutterWidth, (offset.y + height - o).toInt() + 2
                    ),

                    Paint().apply { color = Color.WHITE })

                //textpaint

                val value = abs(round(tt))


            } else {
                /// If we're within two text-ticks, just draw a smaller line.
                canvas.drawRect(
                    Rect((offset.x + gutterWidth - smallTickSize).toInt(),
                        (offset.y + height - o).toInt(), gutterWidth, (offset.y + height - o).toInt() + 2),
                    Paint().apply { color = Color.WHITE })
            }

            startingTickMarkValue += tickDist
        }
    }

    fun dpTopixel(c: Context, dp: Float): Float {
        val density = c.resources.displayMetrics.density
        return dp * density
    }
}