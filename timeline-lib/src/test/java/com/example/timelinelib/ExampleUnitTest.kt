package com.example.timelinelib

import android.graphics.Point
import android.graphics.RectF
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertTrue(IntRange(40,0).contains(20))
    }

    @Mock
    lateinit var rectF: RectF


    @Test
    fun testSublist(){
        arrayOf("String", "heelo", "goo", "null").toList()
            .subList(3, 4)
            .forEach { println(it) }
    }



    @Test
    fun rect(){
        rectF.apply {
            left = 100f
            top = 100f
            right = 200f
            bottom = 200f
        }
        val x= 190f
        val y=190f
        assertTrue(rectF.contained(x, y))

    }

    fun RectF.contained(x: Float, y: Float): Boolean {
        return (left < right && top < bottom  // check for empty first
                && x >= left && x < right && y >= top && y < bottom)
    }

}
