package com.example.timelinelib.listener

interface TickWorkerListener{
    fun onScaleReset()
    fun stopExpanding()
    fun stopContracting()
}