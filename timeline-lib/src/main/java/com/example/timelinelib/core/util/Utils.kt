package com.example.timelinelib.core.util

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()

//return sp dimension value in pixels
fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()

//converts px value into dip or sp
fun Context.px2dip(px: Int): Float = px.toFloat() / resources.displayMetrics.density
fun Context.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity

fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

//returns dip(dp) dimension value in pixels
fun View.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun View.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()

//return sp dimension value in pixels
fun View.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun View.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()

//converts px value into dip or sp
fun View.px2dip(px: Int): Float = px.toFloat() / resources.displayMetrics.density
fun View.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity

fun View.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)
