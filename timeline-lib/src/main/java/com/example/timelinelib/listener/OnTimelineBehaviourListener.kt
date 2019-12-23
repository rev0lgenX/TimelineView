package com.example.timelinelib.listener

import com.example.timelinelib.core.asset.TimelineAssetLocation

interface OnTimelineBehaviourListener {
    fun onAssetVisible(assetLocation: MutableMap<Int,TimelineAssetLocation>)
    fun showAssetAssistant()
    fun hideAssetAssistant()
    }