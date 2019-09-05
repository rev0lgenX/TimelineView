package com.example.timeline

import org.junit.Test

import org.junit.Assert.*
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun time(){
        Duration.between(LocalDateTime.now(), LocalDateTime.now().plusMonths(11)).toHours().let {
            println(it)
        }

        ChronoUnit.MONTHS.between(LocalDate.now(), LocalDate.now().plusYears(10)).let {
            println(it)
        }
    }
}
