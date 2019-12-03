package com.example.timelinelib.core.util

import android.content.Context
import android.util.DisplayMetrics

fun Context.convertDpToPixel(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.convertSpToPixel(sp: Float): Float {
    return sp * (resources.displayMetrics.scaledDensity / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.convertPixelsToDp(px: Float): Float {
    return px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.convertPixelsToSp(px: Float): Float {
    return px / (resources.displayMetrics.scaledDensity / DisplayMetrics.DENSITY_DEFAULT)
}